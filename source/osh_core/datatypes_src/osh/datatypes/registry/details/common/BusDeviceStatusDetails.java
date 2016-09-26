package osh.datatypes.registry.details.common;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType
public class BusDeviceStatusDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8380215142279886946L;

	@XmlType
	public enum ConnectionStatus {
		@XmlEnumValue("ATTACHED")
		ATTACHED,
		@XmlEnumValue("DETACHED")
		DETACHED,
		@XmlEnumValue("ERROR")
		ERROR,
		@XmlEnumValue("UNDEFINED")
		UNDEFINED
	}
	

	protected ConnectionStatus state;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private BusDeviceStatusDetails() {
		this(null, 0, ConnectionStatus.UNDEFINED);
	}

	public BusDeviceStatusDetails(UUID sender, long timestamp, ConnectionStatus state) {
		super(sender, timestamp);
		
		this.state = state;
	}

	public ConnectionStatus getState() {
		return state;
	}

	@Override
	public String toString() {
		return "BusDeviceStatus: " + state.name();
	}
}
