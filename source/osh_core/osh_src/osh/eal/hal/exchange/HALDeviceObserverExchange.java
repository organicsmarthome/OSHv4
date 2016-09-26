package osh.eal.hal.exchange;

import java.util.UUID;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.eal.hal.interfaces.common.IHALDeviceMetaDetails;

/**
 * Please remember cloning!
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class HALDeviceObserverExchange 
				extends HALObserverExchange
				implements IHALDeviceMetaDetails {
	
	private String name;
	private String location;
	
	private DeviceTypes deviceType;
	private DeviceClassification deviceClass;
	
	private boolean configured;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public HALDeviceObserverExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public DeviceTypes getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceTypes deviceType) {
		this.deviceType = deviceType;
	}

	public DeviceClassification getDeviceClassification() {
		return deviceClass;
	}

	public void setDeviceClass(DeviceClassification deviceClass) {
		this.deviceClass = deviceClass;
	}

	public boolean isConfigured() {
		return configured;
	}

	public void setConfigured(boolean configured) {
		this.configured = configured;
	}
	
	public void setDeviceMetaDetails(DeviceMetaDriverDetails deviceMetaDetails) {
		this.name = deviceMetaDetails.getName();
		this.location = deviceMetaDetails.getLocation();
		
		this.deviceType = deviceMetaDetails.getDeviceType();
		this.deviceClass = deviceMetaDetails.getDeviceClassification();
		
		this.configured = deviceMetaDetails.isConfigured();
	}

}
