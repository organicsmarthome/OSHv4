package osh.eal.hal;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.BusDeviceTypes;
import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.oc.IOCHALDataSubscriber;
import osh.eal.hal.exchange.IHALExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class HALBusDriver extends HALDriver implements IDriverDataPublisher, IOCHALDataSubscriber {

	private BusManager assignedBusManager;
	private BusDeviceTypes busDeviceType;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public HALBusDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		// currently NOTHING
	}

	
	/**
	 * @return the assigned ComManager
	 */
	public BusManager getAssignedBusManager() {
		return assignedBusManager;
	}
	
	public void setBusDeviceType(BusDeviceTypes busDeviceType) {
		this.busDeviceType = busDeviceType;
	}

	public BusDeviceTypes getBusDeviceType() {
		return busDeviceType;
	}
	
	// HALdataObject
		/**
		 * receive data from BusManager
		 */
	@Override
	public void onDataFromOcComponent(IHALExchange exchangeObject)
			throws OSHException {
		updateDataFromBusManager(exchangeObject);
	}
	
	public abstract void updateDataFromBusManager(IHALExchange exchangeObject);

	// HALdataSubject
	
	@Override
	public final void setOcDataSubscriber(IDriverDataSubscriber monitorObject) {
		this.assignedBusManager = (BusManager) monitorObject;
	}
	
	@Override
	public final void removeOcDataSubscriber(IDriverDataSubscriber monitorObject) {
		this.assignedBusManager = null;
	}
	
	@Override
	public final void updateOcDataSubscriber(IHALExchange halexchange) {
		this.assignedBusManager.onDataFromCALDriver(halexchange);
	}
	
	public final void notifyBusManager(IHALExchange exchangeObject){
		updateOcDataSubscriber(exchangeObject);
	}

}
