package osh.datatypes.registry.commands;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ChpElectricityRequest extends CommandExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2650396121767761563L;
	private boolean on;
	

	public ChpElectricityRequest(
			UUID sender, 
			UUID receiver, 
			long timestamp,
			boolean on) {
		super(sender, receiver, timestamp);
	
		this.on = on;
	}


	public boolean isOn() {
		return on;
	}
	
	

}
