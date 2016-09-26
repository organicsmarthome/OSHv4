package osh.driver.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.pv.PvProfile;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
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
public class PvSimulationDriverEv0 extends DeviceSimulationDriver {
	
	private boolean pvSwitchedOn;
	private int reactivePowerTarget;
	
	private int pastDaysPrediction;
	
	/** nominal power of PV (e.g. 4.6kWpeak) */
	private int nominalPower;
	/** according to inverter (technical, due to VAmax) */
	private int complexPowerMax;
	@SuppressWarnings("unused")
	private int reactivePowerMax;
	/** according to inverter (technical, due to cosPhi: e.g. 0.8ind...0.8cap) */
	private double cosPhiMax;

	private PvProfile profile;

	/**
	 * CONSTRUCTOR
	 * @throws Exception
	 */
	public PvSimulationDriverEv0(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws Exception {
		super(controllerbox, deviceID, driverConfig);
		
		this.pvSwitchedOn = true;
		this.reactivePowerTarget = 0;
		
		String profileSourceName = driverConfig.getParameter("profilesource");
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
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}
		
		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
		
		this.profile = new PvProfile(profileSourceName, this.nominalPower);
		this.reactivePowerMax = 
				(int) ComplexPowerUtil.convertComplexToReactivePower(
						this.complexPowerMax, this.cosPhiMax, true);
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
				this.reactivePowerTarget = 0;
			}
		}
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
			SparseLoadProfile prof = profile.getProfileForDayOfYear(pastDay).getProfileWithoutDuplicateValues();
			try {
				Long nextLoadChange = 0L;
				while (nextLoadChange != null) {
					int firstLoad = prof.getLoadAt(Commodity.ACTIVEPOWER, nextLoadChange);
					double newCosPhi;
					int reactivePower = 0;
					try {
						newCosPhi = ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(
								firstLoad, this.reactivePowerTarget);
						
						if (newCosPhi > this.cosPhiMax) {
							reactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(
									firstLoad, 
									this.cosPhiMax, 
									(this.reactivePowerTarget >= 0));
						}
					} catch (Exception e) {
						
					}
					
					prof.setLoad(Commodity.REACTIVEPOWER, nextLoadChange, reactivePower);
					nextLoadChange = prof.getNextLoadChange(Commodity.ACTIVEPOWER, nextLoadChange);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
			predicitons.add(prof);
		}
		
		PvPredictionExchange _ox = new PvPredictionExchange(this.getDeviceID(), getTimer().getUnixTime(), predicitons, pastDaysPrediction);
		this.notifyObserver(_ox);
	};

	@Override
	public void onNextTimeTick() {
		if (this.pvSwitchedOn) {
			long now = getTimer().getUnixTime();
			this.setPower(Commodity.ACTIVEPOWER, profile.getPowerAt(now));
			
			if (getPower(Commodity.ACTIVEPOWER) != 0) {
				double newCosPhi;
				try {
					newCosPhi = ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(
							getPower(Commodity.ACTIVEPOWER), this.reactivePowerTarget);
					
					if (newCosPhi > this.cosPhiMax) {
						this.setPower(Commodity.REACTIVEPOWER, (int) ComplexPowerUtil.convertActiveToReactivePower(
								getPower(Commodity.ACTIVEPOWER), 
								this.cosPhiMax, 
								(this.reactivePowerTarget >= 0)));
					}
					else if (newCosPhi < -1) {
						newCosPhi = -1;
						this.setPower(Commodity.REACTIVEPOWER, 0);
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				this.setPower(Commodity.REACTIVEPOWER, 0);
			}
		}
		
		PvObserverExchange _ox = new PvObserverExchange(this.getDeviceID(), getTimer().getUnixTime());
		_ox.setActivePower(this.getPower(Commodity.ACTIVEPOWER));
		_ox.setReactivePower(this.getPower(Commodity.REACTIVEPOWER));
		this.notifyObserver(_ox);
	}

	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
}
