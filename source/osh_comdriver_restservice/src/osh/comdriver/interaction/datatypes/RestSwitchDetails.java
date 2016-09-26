package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="switchDetails")
public class RestSwitchDetails extends RestStateDetail {
	protected boolean on;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestSwitchDetails() {
		this(null, 0);
	};
	
	public RestSwitchDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	};
}
