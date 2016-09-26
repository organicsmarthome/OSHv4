package osh.cal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.UUID;

import osh.OSHComponent;
import osh.cal.exceptions.CALManagerException;
import osh.configuration.OSHParameterCollection;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.cal.CALConfiguration;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHOC;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class CALManager extends OSHComponent implements ILifeCycleListener {
	
	private CALConfiguration calConfig;
	private ArrayList<ComManager> connectedComManagers;

	public CALManager(IOSH theOrganicSmartHome) {
		super(theOrganicSmartHome);
		
		connectedComManagers = new ArrayList<ComManager>();
	}

	public void loadConfiguration(CALConfiguration calConfig) throws CALManagerException {
		this.calConfig = calConfig;
		
		getGlobalLogger().logInfo("...creating CAL-COM-devices...");
		this.processComDeviceConfiguration();
		getGlobalLogger().logInfo("...creating CAL-COM-devices... [DONE]");
		
		getGlobalLogger().logInfo("...CAL-layer is up!");
	}
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processComDeviceConfiguration() throws CALManagerException {
		for (int i = 0; i < this.calConfig.getAssignedComDevices().size(); i++){
			AssignedComDevice _device = this.calConfig.getAssignedComDevices().get(i);
			
			// load driver parameter
			OSHParameterCollection drvParams = new OSHParameterCollection();
			drvParams.loadCollection(_device.getComDriverParameters());
			
			// get the class of the driver an make an instance
			
			Class controllerClass = null;
			String controllerclassname = _device.getComManagerClassName();
			if (controllerclassname == null || controllerclassname.isEmpty()) {
				throw new CALManagerException("no com manager for driver " + _device.getComDeviceID() + " available.");
			}
			try {
				controllerClass = Class.forName(controllerclassname);
				}
			catch (ClassNotFoundException ex) {
				throw new CALManagerException(ex);
			}
			
			Class comDriverClass = null;
			try {
				comDriverClass = Class.forName(_device.getComDriverClassName());
			}
			catch (ClassNotFoundException ex) {
				throw new CALManagerException(ex);
			}
			
			ComManager _comManager = null;
			try {
				_comManager = (ComManager) controllerClass.getConstructor(
						IOSHOC.class, 
						UUID.class).newInstance(
								getOSH(), 
								UUID.fromString(_device.getComDeviceID()));
				
				getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getComDeviceID() + " - ComManager loaded ...... [OK]");
				
			}
			catch (Exception ex) {
				getGlobalLogger().logError("ERROR: initializing " + _device.getClass().getSimpleName() + " - UUID - " + _device.getComDeviceID() + " - ComManager loaded ...... [OK]");
				throw new CALManagerException(ex);
			}
			
			CALComDriver _comDriver = null;
			try {
				Constructor<CALComDriver> constructor = comDriverClass.getConstructor(
						IOSH.class, 
						UUID.class,
						OSHParameterCollection.class);
						
				_comDriver =  (CALComDriver) constructor.newInstance(
								getOSH(), 
								UUID.fromString(_device.getComDeviceID()),
								drvParams);
				getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getComDeviceID() + " - ComDriver loaded ...... [OK]");
			}
			catch (Exception ex) {
				throw new CALManagerException(ex);
			}
			
			_comManager.setOcDataSubscriber(_comDriver);
			_comDriver.setComDataSubscriber(_comManager);
			
			_comDriver.setComDeviceType(_device.getComDeviceType());
		
			connectedComManagers.add(_comManager);
			
		}
	}
	
	
	public ArrayList<ComManager> getConnectedComManagers() {
		return connectedComManagers;
	}
	
	/**
	 * get all members of the lifecycle-process. Used to trigger lifecycle-changes
	 * 
	 * @return
	 */
	private ArrayList<ILifeCycleListener> getLifeCycleMembers() {

		ArrayList<ILifeCycleListener> boxLifeCycleMembers = new ArrayList<ILifeCycleListener>();

		// com drivers
		for (ComManager comManager : this.connectedComManagers) {
			boxLifeCycleMembers.add(comManager);
			boxLifeCycleMembers.add(comManager.getComDriver());
		}

		return boxLifeCycleMembers;
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
