package osh.hal.exchange;

import java.util.UUID;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricCurrentDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricVoltageDetails;

/**
 * 
 * @author Ingo Mauser, verpfuscht von sko & mmae
 *
 */
public class PvObserverExchange 
				extends HALObserverExchange
				implements 	IHALElectricCurrentDetails,
							IHALElectricalPowerDetails,
							IHALElectricVoltageDetails {
	
	// ### IHALElectricCurrentDetails ###
	private double current;
	
	// ### IHALElectricPowerDetails ###
	private int activePower;
	private int reactivePower;

	// ### IHALElectricVoltageDetails ###
	private double voltage;
	
	
	public int getRescheduleAfter() {
		return rescheduleAfter;
	}

	public void setRescheduleAfter(int rescheduleAfter) {
		this.rescheduleAfter = rescheduleAfter;
	}

	public long getNewIppAfter() {
		return newIppAfter;
	}

	public void setNewIppAfter(long newIppAfter) {
		this.newIppAfter = newIppAfter;
	}

	public int getTriggerIppIfDeltaPBigger() {
		return triggerIppIfDeltaPBigger;
	}

	public void setTriggerIppIfDeltaPBigger(int triggerIppIfDeltaPBigger) {
		this.triggerIppIfDeltaPBigger = triggerIppIfDeltaPBigger;
	}

	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(LoadProfileCompressionTypes compressionType) {
		this.compressionType = compressionType;
	}

	public int getCompressionValue() {
		return compressionValue;
	}

	public void setCompressionValue(int compressionValue) {
		this.compressionValue = compressionValue;
	}

	// ### for optimization
	private int rescheduleAfter;
	private long newIppAfter;
	private int triggerIppIfDeltaPBigger;
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR 1
	 */
	public PvObserverExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	
	/**
	 * CONSTRUCTOR 2
	 */
	public PvObserverExchange(
			UUID deviceID, 
			Long timestamp, 
			int activePower,
			int reactivePower,
			double voltage,
			
			int rescheduleAfter,
			long newIppAfter,
			int triggerIppIfDeltaPBigger,
			LoadProfileCompressionTypes compressionType,
			int compressionValue
			) {
		super(deviceID, timestamp);

		this.activePower = activePower;
		this.reactivePower = reactivePower;
		
		this.rescheduleAfter = rescheduleAfter;
		this.newIppAfter = newIppAfter;
		this.triggerIppIfDeltaPBigger = triggerIppIfDeltaPBigger;
		
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
		
	}


	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
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
