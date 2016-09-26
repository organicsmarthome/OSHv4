package osh.datatypes.registry.oc.details;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class SwitchOCDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9070474430421146195L;
	private boolean on;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public SwitchOCDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	@Override
	public String toString() {
		return "Switch: " + (isOn()?"ON":"OFF");
	}
}
