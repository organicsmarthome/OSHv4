package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALControllerExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ChpControllerExchange 
				extends HALControllerExchange {
	
	private boolean stopGenerationFlag;
	
	private boolean electricityRequest;
	private boolean heatingRequest;
	
	private int scheduledRuntime;

	/**
	 * 
	 * @param deviceID
	 * @param timestamp
	 */
	public ChpControllerExchange(
			UUID deviceID, 
			Long timestamp, 
			boolean stopGenerationFlag, 
			boolean electricityRequest, 
			boolean heatingRequest,
			int scheduledRuntime) {
		super(deviceID, timestamp);
		
		this.stopGenerationFlag = stopGenerationFlag;
		this.electricityRequest = electricityRequest;
		this.heatingRequest = heatingRequest;
		this.scheduledRuntime = scheduledRuntime;
	}

	
	public boolean isStopGenerationFlag() {
		return stopGenerationFlag;
	}

	public boolean isElectricityRequest() {
		return electricityRequest;
	}

	public boolean isHeatingRequest() {
		return heatingRequest;
	}

	public int getScheduledRuntime() {
		return scheduledRuntime;
	}


	public void setElectricityRequest(boolean electricityRequest) {
		this.electricityRequest = electricityRequest;
	}
	
	public void setHeatingRequest(boolean heatingRequest) {
		this.heatingRequest = heatingRequest;
	}
	
	public void setStopGenerationFlag(boolean stopGenerationFlag) {
		this.stopGenerationFlag = stopGenerationFlag;
	}
	
}
