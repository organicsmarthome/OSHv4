package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="def")
public class DevicePowerEntry {

	@XmlElement(name="item")
	private Long[] item;

	public Long[] getItem() {
		return item;
	}

	public void setItem(Long[] item) {
		this.item = item;
	}
	
}
