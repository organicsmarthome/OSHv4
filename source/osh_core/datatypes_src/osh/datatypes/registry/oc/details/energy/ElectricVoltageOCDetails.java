package osh.datatypes.registry.oc.details.energy;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ElectricVoltageOCDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4347656947206748157L;
	protected double voltage;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public ElectricVoltageOCDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}


	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

}
