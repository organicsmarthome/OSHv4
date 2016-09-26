package osh.cal;

import java.util.UUID;

/**
 * abstract class for the data object between the HAL and the O/C layer
 * 
 * @author Florian Allerding
 *
 */
public abstract class CALExchange 
						implements ICALExchange {
	
	private UUID deviceID;
	private Long timestamp;

	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public CALExchange(UUID deviceID, Long timestamp) {
		super();
		
		this.deviceID = deviceID;
		this.timestamp = timestamp;
	}

	@Override
	public UUID getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(UUID deviceID) {
		this.deviceID = deviceID;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
