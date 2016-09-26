package osh.runsimulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import osh.OSHLifeCycleManager;
import osh.core.LifeCycleStates;
import osh.core.exceptions.LifeCycleManagerException;
import osh.core.exceptions.OSHException;
import osh.core.logging.OSHLoggerCore;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.OSHSimulationResults;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser, Florian Allerding, Sebastian Kramer
 *
 */
public class runSimulationPackage {

	/* ########################
	 * # System configuration #
	 * ########################	*/

	//for database logging (mysql)
	static protected boolean logToDatabase = false;
//	static protected boolean logToDatabase = true;
	static protected String tableName = "TABLE";
	/* in which databases to log, multiple entries will result in logging to multiple databases
	 * { } : none
	 *  1  : some server
	 *  2  : some other server
	 */
	static protected int[] databasesToLog = {};

	static protected String configFilesDir = null;
	static protected String logDirName = null;
	static protected String configFilesPath = "configfiles";
	static private long forcedStartTime; // 1.1.1970
	static protected int day = 1; // 1 = 1.	
	static protected int month = 1; // 7 = July
	static protected int year = 1970;

	static protected int simulationDuration = 3 * 86400; //simulate 31 days


	/* #########################
	 * # General configuration #
	 * ######################### */

	//	static private String configID = "oshsimconfig";
	static protected String[] configIDs = {
			"example",			
	};

	static Long[][] randomSeeds = { //[0]= scenario, [1]= EA
			{0xd1ce5bL, 0xd1ce5bL},
			//		{0xd1ce5cL, 0xd1ce5cL},
			//		{0xd1ce5dL, 0xd1ce5dL},
			//		{0xd1ce5eL, 0xd1ce5eL},
			//		{0xd1ce5fL, 0xd1ce5fL},
			//		
			//		{0xd1ce60L, 0xd1ce60L},
			//		{0xd1ce61L, 0xd1ce61L},
			//		{0xd1ce62L, 0xd1ce62L},
			//		{0xd1ce63L, 0xd1ce63L},
			//		{0xd1ce64L, 0xd1ce64L},
	};

	// logger for exceptions etc.
	/** Logger log level
	 * "DEBUG"  : nearly everything
	 * "INFO"   : only important stuff (default)
	 * "ERROR"  : errors only -> used for simulation recording
	 * "OFF"    : nothing */
	static private String globalLoggerLogLevel = "DEBUG";


