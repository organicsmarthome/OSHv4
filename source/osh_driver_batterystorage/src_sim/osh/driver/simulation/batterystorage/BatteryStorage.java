package osh.driver.simulation.batterystorage;

/**
 * 
 * @author Jan Mueller, Matthias Maerz
 *
 */
public abstract class BatteryStorage {
	
	protected double efficiencyFactor = 1.0;
	
	private final int standingLoss;
	
	// charging state in percent
	private final int minChargingState;
	private final int maxChargingState;

	private final int maxChargePower;
	private final int minChargePower;
	private final int maxDischargePower;
	private final int minDischargePower;
	
	//Percent from the maxChargepower where the slower charging begins
	private final double chargePowerSlowEndnumber = 0.8;
	
	//state how full the battery is
	protected double stateOfCharge;
	
	//state what percentage from the battery can be used
	protected double stateOfHealth = 1;
	
	//Room Temperature
	public double roomTemperature;
	
	
	protected BatteryStorage(){
		this.standingLoss=0;
		this.minChargingState=0;
		this.maxChargingState=0;

		this.maxChargePower=0;
		this.minChargePower=0;
		this.maxDischargePower=0;
		this.minDischargePower=0;
		
		this.roomTemperature = 0;
	}
	
	public BatteryStorage(
			int standingLoss,
			int minChargingState,
			int maxChargingState,
			int minChargePower,
			int maxChargePower,
			int minDischargePower,
			int maxDischargePower,
			double stateOfCharge,
			double roomTemperature
			) {
		// currently relevant parameters
		this.standingLoss = standingLoss;
		
		this.minChargingState = minChargingState;
		this.maxChargingState = maxChargingState;
		this.minDischargePower = minDischargePower;
		this.maxDischargePower = maxDischargePower;
		this.minChargePower = minChargePower;
		this.maxChargePower = maxChargePower;
		this.stateOfCharge = stateOfCharge;
		
		this.roomTemperature = roomTemperature;
	}

	public abstract void selfDischarge ();
	
	public abstract void capacityLoss (double DoD);
	
	public abstract void capacityLoss ();
	
	public double calculateDoD (double soc, double socT_1){
		double DoD = socT_1 - soc;
		return DoD;
	}
	
	public double getEfficiencyFactor(){
		return this.efficiencyFactor;
	}
	
	public void reduceByStandingLoss(long seconds) {
		int standingEnergyLoss = calcStandingLoss(seconds);
		changeSOC(standingEnergyLoss);
		if (this.stateOfCharge <0){
			this.stateOfCharge=0;
		}
	}
	
	/**
	 * standing loss (DE: Verlustleistung) 
	 * @return [Ws]
	 */
	private int calcStandingLoss(long seconds) {
		int energyLoss = (int) (standingLoss * seconds); //[Ws]
		return energyLoss;
	}
	
	/**
	 * Intervall from the maxSOC when the charging starts to get slower.
	 * e.g. number 0.7 means if the stateChargePower is 70% from the maxSOC, 
	 * the charging starts to slow down 
	 * and the method will return the intervall from 70% to the maxSOC
	 * @return The intervall where the slower charging starts to the maxChargingState.
	 */
	public double getSlowChargePowerIntervall(){
		return (this.maxChargingState - (this.maxChargingState * this.chargePowerSlowEndnumber));
	}
	
	/**
	 * Percent number for the maxSOC when the charging starts to get slower.
	 * e.g. number 0.7 means if the stateChargePower is 70% from the maxSOC, 
	 * the charging starts to slow down.
	 * @return The percent number where the slower charging starts to the maxChargingState.
	 */
	public double getSlowChargePowernumber(){
		return this.chargePowerSlowEndnumber;
	}
	
	/**
	 * positive value: add energy to battery storage<br>
	 * negative value: remove energy from battery storage
	 * @param power [W]
	 * @param seconds [s]
	 */
	public void changeSOCOverTime(int power, long seconds) {
		int energy = (int) (power * seconds);
		this.changeSOC(energy);
	}
	
	/**
	 * positive value: add energy to battery storage<br>
	 * negative value: remove energy from battery storage
	 * @param energy [Ws] (positive value: add energy to battery storage)
	 */
	public void changeSOC(int deltaEnergy) {
		stateOfCharge =  stateOfCharge + deltaEnergy; // negative, because chargePower is negative and dischargePower is positive
	}
	
	public double getStateOfCharge() {
		return this.stateOfCharge;
	}
	
	public double getStateOfHealth() {
		return this.stateOfHealth;
	}
	
	public void setStateOfHealth(double soh) {
		this.stateOfHealth = soh;
	}

	public int getMinChargeState() {
		return this.minChargingState;
	}

	public int getMaxChargeState() {
		return this.maxChargingState;
	}

	public int getMaxChargePower() {
		return this.maxChargePower;
	}
	
	public int getMinChargePower() {
		return this.minChargePower;
	}
	
	public int getMaxDischargePower() {
		return this.maxDischargePower;
	}
	
	public int getMinDischargePower() {
		return this.minDischargePower;
	}
	
}
