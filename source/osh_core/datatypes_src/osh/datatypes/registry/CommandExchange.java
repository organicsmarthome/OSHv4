package osh.datatypes.registry;

import java.util.UUID;

/**
 * 
 * @author Till Schuberth
 *
 */
public abstract class CommandExchange extends EventExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4204877580720933340L;
	protected UUID receiver;
	
	public CommandExchange(UUID sender, UUID receiver, long timestamp) {
		super(sender, timestamp);
		this.receiver = receiver;
	}

	public UUID getReceiver() {
		return receiver;
	}
	
}
