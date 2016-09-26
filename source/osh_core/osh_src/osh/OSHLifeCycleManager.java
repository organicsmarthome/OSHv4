package osh;

import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import osh.cal.CALManager;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.cal.CALConfiguration;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.OSHConfiguration;
import osh.core.DataBroker;
import osh.core.LifeCycleStates;
import osh.core.OCManager;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.LifeCycleManagerException;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.core.logging.OSHGlobalLogger;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.eal.hal.HALManager;
import osh.eal.hal.exceptions.HALManagerException;
import osh.registry.ComRegistry;
import osh.registry.DriverRegistry;
import osh.registry.OCRegistry;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.OSHSimulationResults;
import osh.simulation.SimulationEngine;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.xml.XMLSerialization;

public class OSHLifeCycleManager {
	
	private OSH theOrganicSmartHome;
	
	private OCManager ocManager;
	private HALManager ealManager;
	private CALManager calManager;

	private DataBroker dataBroker;
	
	private SimulationEngine simEngine = null;
	private boolean hasSimEngine = false;
	private ScreenplayType screenPlayType;
	
	private LifeCycleStates currentState;
	
	private IGlobalLogger globalLogger;
	
	
	/**
	 * OSH will be instantiated in this class with external logger
	 */
	public OSHLifeCycleManager(IGlobalLogger globalLogger) {
		this(new OSH(), globalLogger);
	}
	
	/**
	 * external OSH with external logger
	 */
	public OSHLifeCycleManager(OSH theOrganicSmartHome, IGlobalLogger globalLogger) {
		this.theOrganicSmartHome = theOrganicSmartHome;
		this.globalLogger = globalLogger;
		
		this.theOrganicSmartHome.setLogger(globalLogger);
	}
	
	/** 
	 * OSH and logger will be instantiated in this class
	 */
	public OSHLifeCycleManager(SystemLoggerConfiguration systemLoggerConfiguration) {
		this(new OSH(), systemLoggerConfiguration, false);
	}

	/**
	 * external OSH with logger instantiated in this class
	 */
	public OSHLifeCycleManager(
			OSH theOrganicSmartHome,
			SystemLoggerConfiguration systemLoggerConfiguration) {

		this(theOrganicSmartHome, systemLoggerConfiguration, false);
	}
	
	/** 
	 * additional flag for the instantiation of the global logger
	 */
	public OSHLifeCycleManager(
			OSH theOrganicSmartHome,
			SystemLoggerConfiguration systemLoggerConfiguration,
			boolean dontInitLogger) {

		this.theOrganicSmartHome = theOrganicSmartHome;
		
		// create a new Logger with default LogLevel: error
		globalLogger = new OSHGlobalLogger(theOrganicSmartHome, systemLoggerConfiguration, dontInitLogger);
		this.theOrganicSmartHome.setLogger(globalLogger);
	}
	
	/**
	 * Initialise the Organic Smart Home based on the given configuration files,
	 * 	- without a SimEngine (real)
	 * 	- with a ComRegistry instantiated here
	 */
	public void initRealOSHFirstStep(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir) throws LifeCycleManagerException {
		
		hasSimEngine = false;
		ComRegistry comRegistry = new ComRegistry(theOrganicSmartHome);
		
		initOSHReadInFiles(
				oshConfigFile, 
				ocConfigFile, 
				ealConfigFile, 
				calConfigFile, 
				hostTimeZone, 
				forcedStartTime, 
				randomSeed, 
				optimizationMainRandomSeed, 
				runID, 
				configurationID, 
				logDir, 
				comRegistry);
	}
	
	/**
	 * Initialise the Organic Smart Home based on the given configuration files,
	 * 	- without a SimEngine (real)
	 * 	- with an external ComRegistry
	 */
	public void initRealOSHFirstStep(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir,
			ComRegistry comRegistry) throws LifeCycleManagerException {
		
		hasSimEngine = false;
		
		initOSHReadInFiles(
				oshConfigFile, 
				ocConfigFile, 
				ealConfigFile, 
				calConfigFile, 
				hostTimeZone, 
				forcedStartTime, 
				randomSeed, 
				optimizationMainRandomSeed, 
				runID, 
				configurationID, 
				logDir, 
				comRegistry);
	}
	
