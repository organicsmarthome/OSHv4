package osh.datatypes.registry;

import java.util.UUID;

/**
 * 
 * @author Simone Droll
 *
 */

public class BuildingStateExchange extends StateExchange {

	private static final long serialVersionUID = -6624430081473462447L;

	long currentTick;
	private long timestamp;
	double currentActivePower;
	double currentActivePowerConsumption;
	double currentActivePowerChp;
	double currentActivePowerChpFeedIn;
	double currentActivePowerChpAutoConsumption;
	double currentActivePowerPv;
	double currentActivePowerPvFeedIn;
	double currentActivePowerPvAutoConsumption;
	double currentActivePowerBatteryCharging;
	double currentActivePowerBatteryDischarging;
	double currentActivePowerBatteryAutoConsumption;
	double currentActivePowerBatteryFeedIn;
	double currentActivePowerExternal;
	double currentReactivePowerExternal;
	double currentGasPowerExternal;

	public BuildingStateExchange() {
	}
	
	
	public BuildingStateExchange(BuildingStateExchange other) {
		super(other.sender, other.timestamp);


		this.currentTick = other.currentTick;
		this.currentActivePower = other.currentActivePower;
		this.currentActivePowerConsumption = other.currentActivePowerConsumption;
		this.currentActivePowerChp = other.currentActivePowerChp;
		this.currentActivePowerChpFeedIn = other.currentActivePowerChpFeedIn;
		this.currentActivePowerChpAutoConsumption = other.currentActivePowerChpAutoConsumption;
		this.currentActivePowerPv = other.currentActivePowerPv;
		this.currentActivePowerPvFeedIn = other.currentActivePowerPvFeedIn;
		this.currentActivePowerPvAutoConsumption = other.currentActivePowerPvAutoConsumption;
		this.currentActivePowerBatteryCharging = other.currentActivePowerBatteryCharging;
		this.currentActivePowerBatteryDischarging = other.currentActivePowerBatteryDischarging;
		this.currentActivePowerBatteryAutoConsumption = other.currentActivePowerBatteryAutoConsumption;
		this.currentActivePowerBatteryFeedIn = other.currentActivePowerBatteryFeedIn;
		this.currentActivePowerExternal = other.currentActivePowerExternal;
		this.currentReactivePowerExternal = other.currentReactivePowerExternal;
		this.currentGasPowerExternal = other.currentGasPowerExternal;
	}

	public BuildingStateExchange(UUID sender, long currentTick, long timestamp, double currentActivePower,
			double currentActivePowerConsumption, double currentActivePowerChp, double currentActivePowerChpFeedIn,
			double currentActivePowerChpAutoConsumption, double currentActivePowerPv, double currentActivePowerPvFeedIn,
			double currentActivePowerPvAutoConsumption, double currentActivePowerBatteryCharging, double currentActivePowerBatteryDischarging,
			double currentActivePowerBatteryAutoConsumption, double currentActivePowerBatteryFeedIn,
			double currentActivePowerExternal, double currentReactivePowerExternal, double currentGasPowerExternal) {

		super(sender, timestamp);
		
		this.currentTick = currentTick;
		this.currentActivePower = currentActivePower;
		this.currentActivePowerConsumption = currentActivePowerConsumption;
		this.currentActivePowerChp = currentActivePowerChp;
		this.currentActivePowerChpFeedIn = currentActivePowerChpFeedIn;
		this.currentActivePowerChpAutoConsumption = currentActivePowerChpAutoConsumption;
		this.currentActivePowerPv = currentActivePowerPv;
		this.currentActivePowerPvFeedIn = currentActivePowerPvFeedIn;
		this.currentActivePowerPvAutoConsumption = currentActivePowerPvAutoConsumption;
		this.currentActivePowerBatteryCharging = currentActivePowerBatteryCharging;
		this.currentActivePowerBatteryDischarging = currentActivePowerBatteryDischarging;
		this.currentActivePowerBatteryAutoConsumption = currentActivePowerBatteryAutoConsumption;
		this.currentActivePowerBatteryFeedIn = currentActivePowerBatteryFeedIn;
		this.currentActivePowerExternal = currentActivePowerExternal;
		this.currentReactivePowerExternal = currentReactivePowerExternal;
		this.currentGasPowerExternal = currentGasPowerExternal;
		System.out.println("HHSE erzeugt");
	}

	

	
	public double getCurrentActivePowerConsumption() {
		return currentActivePowerConsumption;
	}

