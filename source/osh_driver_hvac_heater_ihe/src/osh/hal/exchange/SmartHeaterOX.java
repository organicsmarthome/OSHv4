package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SmartHeaterOX
					extends HALDeviceObserverExchange
					implements IHALElectricalPowerDetails, IHALHotWaterPowerDetails{
	
	private int temperatureSetting; 
	private int waterTemperature;
	
	private int currentState;
	
	private int hotWaterPower;
	private int activePower;
	private long[] timestampOfLastChangePerSubElement;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public SmartHeaterOX(
			UUID deviceID, 
			Long timestamp,
			
			int temperatureSetting, 
			int waterTemperature,
			
			int currentState,
			
			int activePower,
			int hotWaterPower,
			long[] timestampOfLastChangePerSubElement) {
		
		super(deviceID, timestamp);
		
		this.temperatureSetting = temperatureSetting;
		this.currentState = currentState;
		
		this.hotWaterPower = hotWaterPower;
		this.activePower = activePower;
		this.timestampOfLastChangePerSubElement = timestampOfLastChangePerSubElement;
	}

	public int getTemperatureSetting() {
		return temperatureSetting;
	}
	
	public int getCurrentState() {
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
		return 0;
	}

	public long[] getTimestampOfLastChangePerSubElement() {
		long[] clone = new long[timestampOfLastChangePerSubElement.length];
		for (int i = 0; i < timestampOfLastChangePerSubElement.length; i++) {
			clone[i] = timestampOfLastChangePerSubElement[i];
		}
		return clone;
	}

	@Override
	public double getHotWaterTemperature() {
		return waterTemperature;
	}

}
