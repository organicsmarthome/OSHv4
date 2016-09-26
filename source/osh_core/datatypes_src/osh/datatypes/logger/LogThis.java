package osh.datatypes.logger;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class LogThis extends CommandExchange {
	
	private static final long serialVersionUID = 5173636358971480331L;
	
	private IAnnotatedForLogging toLog;

	
	/**
	 * CONSTRUCTOR 
	 */
	public LogThis(UUID sender, UUID receiver, long timestamp, IAnnotatedForLogging toLog) {
		super(sender, receiver, timestamp);
		this.toLog = toLog;
	}

	
	public IAnnotatedForLogging getToLog() {
		return toLog;
	}
	
}