	/* ########
	 * # MAIN #
	 * ########	*/
	public static void main(String[] args) {

		// reset starting time
		forcedStartTime = 0;
		try {
			XMLGregorianCalendar simulationStartTime = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(year, month, day, 0, 0, 0, 0, 0);
			//					.newXMLGregorianCalendar(year, month, day, 0, 0, 0, 0, 0);
			forcedStartTime = simulationStartTime.toGregorianCalendar().getTimeInMillis() / 1000L;
		} 
		catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

		// iterate all configs
		for (String configID: configIDs) {
			// iterate all random seeds
			for (Long[] seeds: randomSeeds) {
				long simStartTime = System.currentTimeMillis();
				String runID = "" + (simStartTime / 1000);

				Long randomSeed = seeds[0];
				Long optimizationMainRandomSeed = seeds[1];

				OSHSimulationResults simResults = null;

				if (logDirName == null) {
					logDirName = "logs/" + configID + "/" + runID;
				}

				SystemLoggerConfiguration systemLoggingConfiguration = new SystemLoggerConfiguration(
						globalLoggerLogLevel, 
						true, //systemLoggingToConsoleActive
						true, //systemLoggingToFileActive
						false, 
						true, 
						true,
						logDirName);

				File simulationFolder;

				if (configFilesDir == null) 
					simulationFolder = new File(configFilesPath + "/osh");
				else
					simulationFolder = new File(configFilesDir);

				if ( !simulationFolder.exists() ) {
					System.out.println("[ERROR] Simulation folder does not exist: " + simulationFolder.getAbsolutePath());
					System.exit(1);
				}

				System.out.println("[INFO] Simulation running from time " + forcedStartTime + " for " + simulationDuration + " ticks");

				ScreenplayType currentScreenplayType = ScreenplayType.DYNAMIC;


				String configrootPath = configFilesDir == null ? configFilesPath + "/osh/" + configID + "/" : configFilesDir + "/";

				String currentScreenplayFileName = configrootPath + "simulation/Screenplay.xml";
				String currentEalconfigFileName = configrootPath + "system/EALConfig.xml";
				String currentOCConfigFileName = configrootPath + "system/OCConfig.xml";
				String currentOSHConfigFileName = configrootPath + "system/OSHConfig.xml";
				String currentCALConfigFileName = configrootPath + "system/CALConfig.xml";

				File file1 = new File(currentScreenplayFileName);
				File file2 = new File(currentEalconfigFileName);
				File file3 = new File(currentOCConfigFileName);
				File file4 = new File(currentOSHConfigFileName);
				File file5 = new File(currentCALConfigFileName);

				// check if files exist
				if (!file1.exists() || !file2.exists() || !file3.exists()) {
					System.out.println("[ERROR] One ore more of the required files is missing");
					if (!file1.exists()) {
						System.out.println("[ERROR] Screenplay file is missing : " + currentScreenplayFileName);
					}
					if (!file2.exists()) {
						System.out.println("[ERROR] EALConfigFile is missing : " + currentEalconfigFileName);
					}
					if (!file3.exists()) {
						System.out.println("[ERROR] OCConfigFile is missing : " + currentOCConfigFileName);
					}
					if (!file4.exists()) {
						System.out.println("[ERROR] OSHConfigFile is missing : " + currentOSHConfigFileName);
					}
					if (!file5.exists()) {
						System.out.println("[ERROR] CALConfigFile is missing : " + currentCALConfigFileName);
					}
					return;
				}

				OSHLifeCycleManager lifeCycleManager = new OSHLifeCycleManager(systemLoggingConfiguration);

				try {
					lifeCycleManager.initOSHFirstStep(
							currentOSHConfigFileName, 
							currentOCConfigFileName, 
							currentEalconfigFileName, 
							currentCALConfigFileName, 
							TimeZone.getTimeZone("UTC"), 
							forcedStartTime, 
							randomSeed, 
							optimizationMainRandomSeed, 
							runID, 
							configID, 
							logDirName, 
							currentScreenplayType);
				} catch (LifeCycleManagerException e) {
					e.printStackTrace();
					System.exit(1);
				}

				//init database logger
				try {
					lifeCycleManager.initDatabaseLogging(logToDatabase, tableName, forcedStartTime, databasesToLog);
				} catch (LifeCycleManagerException e1) {
					e1.printStackTrace();
					System.exit(1);
				}

				long simFinishTime = 0L;

				try {
					lifeCycleManager.loadScreenplay(currentScreenplayFileName);
					simResults = lifeCycleManager.startSimulation(simulationDuration);

					simFinishTime = System.currentTimeMillis();
					if (logToDatabase) {
						DatabaseLoggerThread.enqueueSimResults(simResults, 0, simulationDuration - 1, (simFinishTime - simStartTime) / 1000);
					}
					lifeCycleManager.switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_SHUTDOWN);
				} catch (LifeCycleManagerException | OSHException e) {
					e.printStackTrace();
					System.exit(1);
				}

				OSHLoggerCore.removeAllAppenders();

				System.out.println("[main] Simulation runtime: " + (simFinishTime - simStartTime) / 1000 + " sec");

				try {
					String outputFileName = logDirName + "/" + configID + "_"
							+ randomSeed + "_simresults" + ".csv";
					if (simResults != null) {
						simResults.logCurrentStateToFile(new File(outputFileName), (simFinishTime - simStartTime) / 1000);					
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				logDirName = null;

			} //RANDOM SEEDS
		} //CONFIGS
	}
}
