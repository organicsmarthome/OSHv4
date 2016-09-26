package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.chiller.AdsorptionChillerModel;
import osh.driver.simulation.spacecooling.HollOutdoorTemperatures;
import osh.driver.simulation.spacecooling.OutdoorTemperatures;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;
import osh.hal.exchange.ChillerControllerExchange;
import osh.hal.exchange.ChillerObserverExchange;
import osh.registry.interfaces.IHasState;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class AdsorptionChillerSimulationDriver 
					extends DeviceSimulationDriver
					implements IHasState {

	//Constants
	/** [W] */
	private final int typicalStandyEnergyConsumption = 10;
	/** [W] */
	private final int typicalRunningActivePowerConsumption = 420;
	/** [degree Celsius] */
	private final double minTemperatureIN = 60.0;
	/** [degree Celsius] */
	private final double maxTemperatureIN = 80.0;
	
	//GET SIMULATED OUTDOOR TEMPERATURES
//	HollOutdoorTemperatureAugust2015 outdoorTemperature = new HollOutdoorTemperatureAugust2015();
	OutdoorTemperatures outdoorTemperature = new HollOutdoorTemperatures(getGlobalLogger());
	
	//Variables
	private boolean runningRequestFromController = false;
	private boolean running = false;
	/** [0..1] */
	private double currentCop = 1; // just default...
	/** [W] */
	private int currentCoolingPower = 0; // just default...
	private int currentHotWaterPower = 0;
	/** RANGE: [22-37]! */
	private double currentOutdoorTemperature = 35.0;
	
	// received from other devices...
	private double observedWaterTemperature = Double.MIN_VALUE;
	
	/**
	 * CONSTRUCTOR
	 */
	public AdsorptionChillerSimulationDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// NOTHING
	}

	@Override
	public void onNextTimeTick() {

		// simulate device
		if (runningRequestFromController) {
			// check whether to switch on or off
			if (observedWaterTemperature <= maxTemperatureIN
					&& observedWaterTemperature >= minTemperatureIN) {
				if ( !running ) {
					doSwitchOnLogic();
				}
			}
			else {
				if ( running ) {
					doSwitchOffLogic();
				}
			}
		}
		else {
			if ( running ) {
				doSwitchOffLogic();
			}
		}
		
		doLogic();
		
		// notify Observer about current status
		ChillerObserverExchange ox = new ChillerObserverExchange(
				getDeviceID(), 
				getTimer().getUnixTime(),
				running,
				outdoorTemperature);
		ox.setColdWaterPower(currentCoolingPower);
		ox.setHotWaterPower(currentHotWaterPower);
		ox.setActivePower(running ? typicalRunningActivePowerConsumption : typicalStandyEnergyConsumption);
//		ox.setReactivePower(reactivePower);
		this.notifyObserver(ox);
	}
	
	
	private void doLogic() {
		
		if(running) {
			// get outdoor temperature
			this.currentOutdoorTemperature = outdoorTemperature.getTemperature(getTimer().getUnixTime());
			
			// CALCULATE COP AND DYNAMIC COOLING POWER
			this.currentCoolingPower = AdsorptionChillerModel.chilledWaterPower(observedWaterTemperature, currentOutdoorTemperature);
			this.currentCop = AdsorptionChillerModel.cop(observedWaterTemperature, currentOutdoorTemperature);
			this.currentHotWaterPower = (int) -Math.round(currentCoolingPower / currentCop);
			
			//DEBUG
//			getGlobalLogger().logDebug("currentOutdoorTemperature: " + currentOutdoorTemperature);
//			getGlobalLogger().logDebug("observedWaterTemperature: " + observedWaterTemperature);
//			getGlobalLogger().logDebug("currentCoolingPower: " + currentCoolingPower);
//			getGlobalLogger().logDebug("currentCop: " + c  urrentCop);
			if (getTimer().getUnixTime() % 3600 == 0) {
				getGlobalLogger().logDebug(
						"outdoorTemperature: " + currentOutdoorTemperature 
						+ " | hotwaterdemand: " + (int) ( (-1) * (currentCoolingPower / currentCop))
						+ " | coldwaterdemand: " + currentCoolingPower
						+ " | cop: " + currentCop);
			}
			
			// it is on...
			this.setPower(Commodity.ACTIVEPOWER, typicalRunningActivePowerConsumption);
			this.setPower(Commodity.COLDWATERPOWER, currentCoolingPower);
			this.setPower(Commodity.HEATINGHOTWATERPOWER, currentHotWaterPower);
		}
		else {
			// reset values
			this.currentCoolingPower = 0;
			this.currentCop = 1;
			this.currentHotWaterPower = 0;
			
			// it is off...
			this.setPower(Commodity.ACTIVEPOWER, typicalStandyEnergyConsumption);
			this.setPower(Commodity.COLDWATERPOWER, currentCoolingPower);
			this.setPower(Commodity.HEATINGHOTWATERPOWER, currentHotWaterPower);
		}
	}
	
	
	private void doSwitchOnLogic() {
		if (running) {
			//should not happen
			getGlobalLogger().logError("BAD!");
		}
		else {
			running = true;
		}
	}
	
	
	private void doSwitchOffLogic() {
		if (running) {
			running = false;
		}
		else {
			//should not happen
			getGlobalLogger().logError("BAD!");
		}
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest)
			throws HALException {
		super.onControllerRequest(controllerRequest);
		
		ChillerControllerExchange cx = (ChillerControllerExchange) controllerRequest;
		boolean stop = cx.isStopGenerationFlag();
		boolean cr = cx.isCoolingRequest();
		
		if ( (cr) && !stop) {
			runningRequestFromController = true;
			//getGlobalLogger().logDebug("cooling request from controller");
		}
		else if (stop) {
			runningRequestFromController = false;
			//getGlobalLogger().logDebug("got stop request");
		}
		else  {
			runningRequestFromController = false;
			//getGlobalLogger().logDebug("no cooling request from controller");
		}
	}
	
	@Override
	public void setCommodityInputStates(
			LimitedCommodityStateMap inputStates,
			AncillaryMeterState ancillaryMeterState) throws EnergySimulationException {
		
		super.setCommodityInputStates(inputStates, ancillaryMeterState);
		if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
			this.observedWaterTemperature = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
		}
	}


	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
