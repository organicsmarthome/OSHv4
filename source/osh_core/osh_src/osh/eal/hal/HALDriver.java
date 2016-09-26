package osh.eal.hal;

import java.util.ArrayList;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHDriver;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.eal.EALDriver;
import osh.registry.DriverRegistry;
import osh.utils.uuid.UUIDLists;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class HALDriver extends EALDriver implements IRealTimeSubscriber, ILifeCycleListener {

	private final UUID deviceID;
	private OSHParameterCollection driverConfig;
	
	private DriverRegistry driverRegistry;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public HALDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox);
		
		this.deviceID = deviceID;
		this.driverConfig = driverConfig;
	}
	
	
	@Override
	protected IOSHDriver getOSH() {
		return (IOSHDriver) super.getOSH();
	}
	
	/**
	 * The UUID of the device.
	 * @return Device-UUID
	 */
	public UUID getDeviceID() {
		return deviceID;
	}
	
	protected DriverRegistry getDriverRegistry() {
		return driverRegistry;
	}
	
	/**
	 * @return the driverConfig
	 */
	public OSHParameterCollection getDriverConfig() {
		return driverConfig;
	}

	/**
	 * @param driverConfig the driverConfig to set
	 */
	public void setDriverConfig(OSHParameterCollection driverConfig) {
		this.driverConfig = driverConfig;
	}

	@Override
	public HALDriver getSyncObject() {
		return this;
	}

	@Override
	public void onSystemRunning() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemShutdown() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		this.driverRegistry = getOSH().getDriverRegistry();
		
		//...in case of use please override and implement things like:
//		getTimer().registerComponent(this, 1);
//		getDriverRegistry().registerStateChangeListener(ComDriverDetails.class, this);
	}

	@Override
	public void onSystemHalt() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemResume() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemError() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		//...in case of use please override
	}


	// HELPER METHODS
	
	protected ArrayList<UUID> parseUUIDArray(String parameter) throws OSHException {
		try {
			ArrayList<UUID> list = UUIDLists.parseUUIDArray(parameter);
			return list;
		} catch( IllegalArgumentException e ) {
			throw new OSHException(e);
		}
	}
	
}
