package osh.datatypes.registry.commands;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;


/**
 * Stop device now
 * 
 * @author Kaibin Bao
 *
 */
public class StopDeviceRequest extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -465373952181618356L;

	public StopDeviceRequest(UUID sender, UUID receiver, long timestamp) {
		super(sender, receiver, timestamp);
	}

}
