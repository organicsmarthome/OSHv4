package osh.driver.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.BaseloadObserverExchange;
import osh.hal.exchange.BaseloadPredictionExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.slp.IH0Profile;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser, Florian Allerding
 *
 */
public class BaseloadSimulationDriver extends DeviceSimulationDriver {
	
	private IH0Profile baseload;
	
	private double cosPhi;
	private boolean isInductive;
	
	private int pastDaysPrediction;
	private float weightForOtherWeekday;
	private float weightForSameWeekday;
	
	private double sumActivePower = 0.0;
	private double sumReactivePower = 0.0;
	
	/**
	 * CONSTRUCTOR
	 *
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BaseloadSimulationDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);

		int yearlyElectricityConsumptionOfHousehold;
		String h0ProfileFileName, h0ClassName;		
		try {
			yearlyElectricityConsumptionOfHousehold = Integer.valueOf(driverConfig.getParameter("baseloadyearlyconsumption"));
		}
		catch (Exception e) {
			yearlyElectricityConsumptionOfHousehold = 1000;
			getGlobalLogger().logWarning("Can't get yearlyElectricityConsumptionOfHousehold, using the default value: " + yearlyElectricityConsumptionOfHousehold);
		}
		
		try {
			h0ProfileFileName = getDriverConfig().getParameter("h0filename");
			if (h0ProfileFileName == null)
				throw new IllegalArgumentException();
		}
		catch (Exception e) {
			h0ProfileFileName = "configfiles/h0/H0ProfileNew.csv";
			getGlobalLogger().logWarning("Can't get h0ProfileFileName, using the default value: " + h0ProfileFileName);
		}
		
		try {
			h0ClassName = getDriverConfig().getParameter("h0classname");
			if (h0ClassName == null)
				throw new IllegalArgumentException();
		}
		catch (Exception e) {
			h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
			getGlobalLogger().logWarning("Can't get h0ClassName, using the default value: " + h0ClassName);
		}	
		
		this.cosPhi = Double.valueOf(driverConfig.getParameter("baseloadcosphi"));
		this.isInductive = Boolean.valueOf(driverConfig.getParameter("baseloadisinductive"));
		
		try {
			this.pastDaysPrediction = Integer.valueOf(driverConfig.getParameter("pastDaysPrediction"));
		}
		catch (Exception e) {
			this.pastDaysPrediction = 14;
			getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
		}
		
		try {
			this.weightForOtherWeekday = Float.valueOf(driverConfig.getParameter("weightForOtherWeekday"));
		}
		catch (Exception e) {
			this.weightForOtherWeekday = 1.0f;
			getGlobalLogger().logWarning("Can't get weightForOtherWeekday, using the default value: " + this.weightForOtherWeekday);
		}
		
		try {
			this.weightForSameWeekday = Float.valueOf(driverConfig.getParameter("weightForSameWeekday"));
		}
		catch (Exception e) {
			this.weightForSameWeekday = 5.0f;
			getGlobalLogger().logWarning("Can't get weightForSameWeekday, using the default value: " + this.weightForSameWeekday);
		}
		
		try {
			Class h0Class = Class.forName(h0ClassName);
			
			this.baseload = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
				.newInstance(TimeConversion.convertUnixTime2Year(getTimer().getUnixTime()),
						h0ProfileFileName,
						yearlyElectricityConsumptionOfHousehold);
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		//initially give LocalObserver load data of past days
		long startTime = getTimer().getUnixTimeAtStart();
		
		
		
		List<SparseLoadProfile> predictions = new LinkedList<SparseLoadProfile>();
		
		//starting in reverse so that the oldest profile is at index 0 in the list
		for (int i = pastDaysPrediction; i >= 1; i--) {
			SparseLoadProfile dayProfile = new SparseLoadProfile();
			int dayCorrection = (int) Math.abs((startTime / 86400 - i) % 365) * 86400;
			
			for (int sec = 0; sec < 86400; sec++) {
				int activeLoad = baseload.getActivePowerAt(dayCorrection + sec);
				
				dayProfile.setLoad(Commodity.ACTIVEPOWER, sec, activeLoad);
				
				try {
					dayProfile.setLoad(
							Commodity.REACTIVEPOWER, sec, 
							(int) ComplexPowerUtil.convertActiveToReactivePower(
									activeLoad, 
									this.cosPhi, 
									this.isInductive));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			dayProfile.setEndingTimeOfProfile(86400);
			predictions.add(dayProfile.getProfileWithoutDuplicateValues());
		}
		
		BaseloadPredictionExchange _ox = new BaseloadPredictionExchange(
				this.getDeviceID(), getTimer().getUnixTime(), 
				predictions,
				pastDaysPrediction, weightForOtherWeekday, weightForSameWeekday);
		this.notifyObserver(_ox);
	};
	
	@Override
	public void onNextTimeTick() {
		this.setPower(
				Commodity.ACTIVEPOWER, 
				baseload.getActivePowerAt(getTimer().getUnixTime()));
		try {
			this.setPower(
					Commodity.REACTIVEPOWER,
					(int) ComplexPowerUtil.convertActiveToReactivePower(
							this.getPower(Commodity.ACTIVEPOWER), this.cosPhi, this.isInductive));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		BaseloadObserverExchange blox = 
				new BaseloadObserverExchange(
						getDeviceID(), 
						getTimer().getUnixTime());
		
		blox.setPower(Commodity.ACTIVEPOWER, this.getPower(Commodity.ACTIVEPOWER));
		blox.setPower(Commodity.REACTIVEPOWER, this.getPower(Commodity.REACTIVEPOWER));
		
		this.sumActivePower += (double) this.getPower(Commodity.ACTIVEPOWER);
		this.sumReactivePower += (double) this.getPower(Commodity.REACTIVEPOWER);
		
		this.notifyObserver(blox);
	}
	

	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();
		
		if (getOSH().getOSHstatus().isSimulation()) {			
			if (DatabaseLoggerThread.isLogDevices()) {
				DatabaseLoggerThread.enqueueBaseload(sumActivePower / 3600000.0, sumReactivePower  / 3600000.0);
			}
		}
	}

}
