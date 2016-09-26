package osh.cal;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.ComDeviceTypes;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSH;
import osh.core.oc.IOCCALDataSubscriber;

/**
 * Superclass for all ComDrivers (devices with ComManager but without full O/C-Unit)
 * 
 * @author Till Schuberth, Ingo Mauser
 */
public abstract class CALComDriver 
							extends CALDriver 
							implements IComDataPublisher, IOCCALDataSubscriber {

	private ComManager assignedComManager;
	private ComDeviceTypes comDeviceType;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public CALComDriver(
			IOSH controllerbox, 
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
	}
	
	/**
	 * @return the assigned ComManager
	 */
	public ComManager getAssignedComManager() {
		return assignedComManager;
	}

	public ComDeviceTypes getComDeviceType() {
		return comDeviceType;
	}

	public void setComDeviceType(ComDeviceTypes comDeviceType) {
		this.comDeviceType = comDeviceType;
	}

	// HALdataObject
	/**
	 * receive data from ComManager
	 */
	@Override
	public final void onDataFromOcComponent(ICALExchange exchangeObject) {
		updateDataFromComManager(exchangeObject);
	}
	
	public abstract void updateDataFromComManager(ICALExchange exchangeObject);
	
	// HALdataSubject
	
	@Override
	public final void setComDataSubscriber(IComDataSubscriber monitorObject) {
		this.assignedComManager = (ComManager) monitorObject;
	}
	
	@Override
	public final void removeComDataSubscriber(IComDataSubscriber monitorObject) {
		this.assignedComManager = null;
	}
	
	@Override
	public final void updateComDataSubscriber(ICALExchange halexchange) {
		this.assignedComManager.onDataFromCALDriver(halexchange);
	}
	
	public final void notifyComManager(ICALExchange exchangeObject){
		updateComDataSubscriber(exchangeObject);
	}

}
