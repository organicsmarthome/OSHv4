package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.gasboiler.GasBoilerModel;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.GasBoilerObserverExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class GasBoilerSimulationDriver extends DeviceSimulationDriver {
	
	private double minTemperature;
	private double maxTemperature;
	private int maxHotWaterPower;
	private int maxGasPower;
	
	private int typicalActivePowerOn;
	private int typicalActivePowerOff;
	private int typicalReactivePowerOn;
	private int typicalReactivePowerOff;
	
	private GasBoilerModel model;
	
	private int newIppAfter;

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public GasBoilerSimulationDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		try {
			this.minTemperature = Double.valueOf(driverConfig.getParameter("minTemperature"));
		}
		catch (Exception e) {
			this.minTemperature = 60;
			getGlobalLogger().logWarning("Can't get minTemperature, using the default value: " + this.minTemperature);
		}
		
		try {
			this.maxTemperature = Double.valueOf(driverConfig.getParameter("maxTemperature"));
		}
		catch (Exception e) {
			this.maxTemperature = 80;
			getGlobalLogger().logWarning("Can't get maxTemperature, using the default value: " + this.maxTemperature);
		}
		
		try {
			this.maxHotWaterPower = Integer.valueOf(driverConfig.getParameter("maxHotWaterPower"));
		}
		catch (Exception e) {
			this.maxHotWaterPower = 15000;
			getGlobalLogger().logWarning("Can't get maxHotWaterPower, using the default value: " + this.maxHotWaterPower);
		}
		
		try {
			this.maxGasPower = Integer.valueOf(driverConfig.getParameter("maxGasPower"));
		}
		catch (Exception e) {
			this.maxGasPower = 15000;
			getGlobalLogger().logWarning("Can't get maxGasPower, using the default value: " + this.maxGasPower);
		}
		
		try {
			this.typicalActivePowerOn = Integer.valueOf(driverConfig.getParameter("typicalActivePowerOn"));
		}
		catch (Exception e) {
			this.typicalActivePowerOn = 100;
			getGlobalLogger().logWarning("Can't get typicalActivePowerOn, using the default value: " + this.typicalActivePowerOn);
		}
		
		try {
			this.typicalActivePowerOff = Integer.valueOf(driverConfig.getParameter("typicalActivePowerOff"));
		}
		catch (Exception e) {
			this.typicalActivePowerOff = 0;
			getGlobalLogger().logWarning("Can't get typicalActivePowerOff, using the default value: " + this.typicalActivePowerOff);
		}
		
		try {
			this.typicalReactivePowerOn = Integer.valueOf(driverConfig.getParameter("typicalReactivePowerOn"));
		}
		catch (Exception e) {
			this.typicalReactivePowerOn = 0;
			getGlobalLogger().logWarning("Can't get typicalReactivePowerOn, using the default value: " + this.typicalReactivePowerOn);
		}
		
		try {
			this.typicalReactivePowerOff = Integer.valueOf(driverConfig.getParameter("typicalReactivePowerOff"));
		}
		catch (Exception e) {
			this.typicalReactivePowerOff = 0;
			getGlobalLogger().logWarning("Can't get typicalReactivePowerOff, using the default value: " + this.typicalReactivePowerOff);
		}
		
		try {
			this.newIppAfter = Integer.valueOf(driverConfig.getParameter("newIppAfter"));
		}
		catch (Exception e) {
			this.newIppAfter = 1 * 3600; //1 hour
			getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
		}
		
		this.model = new GasBoilerModel(maxHotWaterPower, maxGasPower, typicalActivePowerOn, typicalActivePowerOff,
				typicalReactivePowerOn, typicalReactivePowerOff, false);
	}
	
//	Nothing to do for now
//	@Override
//	public void onSimulationIsUp() throws SimulationSubjectException {
//		super.onSimulationIsUp();
//	}

	@Override
	public void onNextTimeTick() {
		long now = getTimer().getUnixTime();
		
		// LOGIC
		double waterTemperature = commodityInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
		
		if (waterTemperature < minTemperature) {
			this.model.switchOn();
		}
		else if (waterTemperature > maxTemperature) {
			this.model.switchOff();
		}
		
		int activePower = this.model.getActivePower();
		int reactivePower = this.model.getReactivePower();
		int gasPower = this.model.getGasPower();
		int hotWaterPower = -this.model.getHotWaterPower();
		
		setPower(Commodity.ACTIVEPOWER, activePower);
		setPower(Commodity.REACTIVEPOWER, reactivePower);
		setPower(Commodity.NATURALGASPOWER, gasPower);
		setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
		
		GasBoilerObserverExchange ox = new GasBoilerObserverExchange(
				getDeviceID(), 
				now,
				minTemperature,
				maxTemperature,
				waterTemperature,
				model.isOn(),
				activePower,
				reactivePower,
				gasPower,
				hotWaterPower,
				maxHotWaterPower,
				maxGasPower,
				typicalActivePowerOn,
				typicalActivePowerOff,
				typicalReactivePowerOn,
				typicalReactivePowerOff,
				newIppAfter);
		this.notifyObserver(ox);
	}
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
}
