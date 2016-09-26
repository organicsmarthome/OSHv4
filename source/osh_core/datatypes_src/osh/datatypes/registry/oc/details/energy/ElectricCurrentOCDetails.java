package osh.datatypes.registry.oc.details.energy;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ElectricCurrentOCDetails extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3976808627342188832L;
	private double current;

	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public ElectricCurrentOCDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
	}

}
