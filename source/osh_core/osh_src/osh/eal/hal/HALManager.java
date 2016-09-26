package osh.eal.hal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import osh.OSH;
import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.eal.AssignedBusDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.GridConfig;
import osh.core.OSHRandomGenerator;
import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.core.oc.LocalObserver;
import osh.core.threads.InvokerThreadRegistry;
import osh.eal.EALManager;
import osh.eal.hal.exceptions.HALManagerException;
import osh.simulation.ActionSimulationLogger;
import osh.simulation.BuildingSimulationEngine;
import osh.simulation.ISimulationActionLogger;
import osh.simulation.OSHSimulationResults;
import osh.simulation.SimulationEngine;
import osh.simulation.energy.SimEnergySimulationCore;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.screenplay.ScreenplayType;

/**
 * Represents the manager of the HAL
 * 
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class HALManager extends EALManager implements ILifeCycleListener  {

	private EALConfiguration ealConfig;
	
	private ArrayList<HALDeviceDriver> connectedDevices;	
	private ArrayList<BusManager> connectedBusManagers;
	
	private SimulationEngine simEngine;
	
	/**
	 * CONSTRUCTOR
	 */
	public HALManager(OSH controllerbox) {
		super(controllerbox);
		
		connectedDevices = new ArrayList<HALDeviceDriver>();
		connectedBusManagers = new ArrayList<BusManager>();
	}
	

	public HALRealTimeDriver getRealTimeDriver() {
		return getOSH().getTimer();
	}

	public ArrayList<HALDeviceDriver> getConnectedDevices() {
		return connectedDevices;
	}

	// if you want to do 'real' things ;-), no SimEngine
	public void loadConfiguration(
			EALConfiguration ealConfig,
			TimeZone hostTimeZone,
			long forcedStartTime) throws HALManagerException {
	
		simEngine = null;
		loadFromConfiguration(ealConfig, hostTimeZone, forcedStartTime);
		
		getGlobalLogger().logInfo("...EAL-layer is up!");
	}
	
	//loading with external SimulationEngine
	public void loadConfiguration(
			EALConfiguration ealConfig,
			TimeZone hostTimeZone,
			long forcedStartTime,
			SimulationEngine simEngine) throws HALManagerException {
		
		this.simEngine = simEngine;
		loadFromConfiguration(ealConfig, hostTimeZone, forcedStartTime);
		initSimulationEngine();
		
		getGlobalLogger().logInfo("...EAL-layer is up!");
	}
	
	//building the SimulationEngine in this class
	public void loadConfiguration(
			EALConfiguration ealConfig,
			TimeZone hostTimeZone,
			long forcedStartTime,
			Long randomSeed,
			List<ConfigurationParameter> engineParameters,
			ScreenplayType currentScreenplayType,
			List<GridConfig> gridConfigurations,
			String meterUUID) throws HALManagerException {
		
		loadFromConfiguration(ealConfig, hostTimeZone, forcedStartTime);
		buildSimulationEngine(
				getOSH().getOSHstatus().getRunID(), 
				getOSH().getOSHstatus().getConfigurationID(), 
				getOSH().getOSHstatus().getLogDir(), 
				randomSeed, 
				engineParameters, 
				currentScreenplayType, 
				gridConfigurations, 
				meterUUID);
		initSimulationEngine();
		
		getGlobalLogger().logInfo("...EAL-layer is up!");
	}
	

	
	private void loadFromConfiguration(
			EALConfiguration ealConfig,
			TimeZone hostTimeZone,
			long forcedStartTime) throws HALManagerException {
		
		boolean isSimulation = getOSH().getOSHstatus().isSimulation();
		boolean runningVirtual = getOSH().getOSHstatus().isRunningVirtual();
		
		getGlobalLogger().logInfo("...loading EAL configuration");
		this.ealConfig = ealConfig;
		if (ealConfig == null) throw new NullPointerException("ealConfig is null");
		
		// init real time driver and set the mode
		HALRealTimeDriver realTimeDriver = new HALRealTimeDriver(
				getGlobalLogger(),
				hostTimeZone,
				isSimulation,
				runningVirtual,
				1,
				forcedStartTime);
		((OSH) getOSH()).setTimer(realTimeDriver);
		realTimeDriver.setThreadRegistry(new InvokerThreadRegistry((IOSH) getOSH()));

		getGlobalLogger().logInfo("...creating EAL-BUS-devices...");
		this.processBusDeviceConfiguration();
		getGlobalLogger().logInfo("...creating EAL-BUS-devices... [DONE]");

		getGlobalLogger().logInfo("...creating EAL-device-drivers");
		this.processDeviceDriverConfiguration(getOSH().getRandomGenerator());
		getGlobalLogger().logInfo("...creating EAL-device-drivers... [DONE]");
	}
	
	private void buildSimulationEngine(
			String runID,
			String configurationID,
			String logDir,
			Long randomSeed,
			List<ConfigurationParameter> engineParameters,
			ScreenplayType currentScreenplayType,
			List<GridConfig> gridConfigurations,
			String meterUUID) throws HALManagerException {
		
		getGlobalLogger().logInfo("...creating EAL-SimulationEngine...");
		
		ISimulationActionLogger simlogger = null;
		try {
			File theDir = new File(logDir);

			// if the directory does not exist, create it
			if (!theDir.exists()) {
				theDir.mkdir();  
			}

			simlogger = new ActionSimulationLogger( 
					getGlobalLogger(), 
					logDir + "/" + configurationID + "_" + randomSeed + "_actionlog.mxml");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// MINUTEWISE POWER LOGGER
		PrintWriter powerWriter = null;
		try {
			powerWriter = new PrintWriter(new File("" + logDir + "/" + configurationID + "_" + randomSeed + "_powerlog" + ".csv"));
			powerWriter.println("currentTick" 
					+ ";" + "currentActivePowerConsumption"
					+ ";" + "currentActivePowerPv"
					+ ";" + "currentActivePowerPvAutoConsumption" 
					+ ";" + "currentActivePowerPvFeedIn"
					+ ";" + "currentActivePowerChp"
					+ ";" + "currentActivePowerChpAutoConsumption" 
					+ ";" + "currentActivePowerChpFeedIn"
					+ ";" + "currentActivePowerBatteryCharging"
					+ ";" + "currentActivePowerBatteryDischarging"
					+ ";" + "currentActivePowerBatteryAutoConsumption" 
					+ ";" + "currentActivePowerBatteryFeedIn"
					+ ";" + "currentActivePowerExternal"
					+ ";" + "currentReactivePowerExternal" 
					+ ";" + "currentGasPowerExternal"
					+ ";" + "epsCosts"
					+ ";" + "plsCosts"
					+ ";" + "gasCosts"
					+ ";" + "feedInCostsPV"
					+ ";" + "feedInCostsCHP"
					+ ";" + "autoConsumptionCosts"
					+ ";" + "currentPvFeedInPrice");
		} 
		catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		
		ArrayList<OSHComponent> allDrivers = new ArrayList<>();
		
		allDrivers.addAll(connectedDevices);
		allDrivers.addAll(connectedBusManagers);

		SimEnergySimulationCore esc = new SimEnergySimulationCore(gridConfigurations, meterUUID);
		
		try {
			simEngine = new BuildingSimulationEngine(
					allDrivers,
					engineParameters,
					esc,
					currentScreenplayType,
					simlogger,
					powerWriter,
					getOSH().getOSHstatus().gethhUUID());
		} catch (SimulationEngineException e) {
			throw new HALManagerException(e);
		}

		//assign time base
		simEngine.assignTimerDriver(getRealTimeDriver());
		
		getGlobalLogger().logInfo("...creating EAL-SimulationEngine... [DONE]");
	}
	
	private void initSimulationEngine() {
		//assign Com-Registry
		simEngine.assignComRegistry(((OSH) getOSH()).getComRegistry());

		//assign OC-Registry
		simEngine.assignOCRegistry(((OSH) getOSH()).getOCRegistry());

		//assign Driver-Registry
		simEngine.assignDriverRegistry(((OSH) getOSH()).getDriverRegistry());
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processDeviceDriverConfiguration(OSHRandomGenerator halRandomGenerator) throws HALManagerException {
		
		for (int i = 0; i < this.ealConfig.getAssignedDevices().size(); i++) {

			AssignedDevice _device = this.ealConfig.getAssignedDevices().get(i);

			if( _device == null )
				throw new HALManagerException("configuration fail: assigned device is null!");
			
			// load driver parameter
			OSHParameterCollection drvParams = new OSHParameterCollection();
			drvParams.loadCollection(_device.getDriverParameters());

			// get the class of the driver an make an instance
			Class driverClass = null;
			try {
				driverClass = Class.forName(_device.getDriverClassName());
			} catch (ClassNotFoundException ex) {
				throw new HALManagerException(ex);
			}

			HALDeviceDriver _driver = null;
			try {
				Constructor<HALDeviceDriver> constructor = driverClass.getConstructor(
						IOSH.class, 
						UUID.class,
						OSHParameterCollection.class);
				_driver = (HALDeviceDriver) constructor.newInstance(
						getOSH(), 
						UUID.fromString(_device.getDeviceID()),
						drvParams);
				getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - Driver loaded ...... [OK]");
			}
			catch (InstantiationException iex) {
				throw new HALManagerException("Instantiation of " + driverClass + " failed!", iex);
			}
			catch (Exception ex) {
				throw new HALManagerException(ex);
			}

			_driver.setControllable(_device.isControllable());
			_driver.setObservable(_device.isObservable());
			_driver.setDeviceType(_device.getDeviceType());
			_driver.setDeviceClassification(_device.getDeviceClassification());
			// add driver to the list of connected devices
			connectedDevices.add(_driver);

			// assign the dispatcher
			//_driver.assignDispatcher(halDispatcher);

			// get the class to the controller and the observer and refer it for
			// the cbox-layer
			if (_device.isControllable()) {
				// ...the controller class
				String controllerClassName = _device.getAssignedLocalOCUnit()
						.getLocalControllerClassName();

				try {
					_driver
					.setRequiredLocalControllerClass((Class<LocalController>) Class
					.forName(controllerClassName));

				} catch (ClassNotFoundException ex) {
					throw new HALManagerException(ex);
				}
				catch (Exception ex) {
					throw new HALManagerException(ex);
				}
				getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - LocalController loaded ...... [OK]");
			}

			if (_device.isObservable()) {
				// ...and the observer class
				String observerClassName = _device.getAssignedLocalOCUnit()
						.getLocalObserverClassName();

				try {
					_driver
					.setRequiredLocalObserverClass((Class<LocalObserver>) Class
					.forName(observerClassName));
				} catch (ClassNotFoundException ex) {
					throw new HALManagerException(ex);
				}
				getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - LocalObserver loaded ...... [OK]");
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processBusDeviceConfiguration() throws HALManagerException {
		for (int i = 0; i < this.ealConfig.getAssignedBusDevices().size(); i++){
			AssignedBusDevice _device = this.ealConfig.getAssignedBusDevices().get(i);
			
			// load driver parameter
			OSHParameterCollection drvParams = new OSHParameterCollection();
			drvParams.loadCollection(_device.getBusDriverParameters());
			
			// get the class of the driver an make an instance
			
			Class controllerClass = null;
			String controllerclassname = _device.getBusManagerClassName();
			if (controllerclassname == null || controllerclassname.isEmpty()) {
				throw new HALManagerException("no com manager for driver " + _device.getBusDeviceID() + " available.");
			}
			try {
				controllerClass = Class.forName(controllerclassname);
			}
			catch (ClassNotFoundException ex) {
				throw new HALManagerException(ex);
			}
			
			Class busDriverClass = null;
			try {
				busDriverClass = Class.forName(_device.getBusDriverClassName());
			}
			catch (ClassNotFoundException ex) {
				throw new HALManagerException(ex);
			}
			
			BusManager _busManager = null;
			try {
				_busManager = (BusManager) controllerClass.getConstructor(
						IOSHOC.class, 
						UUID.class).newInstance(
								getOSH(), 
								UUID.fromString(_device.getBusDeviceID()));
				getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getBusDeviceID() + " - BusManager loaded ...... [OK]");
			}
			catch (Exception ex) {
				throw new HALManagerException(ex);
			}
			
			HALBusDriver _busDriver = null;
			try {
				Constructor<HALBusDriver> constructor = busDriverClass.getConstructor(
						IOSH.class, 
						UUID.class,
						OSHParameterCollection.class);
						
				_busDriver =  (HALBusDriver) constructor.newInstance(
								getOSH(), 
								UUID.fromString(_device.getBusDeviceID()),
								drvParams);
				getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getBusDeviceID() + " - BusDriver loaded ...... [OK]");
			}
			catch (Exception ex) {
				throw new HALManagerException(ex);
			}
			
			_busManager.setOcDataSubscriber(_busDriver);
			_busDriver.setOcDataSubscriber(_busManager);
			
			_busDriver.setBusDeviceType(_device.getBusDeviceType());
		
			connectedBusManagers.add(_busManager);
			
		}
	}
	
	/**
	 * get all members of the lifecycle-process. Used to trigger lifecycle-changes
	 * 
	 * @return
	 */
	private ArrayList<ILifeCycleListener> getLifeCycleMembers() {

		ArrayList<ILifeCycleListener> boxLifeCycleMembers = new ArrayList<ILifeCycleListener>();

		// device drivers
		for (HALDeviceDriver halDevicedriver : this.connectedDevices) {
			boxLifeCycleMembers.add(halDevicedriver);
		}

		// bus managers
		for (BusManager busManager : this.connectedBusManagers) {
			boxLifeCycleMembers.add(busManager);
			boxLifeCycleMembers.add(busManager.getBusDriver());
		}
		
		return boxLifeCycleMembers;
	}
	

//	public void startHAL() {
//		//TODO: why is nothing here?
//	}
//
//	public void addDevice(HALDeviceDriver driver, String deviceDescription) {
//		//TODO: why is nothing here?
//	}
//
//	public void removeDevice(HALDeviceDriver driver) {
//		//TODO: why is nothing here?
//	}
	
	public ArrayList<BusManager> getConnectedBusManagers() {
		return connectedBusManagers;
	}
	
	public SimulationEngine getSimEngine() {
		return simEngine;
	}
	
	public void initDatabaseLogging() throws HALManagerException {
		if (simEngine != null && simEngine instanceof BuildingSimulationEngine) {
			((BuildingSimulationEngine) simEngine).setDatabaseLogging();
		} else {
			throw new HALManagerException("Unable to initiate database logging with this SimulatioinEngine");
		}
	}
	
	public void loadScreenplay(String screenplayFileName) throws SimulationEngineException, HALManagerException {
		if (simEngine != null && simEngine instanceof BuildingSimulationEngine) {
			((BuildingSimulationEngine) simEngine).loadSingleScreenplayFromFile(screenplayFileName);
		} else {
			throw new HALManagerException("Unable to load Screenplay with this SimulatioinEngine");
		}
	}
	
	public OSHSimulationResults startSimulation(long simulationDuration) throws SimulationEngineException, HALManagerException {
		if (simEngine != null && simEngine instanceof BuildingSimulationEngine) {
			simEngine.notifySimulationIsUp();
			return (OSHSimulationResults) ((BuildingSimulationEngine) simEngine).runSimulation(simulationDuration);
		} else {
			throw new HALManagerException("Unable to load start simulation with this SimulatioinEngine");
		}
	}
	
	@Override
	public void onSystemRunning() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemRunning();
		}
	}

	@Override
	public void onSystemShutdown() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemShutdown();
		}
		
		if (simEngine != null) {
			((BuildingSimulationEngine) simEngine).shutdown();
		}
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemIsUp();
		}
	}

	@Override
	public void onSystemHalt() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemHalt();
		}
	}

	@Override
	public void onSystemResume() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemResume();
		}
	}

	@Override
	public void onSystemError() throws OSHException {
		for (ILifeCycleListener listener : getLifeCycleMembers()) {
			listener.onSystemError();
		}
	}
}
