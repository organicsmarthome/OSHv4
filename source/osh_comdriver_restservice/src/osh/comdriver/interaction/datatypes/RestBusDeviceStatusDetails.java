package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="busDeviceStatusDetails")
public class RestBusDeviceStatusDetails extends RestStateDetail {

	@XmlType
	public enum ConnectionStatus {
		@XmlEnumValue("ATTACHED")
		ATTACHED,
		@XmlEnumValue("DETACHED")
		DETACHED,
		@XmlEnumValue("ERROR")
		ERROR
	}
	
	@Enumerated(value=EnumType.STRING) 
	protected ConnectionStatus state;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestBusDeviceStatusDetails() {
		this(null, 0);
	}

	public RestBusDeviceStatusDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public ConnectionStatus getState() {
		return state;
	}

	public void setState(ConnectionStatus state) {
		this.state = state;
	}
}
