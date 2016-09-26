package osh.hal.exchange;

import java.util.UUID;

import osh.cal.CALComExchange;


/**
 * 
 * @author Jan Mueller
 *
 */
public class GUIBatteryStorageComExchange extends CALComExchange {
	
	private double currentStateOfCharge;
	private double minStateOfCharge;
	private double maxStateOfCharge;

	private UUID batteryId;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 * @param currentStateOfCharge
	 * @param minStateOfCharge
	 * @param maxStateOfCharge
	 */
	public GUIBatteryStorageComExchange(
			UUID deviceID, 
			Long timestamp,
			double currentStateOfCharge, 
			double minStateOfCharge, 
			double maxStateOfCharge,
			UUID batteryId) {
		super(deviceID, timestamp);
		
		this.currentStateOfCharge = currentStateOfCharge;
		this.minStateOfCharge = minStateOfCharge;
		this.maxStateOfCharge = maxStateOfCharge;
		this.batteryId = batteryId;
	}

	public double getCurrentStateOfCharge() {
		return currentStateOfCharge;
	}

	public double getMinStateOfCharge() {
		return minStateOfCharge;
	}

	public double getMaxStateOfCharge() {
		return maxStateOfCharge;
	}

	public UUID getBatteryId() {
		return batteryId;
	}


	

}
