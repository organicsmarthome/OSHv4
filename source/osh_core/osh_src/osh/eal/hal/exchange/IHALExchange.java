package osh.eal.hal.exchange;

import java.util.UUID;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public interface IHALExchange {
	
	public UUID getDeviceID();
	
	public Long getTimestamp();
}
