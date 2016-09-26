package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="def")
public class DevicePowerHistory {
	
	@XmlElement(name="label")
	private String label;
	
	@XmlElement(name="data")
	private DevicePowerEntry[] data;
	
	public void setData(DevicePowerEntry[] data) {
		this.data = data;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDataLong(Long[][] lData) {
		
		data = new DevicePowerEntry[lData.length];
		for(int i = 0; i< lData.length; i++)
		{
			data[i] = new DevicePowerEntry();
			data[i].setItem(lData[i]);
		}
		
	}

	public String getLabel() {
		return label;
	}

	public DevicePowerEntry[] getData() {
		return data;
	}
	

}
