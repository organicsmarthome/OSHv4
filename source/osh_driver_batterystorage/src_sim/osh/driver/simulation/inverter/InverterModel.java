package osh.driver.simulation.inverter;

import java.io.Serializable;

/**
 * InverterModel <br>
 * 
 * @author Jan Mueller, Matthias Maerz
 *
 */
public abstract class InverterModel implements Serializable{	
	// ### configuration ###
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6872029056018832655L;
	
	//private final int powerDelta = 100;
	private final int standbyPower = 0;
	
	//private final double inverterEfficiency = 0.85;
	
	// Battery charging power - NOT USED          
	private final int minComplexPower;
	private final int maxComplexPower;
	
	private final int minPower;
	private final int maxPower;
	
	// ### variables ###
	private int activePower=0;
	private int reactivePower=0;
	private int inverterBasePower = 0;
	
	private boolean isOn = false;
	
	
	// ### for logging purposes ###
	//private long timestampOfLastChange = 0;
	
	public InverterModel( ) {
		this.minComplexPower =0;
		this.maxComplexPower = 0;
		this.minPower = 0;
		this.maxPower = 0;
	}
	
	/**
	 * CONSTRUCTOR
	 * @param minComplexPower [VA]
	 * @param maxComplexPower [VA]
	 * @param minPower [W]
	 * @param maxPower [W]
	 */
	public InverterModel( 
			int minComplexPower,
			int maxComplexPower,
			int minPower,
			int maxPower) {
		this.minComplexPower = minComplexPower;
		this.maxComplexPower = maxComplexPower;
		this.minPower = minPower;
		this.maxPower = maxPower;
	}
	
	public abstract double getinverterEfficiency();
		
	
	
	public int calcBatteryChargePower(int availablePower) { //current power is negative 
		int chargePower = -1*(int) Math.round((availablePower - Math.signum(this.inverterBasePower)) * (getinverterEfficiency()) );
		return chargePower; // charge power is positive
	}
	
//	public int calcBatteryChargePower(int currentPower) { //current power is negative
//		int chargePower = (int) Math.round((currentPower + this.inverterBasePower) * (1 - this.inverterEfficiency) );
//		return chargePower; // charge power is negative
//	}
//	
//	
//	public int calcBatteryDischargePower(int currentPower) { //current power positive
//		int dischargePower = (int) Math.round((currentPower - this.inverterBasePower) * (1 - this.inverterEfficiency));
//		return dischargePower; // discharge power is positive
//	}
	
	
	public int getStandbyPower() {
		return this.standbyPower;
	}

	public int calcActivePowerOutputChargingState(double batteryChargePower) { // charge power is positive
	int activePower = (int) Math.round(((batteryChargePower) ) / (getinverterEfficiency()) + Math.signum(batteryChargePower)*this.inverterBasePower);
	return activePower;
}
	
	
//	public int calcActivePowerOutputChargingState(double batteryChargePower) { // charge power is negative
//		int activePower = (int) Math.round(((batteryChargePower) ) / (1 - this.inverterEfficiency) - this.inverterBasePower);
//		return activePower;
//	}
//	 
//	public int calcActivePowerOutputDischargingState(double batteryDischargePower) { // discharge power is positive
//		int activePower = (int) Math.round(((batteryDischargePower) ) / (1 - this.inverterEfficiency) + this.inverterBasePower);
//		return activePower;
//	}
	
	public int getMaxChargePower() {
		return this.maxPower;
	}
	public int getMinChargePower() {
		return this.minPower;
	}
	public int getMaxDischargePower() {
		return this.maxPower;
	}
	public int getMinDischargePower() {
		return this.minPower;
	}
	public boolean isOn() {
		return this.isOn;
	}
	
	public void switchOn() {
		this.isOn = true;
	}
	public void switchOff() {	

		this.isOn = false;
	}
	
	public int getActivePower() {	
		return this.activePower;
	}
	
	public void setActivePower(int power) {	
		this.activePower = power;
	}
	public int getReactivePower() {	
		return this.reactivePower;
	}
	public void setReactivePower(int power) {	
		this.reactivePower = power;
	}
	
	public int getMaxChargeComplexPower() {
		return this.maxComplexPower;
	}
	public int getMinChargeComplexPower() {
		return this.minComplexPower;
	}
	public int getMaxDischargeComplexPower() {
		return this.maxComplexPower;
	}
	public int getMinDischargeComplexPower() {
		return this.minComplexPower;
	}
	
	
	
	
	// ### helper methods ###
	
		
	// ### for logging purposes ###
	
}
