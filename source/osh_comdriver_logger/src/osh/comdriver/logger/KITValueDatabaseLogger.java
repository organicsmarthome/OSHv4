package osh.comdriver.logger;

import osh.core.logging.IGlobalLogger;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class KITValueDatabaseLogger extends ValueDatabaseLogger {
	
	public KITValueDatabaseLogger(IGlobalLogger logger) {
		super("KITStateLogger", logger);
	}

}
