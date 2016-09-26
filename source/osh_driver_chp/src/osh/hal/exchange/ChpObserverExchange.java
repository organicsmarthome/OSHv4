package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;
import osh.hal.interfaces.chp.IHALChpDetails;
import osh.hal.interfaces.chp.IHALExtendedChpDetails;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ChpObserverExchange 
				extends HALDeviceObserverExchange
				implements 	IHALChpDetails,
							IHALExtendedChpDetails,							
							IHALElectricalPowerDetails,
							IHALHotWaterPowerDetails,
							IHALGasPowerDetails {
	
	// ### IHALChpDetails ###
	private boolean running;
	private boolean heatingRequest;
	private boolean electricityRequest;
	private int minRuntimeRemaining;
	private int minRuntime;
	
	// ### IHALExtendedChpDetails ###
	private double temperatureIn;
	private double temperatureOut;
	
	// ### IHALElectricPowerDetails ###
	private int activePower;
	private int reactivePower;
	
	// ### IHALThermalPowerDetails ###
	private int thermalPower;
	
	// ### IHALGasPowerDetails ###
	private int gasPower;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public ChpObserverExchange(
			UUID deviceID, 
			Long timestamp) {
		super(deviceID, timestamp);
	}

	@Override
	public boolean isHeatingRequest() {
		return heatingRequest;
	}

	public void setHeatingRequest(boolean heatingRequest) {
		this.heatingRequest = heatingRequest;
	}

	@Override
	public boolean isElectricityRequest() {
		return electricityRequest;
	}

	public void setElectricityRequest(boolean electricityRequest) {
		this.electricityRequest = electricityRequest;
	}

	@Override
	public int getMinRuntimeRemaining() {
		return minRuntimeRemaining;
	}

	public void setMinRuntimeRemaining(int minRuntimeRemaining) {
		this.minRuntimeRemaining = minRuntimeRemaining;
	}

	public int getMinRuntime() {
		return minRuntime;
	}
	
	public void setMinRuntime(int minRuntime) {
		this.minRuntimeRemaining = minRuntime;
	}
	
	@Override
	public int getActivePower() {
		return activePower;
	}

	public void setActivePower(int activePower) {
		this.activePower = activePower;
	}

	@Override
	public int getReactivePower() {
		return reactivePower;
	}

	public void setReactivePower(int reactivePower) {
		this.reactivePower = reactivePower;
	}

	@Override
	public int getHotWaterPower() {
		return thermalPower;
	}

	public void setThermalPower(int thermalPower) {
		this.thermalPower = thermalPower;
	}

	@Override
	public double getTemperatureIn() {
		return temperatureIn;
	}

	public void setTemperatureIn(double temperatureIn) {
		this.temperatureIn = temperatureIn;
	}

	@Override
	public double getTemperatureOut() {
		return temperatureOut;
	}

	public void setTemperatureOut(double temperatureOut) {
		this.temperatureOut = temperatureOut;
	}	
	
	public int getGasPower() {
		return gasPower;
	}
	
	public void setGasPower(int gasPower) {
		this.gasPower = gasPower;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isRunning() {
		return running;
	}

	@Override
	public double getHotWaterTemperature() {
		return temperatureOut;
	}
}
