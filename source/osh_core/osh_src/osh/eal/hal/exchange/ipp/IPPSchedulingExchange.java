package osh.eal.hal.exchange.ipp;

import java.util.UUID;

import osh.eal.hal.exchange.HALObserverExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class IPPSchedulingExchange 
				extends HALObserverExchange {
	
	private long newIppAfter;
	private long rescheduleAfter;
	private double triggerIfDeltaX;
	
	public IPPSchedulingExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}

	public long getNewIppAfter() {
		return newIppAfter;
	}

	public void setNewIppAfter(long newIppAfter) {
		this.newIppAfter = newIppAfter;
	}

	public long getRescheduleAfter() {
		return rescheduleAfter;
	}

	public void setRescheduleAfter(long rescheduleAfter) {
		this.rescheduleAfter = rescheduleAfter;
	}

	public double getTriggerIfDeltaX() {
		return triggerIfDeltaX;
	}

	public void setTriggerIfDeltaX(double triggerIfDeltaX) {
		this.triggerIfDeltaX = triggerIfDeltaX;
	}
	

}
