package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class HotWaterTankObserverExchange extends HALDeviceObserverExchange {

	private double topTemperature;

	private double tankCapacity;
	private double tankDiameter;
	private double ambientTemperature;
	
	private double hotWaterDemand;
	private double hotWaterSupply;

	
	/**
	 * CONSTRUCTOR
	 * 
	 * @param deviceID
	 * @param timestamp
	 */
	public HotWaterTankObserverExchange(
			UUID deviceID, 
			Long timestamp,
			double topTemperature, 
			double tankCapacity, 
			double tankDiameter,
			double ambientTemperature,
			double hotWaterDemand,
			double hotWaterSupply) {
		super(deviceID, timestamp);

		this.topTemperature = topTemperature;
		this.tankCapacity = tankCapacity;
		this.tankDiameter = tankDiameter;
		this.ambientTemperature = ambientTemperature;
		this.hotWaterDemand = hotWaterDemand;
		this.hotWaterSupply = hotWaterSupply;
	}

	public double getTopTemperature() {
		return topTemperature;
	}

	public double getTankCapacity() {
		return tankCapacity;
	}

	public double getAmbientTemperature() {
		return ambientTemperature;
	}
	
	public double getTankDiameter() {
		return tankDiameter;
	}
	
	public double getHotWaterDemand() {
		return hotWaterDemand;
	}
	
	public double getHotWaterSupply() {
		return hotWaterSupply;
	}
}
