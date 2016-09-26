package osh.datatypes.registry.oc.localobserver;

import java.util.UUID;

//import osh.datatypes.energy.INeededEnergy;
import osh.datatypes.registry.StateExchange;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class BatteryStorageOCSX extends StateExchange {
	
	private static final long serialVersionUID = -8893900933803816383L;
	private UUID batteryId;
	private double stateOfCharge, minStateOfCharge, maxStateOfCharge;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public BatteryStorageOCSX(
			UUID sender, 
			long timestamp, 
			double stateOfCharge, 
			double minStateOfCharge, 
			double maxStateOfCharge,
			UUID batteryId) {
		super(sender, timestamp);
		
		this.stateOfCharge = stateOfCharge;
		this.minStateOfCharge = minStateOfCharge;
		this.maxStateOfCharge = maxStateOfCharge;
		this.batteryId = batteryId;
	}

	
	public UUID getBatteryId() {
		return batteryId;
	}

	public double getStateOfCharge() {
		return stateOfCharge;
	}

	public double getMinStateOfCharge() {
		return minStateOfCharge;
	}

	public double getMaxStateOfCharge() {
		return maxStateOfCharge;
	}

	public boolean equalData(BatteryStorageOCSX o) {
		if (o instanceof BatteryStorageOCSX) {
			BatteryStorageOCSX oex = (BatteryStorageOCSX) o;
			//compare using an epsilon environment
			if (Math.abs(stateOfCharge - oex.stateOfCharge) < 0.001 &&
					Math.abs(minStateOfCharge - oex.minStateOfCharge) < 0.001 &&
					Math.abs(maxStateOfCharge - oex.maxStateOfCharge) < 0.001 
					&& ((batteryId != null && batteryId.equals(oex.batteryId)) || (batteryId == null && oex.batteryId == null))
				)
				return true;
		}
		return false;
	}
	
}
