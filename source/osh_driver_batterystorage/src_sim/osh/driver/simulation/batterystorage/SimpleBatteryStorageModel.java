package osh.driver.simulation.batterystorage;

import java.io.Serializable;

/**
 *  Simple Battery <br>
 * 
 * @author Jan Mueller, Matthias Maerz
 *
 */
public class SimpleBatteryStorageModel extends BatteryStorage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8294438797524072700L;
//
//	// storage capacity in kWh 
//	//private final double batteryCapacity;
//	
//	// Battery storage standing loss [W] 
//	private final int standingLoss;
//	
//	// charging state in percent
//	private final int minChargingState;
//	private final int maxChargingState;
//
//	private final int maxChargePower;
//	private final int minChargePower;
//	private final int maxDischargePower;
//	private final int minDischargePower;
//	
//	//Percent from the maxChargepower where the slower charging begins
//	private final double chargePowerSlowEndnumber = 0.9;
//	
//	//state how full the battery is
//	private int stateOfCharge;
	
	/**
	 * CONSTRCUTOR
	 * @param standingLoss [Ws/s]
	 * @param minChargingState [Ws]
	 * @param maxChargingState [Ws]
	 * @param maxChargePower [W]
	 * @param minChargePower [W]
     * @param maxDischargePower [W]
 	 * @param minDischargePower [W]
	 * @param stateOfCharge [Ws]
	 */
	
	protected SimpleBatteryStorageModel(){
		super(0,0,0,0,0,0,0,0,0);
//		this.standingLoss=0;
//		this.minChargingState=0;
//		this.maxChargingState=0;
//
//		this.maxChargePower=0;
//		this.minChargePower=0;
//		this.maxDischargePower=0;
//		this.minDischargePower=0;
	}
	
	
	public SimpleBatteryStorageModel(
			int standingLoss,
			int minChargingState,
			int maxChargingState,
			int minChargePower,
			int maxChargePower,
			int minDischargePower,
			int maxDischargePower,
			double stateOfCharge
			) {
		super(	standingLoss,
				minChargingState,
				maxChargingState,
				minChargePower,
				maxChargePower,
				minDischargePower,
				maxDischargePower,
				stateOfCharge,
				0);//Room Temperature
		
		// currently relevant parameters
//		this.standingLoss = standingLoss;
//		
//		this.minChargingState = minChargingState;
//		this.maxChargingState = maxChargingState;
//		this.minDischargePower = minDischargePower;
//		this.maxDischargePower = maxDischargePower;
//		this.minChargePower = minChargePower;
//		this.maxChargePower = maxChargePower;
//		this.stateOfCharge = stateOfCharge;
	}
	

	@Override
	public void selfDischarge() {
	}


	@Override
	public void capacityLoss(double DoD) {
	}


	@Override
	public void capacityLoss() {
	}
	
	
}	