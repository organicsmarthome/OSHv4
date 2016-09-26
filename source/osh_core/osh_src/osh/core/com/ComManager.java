package osh.core.com;

import java.util.UUID;

import osh.OSHComponent;
import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.cal.IComDataSubscriber;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.oc.IOCCALDataPublisher;
import osh.core.oc.IOCCALDataSubscriber;
import osh.registry.OCRegistry;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
public abstract class ComManager 
							extends OSHComponent 
							implements	IRealTimeSubscriber, 
										ILifeCycleListener, 
										IComDataSubscriber, 
										IOCCALDataPublisher {

	private CALComDriver comDriver;
	private UUID uuid;
	

	/**
	 * CONSTRUCTOR
	 * @param oc
	 * @param uuid
	 */
	public ComManager(IOSHOC oc, UUID uuid) {
		super(oc);
		this.uuid = uuid;
	}
	
	
	@Override
	public IOSHOC getOSH() {
		return (IOSHOC) super.getOSH();
	}
	

	@Override
	public OSHComponent getSyncObject() {
		return this;
	}	
	
	@Override
	public void setOcDataSubscriber(IOCCALDataSubscriber monitorObject) {
		this.comDriver = (CALComDriver) monitorObject;
	}

	@Override
	public void removeOcDataSubscriber(IOCCALDataSubscriber monitorObject) {
		this.comDriver = null;
	}
	
	public CALComDriver getComDriver() {
		return this.comDriver;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	
	@Override
	public void updateOcDataSubscriber(ICALExchange calexchange) throws OSHException {
		if (this.comDriver != null) {
			this.comDriver.onDataFromOcComponent(calexchange);
		}
		else {
			//NOTHING
			//TODO: error message/exception
			throw new OSHException("No ComDriver available.");
		}
	}
	
	/**
	 * the observer in the design pattern (called method)
	 */
	@Override
	public final void onDataFromCALDriver(ICALExchange exchangeObject) {
		synchronized(getSyncObject()) {
			onDriverUpdate(exchangeObject);
		}
	}
	
	public abstract void onDriverUpdate(ICALExchange exchangeObject);

	
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

	/**
	 * please avoid usage
	 * @return
	 */
	public UUID getGlobalOCUnitUUID() {
		return getOSH().getGlobalObserver().getAssignedOCUnit().getUnitID();
	}

	protected OCRegistry getOCRegistry() {
		return getOSH().getOCRegistry();
	}
}
