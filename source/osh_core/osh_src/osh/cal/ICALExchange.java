package osh.cal;

import java.util.UUID;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public interface ICALExchange {
	
	public UUID getDeviceID();
	
	public Long getTimestamp();
}
