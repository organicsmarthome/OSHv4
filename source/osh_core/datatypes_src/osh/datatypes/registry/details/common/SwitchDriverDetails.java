package osh.datatypes.registry.details.common;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class SwitchDriverDetails extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8607636973727654406L;
	protected boolean on;
	
	public SwitchDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	};
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( !(obj instanceof SwitchDriverDetails) )
			return false;
		SwitchDriverDetails other = (SwitchDriverDetails) obj;
		
		return (this.isOn() == other.isOn());
	}
	
	@Override
	public String toString() {
		return "Switch: " + (isOn()?"ON":"OFF");
	}
}
