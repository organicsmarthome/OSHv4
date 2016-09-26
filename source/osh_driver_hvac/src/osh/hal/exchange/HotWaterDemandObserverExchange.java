package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class HotWaterDemandObserverExchange extends HALDeviceObserverExchange {

	private int power;
	

	public HotWaterDemandObserverExchange(
			UUID deviceID, 
			Long timestamp, 
			int power) {
		super(deviceID, timestamp);

		this.power = power;
	}

	public int getHotWaterPower() {
		return power;
	}
}
