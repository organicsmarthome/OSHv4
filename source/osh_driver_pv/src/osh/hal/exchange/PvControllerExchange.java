package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALControllerExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class PvControllerExchange extends HALControllerExchange {

	private Boolean newPvSwitchedOn;
	private Integer reactivePowerTargetValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public PvControllerExchange(
			UUID deviceID, 
			Long timestamp, 
			Boolean newPvSwitchedOn, 
			Integer reactivePowerTargetValue) {
		super(deviceID, timestamp);
		
		this.newPvSwitchedOn = newPvSwitchedOn;
		this.reactivePowerTargetValue = reactivePowerTargetValue;
	}

	
	public Boolean getNewPvSwitchedOn() {
		return newPvSwitchedOn;
	}

	public void setNewPvSwitchedOn(Boolean newPvSwitchedOn) {
		this.newPvSwitchedOn = newPvSwitchedOn;
	}

	public Integer getNewReactivePower() {
		return reactivePowerTargetValue;
	}

	public void setNewReactivePower(Integer newReactivePower) {
		this.reactivePowerTargetValue = newReactivePower;
	}

}
