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
@XmlType(name="applianceDetails")
public class RestApplianceControlApplianceSwitchOffDetails {
	
protected boolean done = true;
	
	/** for JAXB */
	public RestApplianceControlApplianceSwitchOffDetails() {
		//NOTHING
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	};
	
	
	
}
