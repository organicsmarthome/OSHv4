package osh.cal;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser, Till Schuberth
 *
 */
public abstract class CALComExchange extends CALExchange {
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public CALComExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	
}
