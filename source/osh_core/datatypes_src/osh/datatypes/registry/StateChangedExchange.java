package osh.datatypes.registry;

import java.util.UUID;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class StateChangedExchange extends EventExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -145050460542819652L;
	private Class<? extends StateExchange> type;
	private UUID statefulentity;
	
	
	/**
	 * CONSTRUCTOR
	 * @param timestamp
	 * @param type
	 * @param statefulentity
	 */
	public StateChangedExchange(long timestamp,
			Class<? extends StateExchange> type, UUID statefulentity) {
		super(null, timestamp);
		
		this.type = type;
		this.statefulentity = statefulentity;
	}
	

	public Class<? extends StateExchange> getType() {
		return type;
	}

	public UUID getStatefulentity() {
		return statefulentity;
	}

	// needed for StateChangedEventSet
	@Override
	public int hashCode() {
		if (statefulentity == null){
			System.out.println("STATEFULENTITY NULL");
		}
		if (type == null){
			System.out.println("TYPE NULL");
		}
		return statefulentity.hashCode() ^ type.hashCode();
	}
	
	// needed for StateChangedEventSet
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( !(obj instanceof StateChangedExchange) )
			return false;

		StateChangedExchange other = (StateChangedExchange) obj;
		
		return statefulentity.equals(other.statefulentity)
			&& type.equals(other.type);
	}
}
