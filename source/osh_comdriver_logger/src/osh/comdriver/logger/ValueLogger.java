package osh.comdriver.logger;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;

/**
 * Logger superclass
 * @author Ingo Mauser
 *
 */
public abstract class ValueLogger {
	protected IGlobalLogger logger;
	abstract public void log(long timestamp, Object entity)  throws OSHException;
}
