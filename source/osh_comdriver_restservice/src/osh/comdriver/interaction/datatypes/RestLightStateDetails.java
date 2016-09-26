package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="lightStateDetails")
public class RestLightStateDetails extends RestStateDetail {

	protected boolean state;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestLightStateDetails() {
		this(null, 0);
	}
	
	public RestLightStateDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
