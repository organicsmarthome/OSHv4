package osh.comdriver.interaction.datatypes.fzi.appliancecontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="switchOffapplianceDetails")
public class RestApplianceControlApplianceStatusDetails {
	
	protected boolean on;
	
	/** for JAXB */
	public RestApplianceControlApplianceStatusDetails() {
		//NOTHING
	};

	
	public void setOn(boolean on) {
		this.on = on;
	}
	
	public boolean isOn() {
		return on;
	}
	
}
