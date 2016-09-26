package osh.datatypes.registry;

import java.util.UUID;


/**
 * 
 * @author Till Schuberth
 *
 */
public abstract class EventExchange extends Exchange implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7069547228943707389L;

	public EventExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	@Override
	public EventExchange clone() {
		try {
			return (EventExchange) super.clone();
		} 
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("subclass doesn't provide proper cloning functionality", e);
		}
	}

	/** tries to cast this object to the given event type.
	 * @param type the type of this event
	 * @return This object casted to the event type or null if this is not possible.
	 */
	public <T extends EventExchange> T castToType(Class<T> type) {
		if (type.isAssignableFrom(this.getClass())) {
			return type.cast(this);
		} else {
			return null;
		}
	}

}
