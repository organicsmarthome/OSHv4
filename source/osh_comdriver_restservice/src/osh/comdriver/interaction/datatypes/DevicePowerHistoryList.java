package osh.comdriver.interaction.datatypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="abc")
public class DevicePowerHistoryList {

	@XmlElement(name="List")
	private List<DevicePowerHistory> listDevicePowerHistory;

	public List<DevicePowerHistory> getListDevicePowerHistory() {
		return listDevicePowerHistory;
	}

	public void setListDevicePowerHistory(
			List<DevicePowerHistory> listDevicePowerHistory) {
		this.listDevicePowerHistory = listDevicePowerHistory;
	}
	
	
	
}
