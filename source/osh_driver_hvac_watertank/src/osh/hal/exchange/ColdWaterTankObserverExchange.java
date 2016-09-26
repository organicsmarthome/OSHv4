package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ColdWaterTankObserverExchange
					extends HALDeviceObserverExchange {
	
	private double topTemperature;
	private double tankCapacity;
	
	private double coldWaterDemand;
	private double coldWaterSupply;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public ColdWaterTankObserverExchange(
			UUID deviceID, 
			Long timestamp, 
			double topTemperature,
			double tankCapacity,
			double hotWaterDemand,
			double hotWaterSupply) {
		super(deviceID, timestamp);
		
		this.topTemperature = topTemperature;
		this.tankCapacity = tankCapacity;
	}
	
	public double getTopTemperature() {
		return topTemperature;
	}
	
	public double getTankCapacity() {
		return tankCapacity;
	}
	
	public double getColdWaterDemand() {
		return coldWaterDemand;
	}
	
	public double getColdWaterSupply() {
		return coldWaterSupply;
	}

}
