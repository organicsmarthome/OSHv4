package osh.datatypes.registry.oc.details.energy;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ElectricPowerOCDetails extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 827260439992008634L;

	public ElectricPowerOCDetails(UUID sender, long timestamp, int activePower,
			int reactivePower) {
		super(sender, timestamp);
		this.activePower = activePower;
		this.reactivePower = reactivePower;
	}

	protected int activePower;
	protected int reactivePower;
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public ElectricPowerOCDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public int getActivePower() {
		return activePower;
	}

	public void setActivePower(int activePower) {
		this.activePower = activePower;
	}

	
	public int getReactivePower() {
		return reactivePower;
	}

	public void setReactivePower(int reactivePower) {
		this.reactivePower = reactivePower;
	}

}
