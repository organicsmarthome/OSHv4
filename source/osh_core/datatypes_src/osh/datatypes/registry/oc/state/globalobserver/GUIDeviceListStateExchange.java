package osh.datatypes.registry.oc.state.globalobserver;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.registry.StateExchange;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GUIDeviceListStateExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9104434585663750101L;
	private Set<DeviceTableEntry> deviceList;
	
	@Deprecated
	public GUIDeviceListStateExchange() {
		super(null, 0L);
	}
	
	public GUIDeviceListStateExchange(UUID sender, long timestamp, Set<DeviceTableEntry> deviceList) {
		super(sender, timestamp);
		this.deviceList = deviceList;
	}

	public Set<DeviceTableEntry> getDeviceList() {
		return deviceList;
	}

}
