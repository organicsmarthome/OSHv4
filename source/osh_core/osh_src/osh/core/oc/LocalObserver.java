package osh.core.oc;

import java.util.UUID;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.HALRealTimeDriver;
import osh.eal.hal.IDriverDataSubscriber;
import osh.eal.hal.exchange.IHALExchange;

/**
 * Superclass for local observers
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 * 
 */
public abstract class LocalObserver extends Observer implements IDriverDataSubscriber {
	
	// the local O/C-Unit
	private LocalOCUnit assignedOCUnit;
	

	// current representation of the state from the observed device
	private volatile IHALExchange observerDataObject;
	

	/**
	 * CONSTRUCTOR
	 */
	public LocalObserver(IOSH osh) {
		super(osh);
	}

	
	protected void assignLocalOCUnit(LocalOCUnit localOCUnit) {
		this.assignedOCUnit = localOCUnit;
		// this.deviceID = this.assignedOCUnit.getDeviceID();
	}

	public LocalOCUnit getAssignedOCUnit() {
		return assignedOCUnit;
	}
	
	/**
	 * returns the local controllerUnit according to this observer
	 * @return
	 */
	public LocalController getLocalController() {
		return this.getAssignedOCUnit().localController;
	}

	public UUID getDeviceID() {
		return this.assignedOCUnit.getUnitID();
	}
	
	public DeviceTypes getDeviceType () {
		return this.assignedOCUnit.getDeviceType();
	}
	
	public DeviceClassification getDeviceClassification () {
		return this.assignedOCUnit.getDeviceClassification();
	}
	

	/**
	 * get the current representation of the state from the observed device
	 * @return current representation of the state from the observed device
	 */
	public IHALExchange getObserverDataObject() {
		return observerDataObject;
	}

	public HALRealTimeDriver getSystemTimer() {
		return getOSH().getTimer();
	}

	/**
	 * is invoked every time when the state of the device changes
	 */
	public abstract void onDeviceStateUpdate() throws OCUnitException;


	@Override
	public final void onDataFromCALDriver(IHALExchange exchangeObject) throws OCUnitException {
		// cast to the observer object
		synchronized (getSyncObject()) {
			this.observerDataObject = exchangeObject;
			this.onDeviceStateUpdate();
		}
	}
	
}
