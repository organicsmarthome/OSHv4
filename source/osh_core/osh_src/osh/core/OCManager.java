package osh.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import osh.OSH;
import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.GridConfig;
import osh.core.exceptions.OCManagerException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.logging.IGlobalLogger;
import osh.core.oc.GlobalController;
import osh.core.oc.GlobalOCUnit;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalController;
import osh.core.oc.LocalOCUnit;
import osh.core.oc.LocalObserver;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.OCEnergySimulationCore;

/**
 * for initialization an management of the Organic Smart Home
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth, Sebastian Kramer
 * 
 */
public class OCManager extends OSHComponent implements ILifeCycleListener {
	
	private OSHParameterCollection globalObserverParameterCollection;
	private OSHParameterCollection globalControllerParameterCollection;
	
	private GlobalOCUnit globalOCunit;

	private ArrayList<LocalOCUnit> localOCUnits;

	public OCManager(OSH theOrganicSmartHome) {
		super(theOrganicSmartHome);
	}
	
	@Override
	public IOSHOC getOSH() {
		return (IOSHOC) super.getOSH();
	}
	
	/**
	 * initialize the Organic Smart Home based on the given configuration objects
	 * 
	 * @param ealConfig
	 * @param ocConfig
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadConfiguration(
			OCConfiguration ocConfig,
			List<GridConfig> gridConfigurations,
			String meterUUID,
			List<HALDeviceDriver> deviceDrivers,
			Long optimizationMainRandomSeed,
			String logDir) throws OCManagerException {

		/*
		 * if a optimisation-random seed is given from external it will override the optimisation-random seed in the configuration package
		 */
		if (optimizationMainRandomSeed == null && ocConfig.getOptimizationMainRandomSeed() != null) {
			optimizationMainRandomSeed = Long.valueOf(ocConfig.getOptimizationMainRandomSeed());
		}
		if (optimizationMainRandomSeed == null) {
			getLogger()
					.logError(
							"No optimizationMainRandomSeed available: neither in OCConfig nor as Startup parameter - using default random seed!");
			getLogger()
					.printDebugMessage(
							"No optimizationMainRandomSeed available: Using default seed \"0xd1ce5bL\"");
			optimizationMainRandomSeed = 0xd1ce5bL;
		}
		
		ocConfig.setOptimizationMainRandomSeed(optimizationMainRandomSeed.toString());

		
		getLogger().logInfo("...initializing OC Manager of Organic Smart Home");
		
		// create OCEnergySimulationCore
		OCEnergySimulationCore ocESC;
		try {
			ocESC = new OCEnergySimulationCore(gridConfigurations, meterUUID);
		} catch (HALManagerException e) {
			throw new OCManagerException(e);
		}
		

		// create local OC-Unit connected with the specific HAL-driver
		localOCUnits = createLocalOCUnits(deviceDrivers);

		// load global o/c-unit
		ocConfig.getGlobalControllerClass();
		GlobalObserver globalObserver = null;
		GlobalController globalController = null;
		getLogger().logInfo("...creating global O/C-units");
		Class globalObserverClass = null;
		Class globalControllerClass = null;

		globalObserverParameterCollection = new OSHParameterCollection();
		globalObserverParameterCollection.loadCollection(ocConfig
				.getGlobalObserverParameters());

		globalControllerParameterCollection = new OSHParameterCollection();
		globalControllerParameterCollection.loadCollection(ocConfig
				.getGlobalControllerParameters());
		
		globalControllerParameterCollection.setParameter("optimizationMainRandomSeed", ocConfig.getOptimizationMainRandomSeed());

		try {
			globalObserverClass = Class.forName(ocConfig.getGlobalObserverClass());
			globalControllerClass = Class.forName(ocConfig.getGlobalControllerClass());
		} catch (Exception ex) {
			throw new OCManagerException(ex);
		}

		try {
			globalObserver = (GlobalObserver) globalObserverClass
					.getConstructor(IOSHOC.class,
							OSHParameterCollection.class).newInstance(
							getOSH(),
							globalObserverParameterCollection);
		} catch (Exception ex) {
			throw new OCManagerException(ex);
		}

