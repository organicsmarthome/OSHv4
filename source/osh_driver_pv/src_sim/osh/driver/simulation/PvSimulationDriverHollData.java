package osh.driver.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.pv.PvProfileHollSingleton;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
//import osh.hal.exchange.PvHALStaticDetailsExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class PvSimulationDriverHollData extends DeviceSimulationDriver {
	
	private boolean pvSwitchedOn;
	
	private String pathToFiles;
	private String fileExtension;
	private double profileNominalPower;
	
	private int pastDaysPrediction;
	
	/** nominal power of PV (e.g. 4.6 kWpeak) */
	private final int nominalPower;
	
	/** according to inverter (technical, due to VAmax) S = SQR(P^2+Q^2) */
	@SuppressWarnings("unused")
	private final int complexPowerMax;
	
	/** according to inverter (technical, due to cosPhi: e.g. 0.8ind...0.8cap) */
	private double cosPhiMax;

	private PvProfileHollSingleton profile;

	/**
	 * CONSTRUCTOR
	 * @throws Exception
	 */
	public PvSimulationDriverHollData(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws Exception {
		super(controllerbox, deviceID, driverConfig);
		
		this.pvSwitchedOn = true;
		
//		String profileSourceName = driverConfig.getParameter("profilesource");
		this.nominalPower = Integer.valueOf(driverConfig.getParameter("nominalpower"));
		this.complexPowerMax = Integer.valueOf(driverConfig.getParameter("complexpowermax"));
		this.cosPhiMax = Double.valueOf(driverConfig.getParameter("cosphimax"));
		
		try {
			this.pastDaysPrediction = Integer.valueOf(driverConfig.getParameter("pastDaysPrediction"));
		}
		catch (Exception e) {
			this.pastDaysPrediction = 14;
			getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
		}
		
		try {
			this.pathToFiles = driverConfig.getParameter("pathToFiles");
			if (pathToFiles == null) throw new IllegalArgumentException();
		}
		catch (Exception e) {
			this.pathToFiles = "configfiles/pv/holl2013cleaned";
			getGlobalLogger().logWarning("Can't get pathToFiles, using the default value: " + this.pathToFiles);
		}
		
		try {
			this.fileExtension = driverConfig.getParameter("fileExtension");
			if (fileExtension == null) throw new IllegalArgumentException();
		}
		catch (Exception e) {
			this.fileExtension = ".csv";
			getGlobalLogger().logWarning("Can't get fileExtension, using the default value: " + this.fileExtension);
		}
		
		try {
			this.profileNominalPower = Double.valueOf(driverConfig.getParameter("profileNominalPower"));
		}
		catch (Exception e) {
			this.profileNominalPower = 5307.48;
			getGlobalLogger().logWarning("Can't get profileNominalPower, using the default value: " + this.profileNominalPower);
		}
		// load profile
		profile = new PvProfileHollSingleton(
				nominalPower, pathToFiles + fileExtension, profileNominalPower, this.cosPhiMax);
		
		//TODO adapt profile (power, heading), after making it singleton...
		
	}
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		super.onSimulationIsUp();
		//initially give LocalObserver load data of past days
		long startTime = getTimer().getUnixTimeAtStart();
		
		List<SparseLoadProfile> predicitons = new LinkedList<SparseLoadProfile>();

		int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(startTime);
		
		
		//starting in reverse so that the oldest profile is at index 0 in the list
		for (int i = pastDaysPrediction; i >= 1; i--) {
			
			int pastDay = Math.floorMod(dayOfYear - i, 365);
			predicitons.add(profile.getPowerForDay(pastDay));
		}
		
		PvPredictionExchange _ox = new PvPredictionExchange(this.getDeviceID(), getTimer().getUnixTime(), predicitons, pastDaysPrediction);
		this.notifyObserver(_ox);
	};


	@Override
	public void onNextTimeTick() {
		
		long now = getTimer().getUnixTime();
		
		if (this.pvSwitchedOn && now % 60 == 0) {
			
			this.setPower(
					Commodity.ACTIVEPOWER, 
					profile.getPowerAt(now));
			
			try {
				this.setPower(
						Commodity.REACTIVEPOWER, 
						(int) ComplexPowerUtil.convertActiveToReactivePower(
								getPower(Commodity.ACTIVEPOWER), 
								this.cosPhiMax, 
								true));
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		PvObserverExchange _ox = new PvObserverExchange(
				this.getDeviceID(), 
				now);
		_ox.setActivePower(this.getPower(Commodity.ACTIVEPOWER));
		_ox.setReactivePower(this.getPower(Commodity.REACTIVEPOWER));
		this.notifyObserver(_ox);
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {
		PvControllerExchange controllerExchange = (PvControllerExchange) controllerRequest;
		Boolean newPvSwitchedOn = controllerExchange.getNewPvSwitchedOn();
		
		// check whether to switch pv on or off
		if (newPvSwitchedOn != null) {
			this.pvSwitchedOn = newPvSwitchedOn;
			if (this.pvSwitchedOn == false) {
				this.setPower(Commodity.ACTIVEPOWER, 0);
				this.setPower(Commodity.REACTIVEPOWER, 0);
			}
		}
	}

	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
}
