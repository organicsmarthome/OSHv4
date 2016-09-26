package osh.datatypes.registry.commands;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Till Schuberth
 *
 */
public class SwitchCommandExchange extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4073123591294900927L;
	private boolean newstate;
	
	public SwitchCommandExchange(UUID sender, UUID receiver, long timestamp, boolean newstate) {
		super(sender, receiver, timestamp);
		this.newstate = newstate;
	}
	public boolean isNewstate() {
		return newstate;
	}	
	

}