		try {
			globalController = (GlobalController) globalControllerClass
					.getConstructor(IOSHOC.class,
							OSHParameterCollection.class,
							GAConfiguration.class,
							OCEnergySimulationCore.class).newInstance(
							getOSH(),
							globalControllerParameterCollection,
							ocConfig.getGaConfiguration(),
							ocESC);
		} catch (Exception ex) {
			throw new OCManagerException(ex);
		}
		((OSH) getOSH()).setGlobalObserver(globalObserver);
		((OSH) getOSH()).setGlobalController(globalController);
		
		
		// create global O/C-unit
		this.globalOCunit = new GlobalOCUnit(
				UUID.fromString(ocConfig.getGlobalOcUuid()),
				getOSH(), 
				globalObserver, 
				globalController);

		registerLocalUnits();
	}
	
	/**
	 * get all members of the lifecycle-process. Used to trigger lifecycle-changes
	 * 
	 * @return
	 */
	private ArrayList<ILifeCycleListener> getLifeCycleMembers() {

		ArrayList<ILifeCycleListener> boxLifeCycleMembers = new ArrayList<ILifeCycleListener>();

		// OC-units for device drivers
		for (LocalOCUnit localOCUnit : localOCUnits) {

			if (localOCUnit.localObserver != null) {
				boxLifeCycleMembers
						.add(localOCUnit.localObserver);
			}
			if (localOCUnit.localController != null) {
				boxLifeCycleMembers
						.add(localOCUnit.localController);
			}
		}

		boxLifeCycleMembers.add(globalOCunit.getObserver());
		boxLifeCycleMembers.add(globalOCunit.getController());

		return boxLifeCycleMembers;
	}

	/**
	 * creates local o/c-unit based on the driver information. Only for devices
	 * witch are at least "Observable" such an instance will be created
	 * 
	 * @param deviceDrivers
	 * @return
	 */
	private ArrayList<LocalOCUnit> createLocalOCUnits(
			List<HALDeviceDriver> deviceDrivers)
			throws OCManagerException {

		ArrayList<LocalOCUnit> _localOCUnits = new ArrayList<LocalOCUnit>();

		getLogger().logInfo("...creating local units");

		for (HALDeviceDriver deviceDriver : deviceDrivers) {

			// is this device able to be observed or controlled?
			// ...then build an o/c-unit
			// otherwise do nothing ;-)
			LocalObserver localObserver = null;

			if (deviceDriver.isObservable()) {
				// getting the class for the local oc unit
				try {
					localObserver = (LocalObserver) deviceDriver
							.getRequiredLocalObserverClass()
							.getConstructor(IOSHOC.class)
							.newInstance(getOSH());
				} catch (Exception ex) {
					throw new OCManagerException(ex);
				}

				LocalController localController = null;

				if (deviceDriver.isControllable()) {
					try {
						localController = (LocalController) deviceDriver
								.getRequiredLocalControllerClass()
								.getConstructor(IOSHOC.class)
								.newInstance(getOSH());
					} catch (Exception ex) {
						throw new OCManagerException(ex);
					}

				}
				// init localunit and refer the realtime module

				// UUID id = _driver.getDeviceID();

				LocalOCUnit _localOCUnit = new LocalOCUnit(
						getOSH(),
						deviceDriver.getDeviceID(), 
						localObserver,
						localController);

				// assign the type of the device an it's classification
				_localOCUnit.setDeviceType(deviceDriver.getDeviceType());
				_localOCUnit.setDeviceClassification(deviceDriver
						.getDeviceClassification());

				// register the local observer at the specific HAL-driver
				// (Observer Pattern/publish-subscribe)
				deviceDriver.setOcDataSubscriber(_localOCUnit.localObserver);

				// do the same with the local controller
				// Before check if the controller is available
				// (it's not available when the device is not controllable)
				if (_localOCUnit.localController != null) {
					_localOCUnit.localController.setOcDataSubscriber(deviceDriver);
				}

				// add the new unit
				_localOCUnits.add(_localOCUnit);
			}
		}

		return _localOCUnits;
	}
	
	/**
	 * register local o/c-units at the Organic Smart Home (global o/c-unit)
	 */
	private void registerLocalUnits() {
		for (LocalOCUnit _localunit : localOCUnits) {
			try {
				globalOCunit.registerLocalUnit(_localunit);
			} catch (OSHException e) {
				getGlobalLogger().logError("", e);
			}
		}
	}
	
	/**
	 * get the global logger => here the configuration of the logger can be done
	 * during the runtime.
	 * 
	 * @return
	 */
	public IGlobalLogger getLogger() {
		return getOSH().getLogger();
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
