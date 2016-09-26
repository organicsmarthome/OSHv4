package osh.datatypes.registry.details.common;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.registry.StateExchange;

@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType
public class DeviceMetaDriverDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4956177889110340703L;
	protected String name;
	protected String location;
	
	protected DeviceTypes deviceType;
	protected DeviceClassification deviceClassification;
	
	protected boolean configured;
	
	protected String icon;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private DeviceMetaDriverDetails() {
		this(null, 0);
	}

	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public DeviceMetaDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
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
		return deviceClassification;
	}

	public void setDeviceClassification(DeviceClassification deviceClassification) {
		this.deviceClassification = deviceClassification;
	}
	
	public boolean isConfigured() {
		return configured;
	}
	
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}
	
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return "Device \"" + name + "\" in " + location + " type: " + deviceType + "(" + deviceClassification + ")";
	}
	
	@Override
	public DeviceMetaDriverDetails clone() {
		long uuidSenderLSB = this.sender.getLeastSignificantBits();
		long uuidSenderMSB = this.sender.getMostSignificantBits();
		DeviceMetaDriverDetails clone = new DeviceMetaDriverDetails(new UUID(uuidSenderMSB, uuidSenderLSB), this.getTimestamp());
		
		clone.name = this.name;
		clone.location = this.location;
		
		clone.deviceType = this.deviceType;
		clone.deviceClassification = this.deviceClassification;
		
		clone.configured = this.configured;
		
		clone.icon = this.icon;
		
		return clone;
	}
	
}
