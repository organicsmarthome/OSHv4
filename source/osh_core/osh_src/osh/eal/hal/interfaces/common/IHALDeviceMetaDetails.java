package osh.eal.hal.interfaces.common;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALDeviceMetaDetails {
	
	public String getName();
	public String getLocation();
	
	public DeviceTypes getDeviceType();
	public DeviceClassification getDeviceClassification();
	
	public boolean isConfigured();
}
