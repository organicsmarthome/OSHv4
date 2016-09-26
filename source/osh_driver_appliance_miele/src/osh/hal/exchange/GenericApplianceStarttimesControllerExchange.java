package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALControllerExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class GenericApplianceStarttimesControllerExchange extends HALControllerExchange {

	private long startTime;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public GenericApplianceStarttimesControllerExchange(
			UUID deviceID, 
			Long timestamp,
			long startTime) {
		super(deviceID, timestamp);
		
		this.startTime = startTime;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