	/**
	 * Initialise the Organic Smart Home based on the given configuration files,
	 * 	- with an external SimEngine
	 * 	- with an external ComRegistry
	 */
	public void initOSHFirstStep(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir,
			SimulationEngine simEngine,
			ComRegistry comRegistry) throws LifeCycleManagerException {
		
		this.simEngine = simEngine;
		this.hasSimEngine = true;
		
		initOSHReadInFiles(
				oshConfigFile, 
				ocConfigFile, 
				ealConfigFile, 
				calConfigFile, 
				hostTimeZone, 
				forcedStartTime, 
				randomSeed, 
				optimizationMainRandomSeed, 
				runID, 
				configurationID, 
				logDir, 
				comRegistry);
	}
	
	/**
	 * Initialise the Organic Smart Home based on the given configuration files,
	 * 	- with a SimEngine instantiated in the EAL-Manager
	 * 	- with a ComRegistry instantiated in this class
	 */
	public void initOSHFirstStep(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir,
			ScreenplayType screenPlayType) throws LifeCycleManagerException {
		
		this.hasSimEngine = true;
		this.screenPlayType = screenPlayType;
		ComRegistry comRegistry = new ComRegistry(theOrganicSmartHome);
		
		initOSHReadInFiles(
				oshConfigFile, 
				ocConfigFile, 
				ealConfigFile, 
				calConfigFile, 
				hostTimeZone, 
				forcedStartTime, 
				randomSeed, 
				optimizationMainRandomSeed, 
				runID, 
				configurationID, 
				logDir, 
				comRegistry);
	}
	
	/**
	 * Initialise the Organic Smart Home based on the given configuration files,
	 * 	- with a SimEngine instantiated in the EAL-Manager
	 * 	- with an external ComRegistry
	 */
	public void initOSHFirstStep(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir,
			ComRegistry comRegistry,
			ScreenplayType screenPlayType) throws LifeCycleManagerException {
		
		this.hasSimEngine = true;
		this.screenPlayType = screenPlayType;
		
		initOSHReadInFiles(
				oshConfigFile, 
				ocConfigFile, 
				ealConfigFile, 
				calConfigFile, 
				hostTimeZone, 
				forcedStartTime, 
				randomSeed, 
				optimizationMainRandomSeed, 
				runID, 
				configurationID, 
				logDir, 
				comRegistry);
	}
	
	/**
	 * initialize the Organic Smart Home based on the given configuration files,
	 * unmarshalls the configuration files
	 */
	private void initOSHReadInFiles(
			String oshConfigFile,
			String ocConfigFile,
			String ealConfigFile,
			String calConfigFile,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			Long optimizationMainRandomSeed,
			String runID,
			String configurationID,
			String logDir,
			ComRegistry comRegistry) throws LifeCycleManagerException {

		// load from files:
		OSHConfiguration oshConfig = null;
		try {
			oshConfig = (OSHConfiguration) XMLSerialization.file2Unmarshal(
					oshConfigFile, 
					OSHConfiguration.class);
		} catch (Exception ex) {
			globalLogger.logError("can't load OSH-configuration", ex);
			throw new LifeCycleManagerException(ex);
		}

		OCConfiguration ocConfig = null;
		try {
			ocConfig = (OCConfiguration) XMLSerialization.file2Unmarshal(
					ocConfigFile, 
					OCConfiguration.class);
		} catch (Exception ex) {
			globalLogger.logError("can't load OC-configuration", ex);
			throw new LifeCycleManagerException(ex);
		}

		EALConfiguration ealConfig = null;
		try {
			ealConfig = (EALConfiguration) XMLSerialization.file2Unmarshal(
					ealConfigFile, 
					EALConfiguration.class);
		} catch (Exception ex) {
			globalLogger.logError("can't load EAL-configuration", ex);
			throw new LifeCycleManagerException(ex);
		}

		CALConfiguration calConfig = null;
		try {
			calConfig = (CALConfiguration) XMLSerialization.file2Unmarshal(
					calConfigFile, 
					CALConfiguration.class);
		} catch (Exception ex) {
			globalLogger.logError("can't load CAL-configuration", ex);
			throw new LifeCycleManagerException(ex);
		}

		/*
		 * if a random seed is given from external it will override the random seed in the configuration package
		 */
		if (randomSeed == null && oshConfig.getRandomSeed() != null) {
			randomSeed = Long.valueOf(oshConfig.getRandomSeed());
		}
		if (randomSeed == null) {
			globalLogger
			.logError(
					"No randomSeed available: neither in OCConfig nor as Startup parameter - using default random seed!");
			globalLogger
			.printDebugMessage(
					"No randomSeed available: Using default seed \"0xd1ce5bL\"");
			randomSeed = 0xd1ce5bL;
		}
		
		oshConfig.setRandomSeed(randomSeed.toString());
		
		theOrganicSmartHome.setRandomGenerator(
				new OSHRandomGenerator(new Random(randomSeed)));
		globalLogger.logInfo("Using random seed 0x" + Long.toHexString(randomSeed));
		
		//assigning Registries

		// assign OCRegistry (O/C communication above HAL)
		OCRegistry ocRegistry = new OCRegistry(theOrganicSmartHome);
		theOrganicSmartHome.setOCRegistry(ocRegistry);

		// assign DriverRegistry (DeviceDriver, ComDriver and BusDriver communication below HAL)
		DriverRegistry driverRegistry = new DriverRegistry(theOrganicSmartHome);
		theOrganicSmartHome.setDriverRegistry(driverRegistry);
		
		// assign ComRegistry (communication to SignalProviders, REMS etc.)
		theOrganicSmartHome.setExternalRegistry(comRegistry);
		
		//instantiating the data broker and assiging it to the osh
		dataBroker = new DataBroker(UUID.randomUUID(), theOrganicSmartHome);
		theOrganicSmartHome.setDataBroker(dataBroker);
		
		initOSHSetupStatus(
				oshConfig,
				ocConfig,
				ealConfig,
				calConfig,
				hostTimeZone,
				forcedStartTime,
				optimizationMainRandomSeed,
				runID,
				configurationID,
				logDir);

	}
	
