package osh.datatypes.registry.oc.state;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import osh.datatypes.registry.StateExchange;


@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
@XmlType
public class LastActionExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8418589703573486289L;
	@XmlAnyElement
	private IAction lastAction;

	public LastActionExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	/** for JAXB, do not use */
	@SuppressWarnings("unused")
	@Deprecated
	private LastActionExchange() {
		super(null, 0);
	}
	
	public LastActionExchange(UUID sender, long timestamp, IAction lastAction) {
		super(sender, timestamp);
		this.lastAction = lastAction;
	}

	public IAction getLastAction() {
		return lastAction;
	}

	public void setLastAction(IAction lastAction) {
		this.lastAction = lastAction;
	}
	
}
