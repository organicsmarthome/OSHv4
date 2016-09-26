package osh.hal.exchange;

import java.util.UUID;

import osh.cal.CALComExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class GUIWaterStorageComExchange extends CALComExchange {
	
	private double currenttemp;
	private double mintemp;
	private double maxtemp;
	private double demand;
	private double supply;
	private UUID tankId;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 * @param currenttemp
	 * @param mintemp
	 * @param maxtemp
	 */
	public GUIWaterStorageComExchange(
			UUID deviceID, 
			Long timestamp,
			double currenttemp, 
			double mintemp, 
			double maxtemp,
			double demand,
			double supply,
			UUID tankId) {
		super(deviceID, timestamp);
		
		this.currenttemp = currenttemp;
		this.mintemp = mintemp;
		this.maxtemp = maxtemp;
		this.demand = demand;
		this.supply = supply;
		this.tankId = tankId;
	}


	public double getCurrenttemp() {
		return currenttemp;
	}


	public double getMintemp() {
		return mintemp;
	}


	public double getMaxtemp() {
		return maxtemp;
	}
	
	public double getDemand() {
		return demand;
	}


	public double getSupply() {
		return supply;
	}
	
	public UUID getTankId() {
		return tankId;
	}

}
