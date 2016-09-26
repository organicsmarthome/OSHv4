package osh.hal.exchange;

import java.util.UUID;

import osh.eal.hal.exchange.HALControllerExchange;


/**
 * 
 * @author Ingo Mauser, Julian Rothenbacher
 *
 */
public class FutureApplianceControllerExchange 
				extends HALControllerExchange {
	
	
	private UUID applianceConfigurationProfileID;
	private int selectedProfileId;
	private long[] selectedStartTimes;
	

	/**
	 * CONSTRUCTOR
	 */
	public FutureApplianceControllerExchange(
			UUID deviceID, 
			Long timestamp,
			UUID applianceConfigurationProfileID,
			int selectedProfileId,
			long[] selectedStartTimes
			) {
		super(deviceID, timestamp);
		
		this.applianceConfigurationProfileID = applianceConfigurationProfileID;
		this.selectedProfileId = selectedProfileId;
		this.selectedStartTimes = selectedStartTimes;
	}

	public UUID getApplianceConfigurationProfileID() {
		return applianceConfigurationProfileID;
	}

	public int getSelectedProfileId() {
		return selectedProfileId;
	}

	public long[] getSelectedStartTimes() {
		return selectedStartTimes;
	}
	
	
	// CLONING not necessary
	
}
