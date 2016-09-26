package osh.busdriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import osh.comdriver.logger.ValueConsoleLogger;
import osh.comdriver.logger.ValueDatabaseLogger;
import osh.comdriver.logger.ValueFileLogger;
import osh.comdriver.logger.ValueLogger;
import osh.comdriver.logger.ValueLoggerConfiguration;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.logger.IAnnotatedForLogging;
import osh.datatypes.logger.LogThis;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateExchange;
import osh.eal.hal.HALBusDriver;
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public abstract class LoggerBusDriver extends HALBusDriver implements IEventTypeReceiver {
	
	// Config
	boolean valueLoggingToConsoleActive = true;
	int valueLoggingToConsoleResolution = 60;
	
	boolean valueLoggingToFileActive = false;
	int valueLoggingToFileResolution = 60;
	
	boolean valueLoggingToDatabaseActive = true;
	int valueLoggingToDatabaseResolution = 60;
//	int valueLoggingToDatabaseResolution = 10;
	
	boolean valueLoggingToRrdDatabaseActive = true;
	int valueLoggingToRrdDatabaseResolution = 5;
	
	//
	protected ValueConsoleLogger consoleLog;
	protected ValueFileLogger fileLog;
	protected ValueDatabaseLogger databaseLog;
	
	protected static boolean logAll = true;
	
	protected ValueLoggerConfiguration valueLoggerConfiguration;
	protected HashMap<UUID,List<String>> loggerUuidAndClassesToLogMap;
	
	protected long lastLoggingToConsoleAt;
	protected long lastLoggingToFileAt;
	protected long lastLoggingToDatabaseAt;
	protected long lastLoggingToRrdDatabaseAt;

	
	/**
	 * CONSTRUCTOR
	 */
	public LoggerBusDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
		this.valueLoggerConfiguration = new ValueLoggerConfiguration(
				valueLoggingToConsoleActive, 
				valueLoggingToConsoleResolution, 
				valueLoggingToFileActive, 
				valueLoggingToFileResolution, 
				valueLoggingToDatabaseActive, 
				valueLoggingToDatabaseResolution, 
				valueLoggingToRrdDatabaseActive, 
				valueLoggingToRrdDatabaseResolution);

		
		if ( valueLoggerConfiguration !=null && valueLoggerConfiguration.getIsValueLoggingToFileActive()  ) {
			fileLog = new ValueFileLogger(
					getOSH().getOSHstatus().getRunID(),
					"0",
					getOSH().getOSHstatus().getConfigurationID(),
					getOSH().getOSHstatus().isSimulation());
		}
		
		//TODO: add option for "log all"
		String loggerUuidAndClassesToLogMapString = getDriverConfig().getParameter("loggeruuidandclassestolog");
		if (loggerUuidAndClassesToLogMapString != null 
				&& !loggerUuidAndClassesToLogMapString.equals("")) {
			String[] splittedLoggerUuidAndClassesToLogMapString = loggerUuidAndClassesToLogMapString.split(";");
			if (splittedLoggerUuidAndClassesToLogMapString != null 
					&& splittedLoggerUuidAndClassesToLogMapString.length > 0) {
				
				loggerUuidAndClassesToLogMap = new HashMap<>();
				
				for (int i = 0; i < splittedLoggerUuidAndClassesToLogMapString.length; i++) {
					UUID uuid = UUID.fromString(splittedLoggerUuidAndClassesToLogMapString[i].split(":")[0]);
					String classes = splittedLoggerUuidAndClassesToLogMapString[i].split(":")[1];
					String[] splittedClasses = classes.split(",");
					List<String> listedClasses = new ArrayList<String>();
					for (int j = 0; j < splittedClasses.length; j++) {
						listedClasses.add(splittedClasses[j]);
					}
					loggerUuidAndClassesToLogMap.put(uuid, listedClasses);
				}
			}
		}
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getDriverRegistry().register(LogThis.class, this);
	}
	
	/**
	 * Pull-logging
	 */
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		ArrayList<ValueLogger> activeLoggers = new ArrayList<>();
		
		long currentTime = getTimer().getUnixTime();
		
		if (consoleLog != null
				&& (currentTime - lastLoggingToConsoleAt) >= valueLoggerConfiguration.getValueLoggingToConsoleResolution()) {
			activeLoggers.add(consoleLog);
			lastLoggingToConsoleAt = currentTime;
		}
		
		if (fileLog != null
				&& (currentTime - lastLoggingToFileAt) >= valueLoggerConfiguration.getValueLoggingToFileResolution()) {
			activeLoggers.add(fileLog);
			lastLoggingToFileAt = currentTime;
		}
		
		if (databaseLog != null
				&& (currentTime - lastLoggingToDatabaseAt) >= valueLoggerConfiguration.getValueLoggingToDatabaseResolution()) {
			activeLoggers.add(databaseLog);
			lastLoggingToDatabaseAt = currentTime;
			getGlobalLogger().logDebug("last logging at: " + lastLoggingToDatabaseAt);
		}

		//TODO: log all (allow partial logging)
		if (!activeLoggers.isEmpty()) {
			if ( logAll ) {
				for ( Class<? extends StateExchange> type : getDriverRegistry().getTypes() ) {
					for ( Entry<UUID, ? extends StateExchange> ent : getDriverRegistry().getStates(type).entrySet() ) {
						for ( ValueLogger vlog : activeLoggers ) {
							vlog.log( currentTime, ent.getValue() );
						}
					}
				}
			} 
			else {
				for (Entry<UUID,List<String>> e : loggerUuidAndClassesToLogMap.entrySet()) {
					UUID uuid = e.getKey();
					for (String className : e.getValue()) {
						try {
							@SuppressWarnings("rawtypes")
							Class realClass = Class.forName(className);
							
							@SuppressWarnings("unchecked")
							StateExchange a = getDriverRegistry().getState(realClass, uuid);
							
							for( ValueLogger vlog : activeLoggers ) {
								vlog.log( currentTime, a );
							}
						}
						catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} /* for */
			} /* if( logAll )  */
		} /* if (updateNecessary) */
	}
	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		if (event instanceof LogThis) {
			
			LogThis logThis = (LogThis) event;
			
			// get content to log
			IAnnotatedForLogging toLog = logThis.getToLog();
			
			ArrayList<ValueLogger> activeLoggers = new ArrayList<>();
			
			long currentTime = getTimer().getUnixTime();
			
			if (consoleLog != null) {
				activeLoggers.add(consoleLog);
			}
			
			if (fileLog != null
					&& (currentTime - lastLoggingToFileAt) >= valueLoggerConfiguration.getValueLoggingToFileResolution()) {
				activeLoggers.add(fileLog);
			}
			
			if (databaseLog != null) {
				activeLoggers.add(databaseLog);
			}
			
			for ( ValueLogger vlog : activeLoggers ) {
				vlog.log( currentTime, toLog );
			}
		}
	}
}