	public void setCurrentActivePowerConsumption(double currentActivePowerConsumption) {
		this.currentActivePowerConsumption = currentActivePowerConsumption;
	}

	public double getCurrentActivePowerChpAutoConsumption() {
		return currentActivePowerChpAutoConsumption;
	}

	public void setCurrentActivePowerChpAutoConsumption(double currentActivePowerChpAutoConsumption) {
		this.currentActivePowerChpAutoConsumption = currentActivePowerChpAutoConsumption;
	}

	public double getCurrentActivePowerPvAutoConsumption() {
		return currentActivePowerPvAutoConsumption;
	}

	public void setCurrentActivePowerPvAutoConsumption(double currentActivePowerPvAutoConsumption) {
		this.currentActivePowerPvAutoConsumption = currentActivePowerPvAutoConsumption;
	}

	public long getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(long currentTick) {
		this.currentTick = currentTick;
	}

	public UUID getSender() {
		return sender;
	}

	public void setSender(UUID sender) {
		this.sender = sender;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getCurrentActivePower() {
		return currentActivePower;
	}

	public void setCurrentActivePower(double currentActivePower) {
		this.currentActivePower = currentActivePower;
	}

	public double getCurrentActivePowerChp() {
		return currentActivePowerChp;
	}

	public void setCurrentActivePowerChp(double currentActivePowerChp) {
		this.currentActivePowerChp = currentActivePowerChp;
	}

	public double getCurrentActivePowerChpFeedIn() {
		return currentActivePowerChpFeedIn;
	}

	public void setCurrentActivePowerChpFeedIn(double currentActivePowerChpFeedIn) {
		this.currentActivePowerChpFeedIn = currentActivePowerChpFeedIn;
	}

	public double getCurrentActivePowerPv() {
		return currentActivePowerPv;
	}

	public void setCurrentActivePowerPv(double currentActivePowerPv) {
		this.currentActivePowerPv = currentActivePowerPv;
	}

	public double getCurrentActivePowerPvFeedIn() {
		return currentActivePowerPvFeedIn;
	}

	public void setCurrentActivePowerPvFeedIn(double currentActivePowerPvFeedIn) {
		this.currentActivePowerPvFeedIn = currentActivePowerPvFeedIn;
	}
	
	public double getCurrentActivePowerBatteryCharging() {
		return currentActivePowerBatteryCharging;
	}

	public void setCurrentActivePowerBatteryCharging(double currentActivePowerBatteryCharging) {
		this.currentActivePowerBatteryCharging = currentActivePowerBatteryCharging;
	}

	public double getCurrentActivePowerBatteryDischarging() {
		return currentActivePowerBatteryDischarging;
	}

	public void setCurrentActivePowerBatteryDischarging(double currentActivePowerBatteryDischarging) {
		this.currentActivePowerBatteryDischarging = currentActivePowerBatteryDischarging;
	}

	public double getCurrentActivePowerBatteryAutoConsumption() {
		return currentActivePowerBatteryAutoConsumption;
	}

	public void setCurrentActivePowerBatteryAutoConsumption(double currentActivePowerBatteryAutoConsumption) {
		this.currentActivePowerBatteryAutoConsumption = currentActivePowerBatteryAutoConsumption;
	}

	public double getCurrentActivePowerBatteryFeedIn() {
		return currentActivePowerBatteryFeedIn;
	}

	public void setCurrentActivePowerBatteryFeedIn(double currentActivePowerBatteryFeedIn) {
		this.currentActivePowerBatteryFeedIn = currentActivePowerBatteryFeedIn;
	}

	public double getCurrentActivePowerExternal() {
		return currentActivePowerExternal;
	}

	public void setCurrentActivePowerExternal(double currentActivePowerExternal) {
		this.currentActivePowerExternal = currentActivePowerExternal;
	}

	public double getCurrentReactivePowerExternal() {
		return currentReactivePowerExternal;
	}

	public void setCurrentReactivePowerExternal(double currentReactivePowerExternal) {
		this.currentReactivePowerExternal = currentReactivePowerExternal;
	}

	public double getCurrentGasPowerExternal() {
		return currentGasPowerExternal;
	}

	public void setCurrentGasPowerExternal(double currentGasPowerExternal) {
		this.currentGasPowerExternal = currentGasPowerExternal;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": Sender " + getSender() + ", time: " + getTimestamp();
	}
	

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public BuildingStateExchange clone() {
		
		return new BuildingStateExchange(this);
	}
	
}
