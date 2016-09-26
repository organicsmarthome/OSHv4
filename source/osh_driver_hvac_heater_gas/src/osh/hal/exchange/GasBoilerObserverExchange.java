package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class GasBoilerObserverExchange
					extends HALDeviceObserverExchange
					implements IHALElectricalPowerDetails, IHALHotWaterPowerDetails, IHALGasPowerDetails {
	
	private double minTemperature;
	private double maxTemperature;
	private double hotWaterTemperature;
	
	private boolean currentState;
	
	private int hotWaterPower;
	private int activePower;
	private int reactivePower;
	private int gasPower;
	
	private int maxHotWaterPower;
	private int maxGasPower;
	
	private int typicalActivePowerOn;
	private int typicalActivePowerOff;
	private int typicalReactivePowerOn;
	private int typicalReactivePowerOff;
	
	private int newIppAfter;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public GasBoilerObserverExchange(
			UUID deviceID, 
			Long timestamp,
			
			double minTemperature,
			double maxTemperature,
			double waterTemperature,
			
			boolean currentState,
			
			int activePower,
			int reactivePower,
			int gasPower,
			int hotWaterPower,
			int maxHotWaterPower,
			int maxGasPower,
			int typicalActivePowerOn,
			int typicalActivePowerOff,
			int typicalReactivePowerOn,
			int typicalReactivePowerOff,
			int newIppAfter) {
		
		super(deviceID, timestamp);
		
		this.minTemperature = minTemperature;
		this.maxTemperature = maxTemperature;
		this.hotWaterTemperature = waterTemperature;
		
		this.currentState = currentState;
		
		this.activePower = activePower;
		this.hotWaterPower = hotWaterPower;
		this.gasPower = gasPower;
		
		this.maxHotWaterPower = maxHotWaterPower;
		this.maxGasPower = maxGasPower;
		
		this.newIppAfter = newIppAfter;
		this.typicalActivePowerOn = typicalActivePowerOn;
		this.typicalActivePowerOff = typicalActivePowerOff;
		this.typicalReactivePowerOn = typicalReactivePowerOn;
		this.typicalReactivePowerOff = typicalReactivePowerOff;
	}

	public double getMinTemperature() {
		return minTemperature;
	}
	
	public double getMaxTemperature() {
		return maxTemperature;
	}
	
	public boolean getCurrentState() {
		return currentState;
	}

	@Override
	public int getHotWaterPower() {
		return hotWaterPower;
	}

	@Override
	public int getActivePower() {
		return activePower;
	}

	@Override
	public int getReactivePower() {
		return reactivePower;
	}

	@Override
	public int getGasPower() {
		return gasPower;
	}

	public int getMaxHotWaterPower() {
		return maxHotWaterPower;
	}

	public int getMaxGasPower() {
		return maxGasPower;
	}

	public int getTypicalActivePowerOn() {
		return typicalActivePowerOn;
	}

	public int getTypicalActivePowerOff() {
		return typicalActivePowerOff;
	}

	public int getTypicalReactivePowerOn() {
		return typicalReactivePowerOn;
	}

	public int getTypicalReactivePowerOff() {
		return typicalReactivePowerOff;
	}

	public int getNewIppAfter() {
		return newIppAfter;
	}

	@Override
	public double getHotWaterTemperature() {
		return hotWaterTemperature;
	}

}
