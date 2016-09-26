package osh.core.bus;

import java.util.UUID;

import osh.OSHComponent;
import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.oc.IOCHALDataPublisher;
import osh.core.oc.IOCHALDataSubscriber;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.IDriverDataSubscriber;
import osh.eal.hal.exchange.IHALExchange;

/**
 * 
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 *
 */
public abstract class BusManager extends OCComponent 
							implements	IRealTimeSubscriber, 
										ILifeCycleListener, 
										IDriverDataSubscriber, 
										IOCHALDataPublisher  {

	private HALBusDriver busDriver;
	private UUID uuid;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public BusManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox);
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
	public void setOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
		this.busDriver = (HALBusDriver) monitorObject;
	}

	@Override
	public void removeOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
		this.busDriver = null;
	}

	
	public HALBusDriver getBusDriver() {
		return this.busDriver;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	
	@Override
	public void updateOcDataSubscriber(IHALExchange halexchange) throws OSHException {
		if (this.busDriver != null) {
			this.busDriver.onDataFromOcComponent(halexchange);
		}
		else {
			//NOTHING
			//TODO: error message/exception
		}
	}

	@Override
	public final void onDataFromCALDriver(IHALExchange exchangeObject) {
		synchronized(getSyncObject()) {
			onDriverUpdate(exchangeObject);
		}
	}
	
	public abstract void onDriverUpdate(IHALExchange exchangeObject);

	@Override
	public void onSystemRunning() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemShutdown() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemHalt() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemResume() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemError() throws OSHException {
		//NOTHING
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		//NOTHING
	}

}
