package osh.comdriver.logger.kit;

import osh.comdriver.logger.ValueDatabaseLogger;
import osh.core.logging.IGlobalLogger;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class MeregioMobilLegacyDatabaseLogger extends ValueDatabaseLogger {
	
	public MeregioMobilLegacyDatabaseLogger(IGlobalLogger logger) {
		super("MeregioMobilLegacyLogger", logger);
	}

}
