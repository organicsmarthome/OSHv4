package osh.comdriver.logger.kit;

import osh.comdriver.logger.ValueDatabaseLogger;
import osh.core.logging.IGlobalLogger;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class SmartHomeLog1LegacyDatabaseLogger extends ValueDatabaseLogger {
	
	public SmartHomeLog1LegacyDatabaseLogger(IGlobalLogger logger) {
		super("SmartHomeLog1LegacyLogger", logger);
	}

}
