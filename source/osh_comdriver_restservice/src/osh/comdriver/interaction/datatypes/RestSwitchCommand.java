package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlRootElement(name="switchCommand")
public class RestSwitchCommand {
	protected boolean on;
	
	public boolean isTurnOn() {
		return on;
	}

	public void setTurnOn(boolean on) {
		this.on = on;
	};
}
