package osh.eal.hal.exchange;

import java.util.UUID;

/**
 * 
 * @author Florian Allerding
 *
 */
public abstract class HALControllerExchange extends HALExchange {
	public HALControllerExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
}
