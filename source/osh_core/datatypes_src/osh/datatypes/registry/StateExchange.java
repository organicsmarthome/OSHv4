package osh.datatypes.registry;

import java.io.Serializable;
import java.util.UUID;




/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */

public abstract class StateExchange extends Exchange implements Cloneable, Serializable {

//	/** for JAXB */
//	@Deprecated
//	private StateExchange() {
//		super(null, 0);
//	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -701677297851327328L;

	protected StateExchange() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public StateExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	@Override
	public StateExchange clone() {
		try {
			return (StateExchange) super.clone();
		} 
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("clone for StateExchange not correctly implemented", e);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ": Sender " + getSender() + ", time: " + getTimestamp();
	}
	
	
	
}
