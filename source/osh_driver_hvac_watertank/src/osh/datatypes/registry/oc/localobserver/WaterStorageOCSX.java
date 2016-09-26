package osh.datatypes.registry.oc.localobserver;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 *
 */
public class WaterStorageOCSX extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7810796095763895290L;
	private UUID tankId;
	private double currenttemp, mintemp, maxtemp, demand, supply;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param currenttemp
	 * @param mintemp
	 * @param maxtemp
	 */
	public WaterStorageOCSX(
			UUID sender, 
			long timestamp, 
			double currenttemp, 
			double mintemp, 
			double maxtemp,
			double demand,
			double supply,
			UUID tankId) {
		super(sender, timestamp);
		
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
	

	public boolean equalData(WaterStorageOCSX o) {
		if (o instanceof WaterStorageOCSX) {
			WaterStorageOCSX oex = (WaterStorageOCSX) o;
			
			//compare using an epsilon environment
			if (Math.abs(currenttemp - oex.currenttemp) < 0.001 &&
					Math.abs(mintemp - oex.mintemp) < 0.001 &&
					Math.abs(maxtemp - oex.maxtemp) < 0.001 &&
					Math.abs(demand - oex.demand) < 0.001 &&
					Math.abs(supply - oex.supply) < 0.001 
					&& ((tankId != null && tankId.equals(oex.tankId)) || (tankId == null && oex.tankId == null))
				)
				return true;
		}
		
		return false;
	}
}
