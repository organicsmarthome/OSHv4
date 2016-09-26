package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.common.IHALSwitchDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class SmartPlugObserverExchange 
				extends HALDeviceObserverExchange 
				implements	IHALElectricalPowerDetails, 
							IHALSwitchDetails {
	
	//
	private boolean incompleteData;
	
	// ### IHALElectricPowerDetails ###
	private int activePower;
	private int reactivePower;
	
	// ### IHALSwitchDetails ###
	private boolean on;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public SmartPlugObserverExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}

	public void setIncompleteData(boolean incompleteData) {
		this.incompleteData = incompleteData;
	}
	
	public boolean isIncompleteData() {
		return incompleteData;
	}

	public boolean isOn() {
		return on;
	}


	public void setOn(boolean on) {
		this.on = on;
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
