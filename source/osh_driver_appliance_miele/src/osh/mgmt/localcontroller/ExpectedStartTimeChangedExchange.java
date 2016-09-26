package osh.mgmt.localcontroller;

import java.util.UUID;

import osh.datatypes.registry.EventExchange;


/**
 * 
 * @author Kaibin Bao
 *
 */
public class ExpectedStartTimeChangedExchange extends EventExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 806696758718863420L;
	private long expectedStartTime;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public ExpectedStartTimeChangedExchange(UUID sender, long timestamp, long expectedStartTime) {
		super(sender, timestamp);
		this.expectedStartTime = expectedStartTime;
	}

	public long getExpectedStartTime() {
		return expectedStartTime;
	}

}
