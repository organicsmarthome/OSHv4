package osh.datatypes.registry.commands;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;


/**
 * Start device now
 * 
 * @author Kaibin Bao
 *
 */
public class StartDeviceRequest extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4843814099005430530L;

	public StartDeviceRequest(UUID sender, UUID receiver, long timestamp) {
		super(sender, receiver, timestamp);
	}

}