	private void initOSHSetupStatus(
			OSHConfiguration oshConfig, 
			OCConfiguration ocConfig,
			EALConfiguration ealConfig, 
			CALConfiguration calConfig, 
			TimeZone hostTimeZone, 
			long forcedStartTime,
			Long optimizationMainRandomSeed, 
			String runID, 
			String configurationID, 
			String logDir
			) throws LifeCycleManagerException {
		
		
		boolean hasGUI = false;
		
		// setting flag for GUI present
		for (AssignedComDevice comDev : calConfig.getAssignedComDevices()) {
			if (comDev.getComDeviceType().equals(ComDeviceTypes.GUI)) {
				hasGUI = true;
				break;
			}
		}		
		theOrganicSmartHome.getOSHstatusObj().setIsGUI(hasGUI);
		
		// set some status variables...
		switch (oshConfig.getRunningType()) {
			case SIMULATION: {
				theOrganicSmartHome.getOSHstatusObj().setIsSimulation(true);
				theOrganicSmartHome.getOSHstatusObj().setVirtual(false);
				break;
			}
			case REAL: {
				theOrganicSmartHome.getOSHstatusObj().setIsSimulation(false);
				theOrganicSmartHome.getOSHstatusObj().setVirtual(false);
				break;
			}
			case HIL: {
				theOrganicSmartHome.getOSHstatusObj().setIsSimulation(false);
				theOrganicSmartHome.getOSHstatusObj().setVirtual(true);
				break;
			}
		}		
		
		// info: record simulation is already set in the constructor
		theOrganicSmartHome.getOSHstatusObj().setRunID(runID);
		theOrganicSmartHome.getOSHstatusObj().setConfigurationID(configurationID);
		theOrganicSmartHome.getOSHstatusObj().setLogDir(logDir);
		theOrganicSmartHome.getOSHstatusObj().sethhUUID(UUID.fromString(oshConfig.getHhUUID()));	
		
		
		initOSHInstantiateManagers(
				oshConfig,
				ocConfig,
				ealConfig,
				calConfig,
				hostTimeZone,
				forcedStartTime,
				optimizationMainRandomSeed,
				logDir);
		
	}
	
