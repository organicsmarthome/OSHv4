package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="deviceMetaDetails")
public class RestDeviceMetaDetails extends RestStateDetail {

	protected String name;
	protected String location;
	
	protected DeviceTypes deviceType;
	protected DeviceClassification deviceClassification;
	
	protected boolean configured;
	
	protected String icon;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestDeviceMetaDetails() {
		this(null, 0);
	}

	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public RestDeviceMetaDetails(UUID sender, long timestamp) {
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
	
	
}
