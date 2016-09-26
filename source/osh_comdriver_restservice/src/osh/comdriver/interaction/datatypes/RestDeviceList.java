package osh.comdriver.interaction.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Kaibin Bao
 *
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="DeviceList")
public class RestDeviceList {
	private List<RestDevice> devicelist;

	public RestDeviceList() {
		devicelist = new ArrayList<>();
	}
	
	@XmlElement(name="Device")
	public List<RestDevice> getDeviceList() {
		if( devicelist == null )
			devicelist = new ArrayList<RestDevice>();
		return devicelist;
	}
	
	public void setDeviceList(List<RestDevice> device) {
		this.devicelist = device;
	}

	public void add(RestDevice device) {
		devicelist.add(device);
	}
}