	private void initOSHInstantiateManagers(
			OSHConfiguration oshConfig, 
			OCConfiguration ocConfig,
			EALConfiguration ealConfig, 
			CALConfiguration calconfig, 
			TimeZone hostTimeZone, 
			long forcedStartTime,
			Long optimizationMainRandomSeed, 
			String logDir) throws LifeCycleManagerException {
		
		ocManager = new OCManager(theOrganicSmartHome);
		ealManager = new HALManager(theOrganicSmartHome);
		calManager = new CALManager(theOrganicSmartHome);
		
		try {
			globalLogger.logInfo("...starting EAL-layer");
			
			
			if (hasSimEngine) {
				if (simEngine != null) {
					ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime, simEngine);
				} else {
					ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime, Long.valueOf(oshConfig.getRandomSeed()), oshConfig.getEngineParameters(), 
							screenPlayType, oshConfig.getGridConfigurations(), oshConfig.getMeterUUID());
					this.simEngine = ealManager.getSimEngine();
				}
			} else {
				ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime);
			}
		} 
		catch (Exception ex) {
			throw new LifeCycleManagerException(ex);
		}
		
		try {
			globalLogger.logInfo("...starting O/C-layer");
			ocManager.loadConfiguration(
					ocConfig, 
					oshConfig.getGridConfigurations(), 
					oshConfig.getMeterUUID(), 
					ealManager.getConnectedDevices(), 
					optimizationMainRandomSeed, 
					logDir);
		} 
		catch (Exception ex) {
			throw new LifeCycleManagerException(ex);
		}
		
		try {
			globalLogger.logInfo("...starting CAL-layer");
			calManager.loadConfiguration(calconfig);
		} 
		catch (Exception ex) {
			throw new LifeCycleManagerException(ex);
		}
		
		try {
			switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_IS_UP);
			globalLogger.logInfo("...Organic Smart Home is up!");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
			throw new LifeCycleManagerException(ex);
		}
		
		//start all component threads (do not start earlier, otherwise components
		//get callbacks before the system is up. This can lead to evil race conditions.
		theOrganicSmartHome.getComRegistry().startQueueProcessingThreads();
		theOrganicSmartHome.getOCRegistry().startQueueProcessingThreads();
		theOrganicSmartHome.getComRegistry().startQueueProcessingThreads();
		
		theOrganicSmartHome.getTimer().startTimerProcessingThreads();
	}

