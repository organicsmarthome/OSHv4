package osh.hal.exchange;

import java.util.UUID;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;

/**
 * 
 * @author Jan Mueller
 *
 */
public class BatteryStorageOX
					extends HALDeviceObserverExchange
					implements IHALElectricalPowerDetails {

	private int activePower;
	private int reactivePower;
	
	private double batteryStateOfCharge;
	private double batteryStateOfHealth;
	private int batteryStandingLoss;
	private int batteryMinChargingState;
	private int batteryMaxChargingState;
	private int batteryMinChargePower;
	private int batteryMinDischargePower;
	private int batteryMaxChargePower;
	private int inverterMinComplexPower;
	private int inverterMaxComplexPower;
	private int inverterMaxPower;
	private int inverterMinPower;
	private int batteryMaxDischargePower;
	
	private int rescheduleAfter;
	private long newIppAfter;
	private int triggerIppIfDeltaSoCBigger;
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public BatteryStorageOX(
			UUID deviceID, 
			Long timestamp,

			int activePower,
			int reactivePower,
			
			double batteryStateOfCharge,
			double batteryStateOfHealth,
			int batteryStandingLoss,
			int batteryMinChargingState,
			int batteryMaxChargingState,
			int batteryMinChargePower,
			int batteryMaxChargePower,
			int batteryMinDischargePower,
			int batteryMaxDischargePower,
			int inverterMinComplexPower,
			int inverterMaxComplexPower,
			int inverterMinPower,
			int inverterMaxPower,
			int rescheduleAfter,
			long newIppAfter,
			int triggerIppIfDeltaSoCBigger,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(deviceID, timestamp);
		

		this.activePower = activePower;
		this.reactivePower = reactivePower;
		
		this.batteryStateOfCharge = batteryStateOfCharge;
		
		this.batteryStandingLoss = batteryStandingLoss;
		
		this.batteryMinChargingState = batteryMinChargingState;
		this.batteryMaxChargingState = batteryMaxChargingState;
		
		this.batteryMinChargePower = batteryMinChargePower;
		this.batteryMaxChargePower = batteryMaxChargePower;
		
		this.batteryMinDischargePower = batteryMinDischargePower;
		this.batteryMaxDischargePower = batteryMaxDischargePower;
		
		this.inverterMinComplexPower = inverterMinComplexPower;
		this.inverterMaxComplexPower = inverterMaxComplexPower;
		
		this.inverterMaxPower = inverterMaxPower;
		this.inverterMinPower = inverterMinPower;
		
		this.rescheduleAfter = rescheduleAfter;
		this.newIppAfter = newIppAfter;
		this.triggerIppIfDeltaSoCBigger = triggerIppIfDeltaSoCBigger;
		
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	}
	
	@Override
	public int getActivePower() {
		return activePower;
	}

	@Override
	public int getReactivePower() {
		return reactivePower;
	}


	public double getBatteryStateOfCharge() {
		return batteryStateOfCharge;
	}
	
	public double getBatteryStateOfHealth() {
		return batteryStateOfHealth;
	}

	public int getBatteryStandingLoss() {
		return batteryStandingLoss;
	}

	public int getBatteryMinChargingState() {
		return batteryMinChargingState;
	}

	public int getBatteryMaxChargingState() {
		return batteryMaxChargingState;
	}

	public int getBatteryMinChargePower() {
		return batteryMinChargePower;
	}

	public int getBatteryMinDischargePower() {
		return batteryMinDischargePower;
	}

	public int getBatteryMaxChargePower() {
		return batteryMaxChargePower;
	}

	public int getInverterMinComplexPower() {
		return inverterMinComplexPower;
	}

	public int getInverterMaxComplexPower() {
		return inverterMaxComplexPower;
	}

	public int getInverterMaxPower() {
		return inverterMaxPower;
	}

	public int getInverterMinPower() {
		return inverterMinPower;
	}

	public int getBatteryMaxDischargePower() {
		return batteryMaxDischargePower;
	}
	
	public int getRescheduleAfter() {
		return rescheduleAfter;
	}

	public long getNewIppAfter() {
		return newIppAfter;
	}

	public int getTriggerIppIfDeltaSoCBigger() {
		return triggerIppIfDeltaSoCBigger;
	}

	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}

	public int getCompressionValue() {
		return compressionValue;
	}
	
}
