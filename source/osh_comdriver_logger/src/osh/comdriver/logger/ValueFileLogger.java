package osh.comdriver.logger;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import osh.core.exceptions.OSHException;


/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class ValueFileLogger extends ValueLogger {
	
	private String prefixForLogs;
	private String suffixForLogs;
	
	//logger
	//TODO make generic
	private Logger powerLogger;
	private Logger powerDetailsLogger;
	private Logger ancillaryCommodityPowerDetailsLogger;
	private Logger scheduleLogger;
	private Logger temperatureLogger;
	private Logger externalSignalLogger;
	private Logger costDetailedDetailsLogger;
	
	
	/**
	 * CONSTRUCTOR
	 * @param runID
	 * @param packageID
	 * @param configurationID
	 * @param isSimulation
	 */
	public ValueFileLogger(String runID, String packageID, String configurationID, boolean isSimulation) {
		super();
		
		initSimulationLogger(runID, packageID, configurationID);
	}
	

	private void initSimulationLogger(
			String runID,
			String packageID,
			String configurationID) {
		
		prefixForLogs = "logs/" + runID + "/valueLogs/" + packageID + "_" + configurationID;
		suffixForLogs = runID + ".csv";
		
		powerLogger = createFileLogger("Power");
		powerDetailsLogger = createFileLogger("PowerDetails");
		ancillaryCommodityPowerDetailsLogger = createFileLogger("AncillaryCommodityPowerDetails");
		scheduleLogger = createFileLogger("Schedule");
		temperatureLogger = createFileLogger("InhouseTemperatures");
		externalSignalLogger = createFileLogger("ExternalSignals");
		costDetailedDetailsLogger = createFileLogger("DetailedCosts");
	}
	
	
	private Logger createFileLogger(String name) {
		Logger simulationDataLogger = Logger.getLogger(name);
		FileAppender newfileAppender = null;
		try {
			newfileAppender = new FileAppender(
					new PatternLayout(), 
					prefixForLogs + "_" + name + suffixForLogs);
			newfileAppender.setName("logfileappender: " + name);
		}
		catch (IOException e1) {
			throw new RuntimeException("Exception in simulationLogger", e1);
		}
		simulationDataLogger.addAppender(newfileAppender);
		simulationDataLogger.setLevel(Level.INFO);
		
		return simulationDataLogger;
			
	}
	

	//TODO make generic
	public void logPower(String entryLine) {
		powerLogger.log(Level.INFO, entryLine);
	}
	
	public void logPowerDetails(String entryLine) {
		powerDetailsLogger.log(Level.INFO, entryLine);
	}
	
	public void logSchedule(String entryLine) {
		scheduleLogger.log(Level.INFO, entryLine);
	}
	
	public void logTemperature(String entryLine) {
		temperatureLogger.log(Level.INFO, entryLine);
	}
	
	public void logExternalSignals(String entryLine) {
		externalSignalLogger.log(Level.INFO, entryLine);
	}
	
	public void logAncillaryCommodityPowerDetails(String entryLine) {
		ancillaryCommodityPowerDetailsLogger.log(Level.INFO, entryLine);
	}
	
	public void logCostDetailed(String entryLine) {
		costDetailedDetailsLogger.log(Level.INFO, entryLine);
	}


	@Override
	public void log(long timestamp, Object entity) throws OSHException {
		// TODO do logging
	}

}