//	private void handleSimEngine(
//			String runID,
//			String configurationID,
//			String logDir) {
//		
//		//if sim engine is not from external, instantiate it now
//		if (simEngine == null) {
//
//			ISimulationActionLogger simlogger = null;
//			try {
//				File theDir = new File(logDir);
//
//				// if the directory does not exist, create it
//				if (!theDir.exists()) {
//					theDir.mkdir();  
//				}
//
//				simlogger = new ActionSimulationLogger( 
//						globalLogger, 
//						logDir + "/" + configurationID + "_" + oshConfiguration.getRandomSeed() + "_actionlog.mxml");
//			} 
//			catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//
//			// MINUTEWISE POWER LOGGER
//			PrintWriter powerWriter = null;
//			try {
//				powerWriter = new PrintWriter(new File("" + logDir + "/" + configurationID + "_" + oshConfiguration.getRandomSeed() + "_powerlog" + ".csv"));
//				powerWriter.println("currentTick" 
//						+ ";" + "currentActivePowerConsumption"
//						+ ";" + "currentActivePowerPv"
//						+ ";" + "currentActivePowerPvAutoConsumption" 
//						+ ";" + "currentActivePowerPvFeedIn"
//						+ ";" + "currentActivePowerChp"
//						+ ";" + "currentActivePowerChpAutoConsumption" 
//						+ ";" + "currentActivePowerChpFeedIn"
//						+ ";" + "currentActivePowerBatteryCharging"
//						+ ";" + "currentActivePowerBatteryDischarging"
//						+ ";" + "currentActivePowerBatteryAutoConsumption" 
//						+ ";" + "currentActivePowerBatteryFeedIn"
//						+ ";" + "currentActivePowerExternal"
//						+ ";" + "currentReactivePowerExternal" 
//						+ ";" + "currentGasPowerExternal"
//						+ ";" + "epsCosts"
//						+ ";" + "plsCosts"
//						+ ";" + "gasCosts"
//						+ ";" + "feedInCostsPV"
//						+ ";" + "feedInCostsCHP"
//						+ ";" + "autoConsumptionCosts"
//						+ ";" + "currentPvFeedInPrice");
//			} 
//			catch (FileNotFoundException e2) {
//				e2.printStackTrace();
//			}
//			
//			ArrayList<OSHComponent> allDrivers = new ArrayList<>();
//			
//			allDrivers.addAll(ealManager.getConnectedBusManagers());
//			allDrivers.addAll(ealManager.getConnectedDevices());
//
//			simEngine = new BuildingSimulationEngine(
//					allDrivers,
//					oshConfiguration.getEngineParameters(),
//					ocManager.getESC(),
//					currentScreenplayType,
//					simlogger,
//					powerWriter);
//
//			//assign time base
//			simEngine.assignTimerDriver(ealManager.getRealTimeDriver());
//		}
//		
//		//assign Com-Registry
//		simEngine.assignComRegistry(theOrganicSmartHome.getComRegistry());
//
//		//assign OC-Registry
//		simEngine.assignOCRegistry(theOrganicSmartHome.getOCRegistry());
//
//		//assign Driver-Registry
//		simEngine.assignDriverRegistry(theOrganicSmartHome.getDriverRegistry());
//	}
	
	
	public void switchToLifeCycleState(LifeCycleStates nextState) throws OSHException {
		this.currentState = nextState;
		switch (nextState)
		{
			case ON_SYSTEM_INIT:
			{
				//NOTHING
				break;
			}
			case ON_SYSTEM_RUNNING:
			{
				dataBroker.onSystemRunning();
				ocManager.onSystemRunning();
				ealManager.onSystemRunning();
				calManager.onSystemRunning();
				globalLogger.logInfo("...switching to SYSTEM_RUNNING");
				break;			
			}
			case ON_SYSTEM_IS_UP:
			{
				dataBroker.onSystemIsUp();
				ocManager.onSystemIsUp();
				ealManager.onSystemIsUp();
				calManager.onSystemIsUp();
				this.globalLogger.logInfo("...switching to SYSTEM_IS_UP");
				break;
			}
			case ON_SYSTEM_SHUTDOWN:
			{
				dataBroker.onSystemShutdown();
				ocManager.onSystemShutdown();
				ealManager.onSystemShutdown();
				calManager.onSystemShutdown();
				DatabaseLoggerThread.shutDown();
				this.globalLogger.logInfo("...switching to SYSTEM_SHUTDOWN");
				break;
			}
			case ON_SYSTEM_HALT:
			{
				dataBroker.onSystemHalt();
				ocManager.onSystemHalt();
				ealManager.onSystemHalt();
				calManager.onSystemHalt();
				this.globalLogger.logInfo("...switching to SYSTEM_HALT");
				break;
			}
			case ON_SYSTEM_RESUME:
			{
				dataBroker.onSystemResume();
				ocManager.onSystemResume();
				ealManager.onSystemResume();
				calManager.onSystemResume();
				this.globalLogger.logInfo("...switching to SYSTEM_RESUME");
				break;
			}
			case ON_SYSTEM_ERROR:
			{
				dataBroker.onSystemError();
				ocManager.onSystemError();
				ealManager.onSystemError();
				calManager.onSystemError();
				this.globalLogger.logInfo("...switching to SYSTEM_ERROR");
				break;
			}
		}
	}
	
	/**
	 * @return the currentState
	 */
	protected LifeCycleStates getCurrentState() {
		return currentState;
	}
	
	public SimulationEngine getsimEngine() {
		return simEngine;
	}
	
	public void loadScreenplay(String screenplayFileName) throws LifeCycleManagerException {
		if (ealManager instanceof HALManager) {
			try {
				((HALManager) ealManager).loadScreenplay(screenplayFileName);
			} catch (SimulationEngineException | HALManagerException e) {
				throw new LifeCycleManagerException(e);
			}
		} else {
			throw new LifeCycleManagerException("Unable to load Screenplay with this EAL");
		}
	}
	
	public void initDatabaseLogging(boolean isDatabaseLogging, String tableName, 
			long forcedStartTime, int[] databasesToLog) throws LifeCycleManagerException {
		if (isDatabaseLogging) {
			DatabaseLoggerThread.initLogger(tableName, 
					theOrganicSmartHome.getOSHstatus().getLogDir(), 
					forcedStartTime, 
					databasesToLog);
			
			if (ealManager instanceof HALManager) {
				try {
					((HALManager) ealManager).initDatabaseLogging();
				} catch (HALManagerException e) {
					throw new LifeCycleManagerException(e);
				}
			}				
		}
	}
	
	public OSHSimulationResults startSimulation(long simulationDuration) throws LifeCycleManagerException, OSHException {
		
		if (ealManager instanceof HALManager) {
			switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_RUNNING);
			try {
				globalLogger.logInfo("... sim started");
				OSHSimulationResults simResults = ((HALManager) ealManager).startSimulation(simulationDuration);
				globalLogger.logInfo("... sim stopped");
				return simResults;
			} catch (SimulationEngineException | HALManagerException e) {
				throw new LifeCycleManagerException(e);
			}
		} else {
			throw new LifeCycleManagerException("Unable to start simulation with this EAL");
		}
	}

}
