package osh.driver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.driver.details.chp.ChpDriverDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.ChpObserverExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Ingo Mauser, Jan Müller
 *
 */
public abstract class ChpDriver 
						extends HALDeviceDriver 
						implements IEventTypeReceiver, IHasState {

	protected int minimumRuntime;
	protected int runtime;
	
	private boolean electricityRequest;
	private boolean heatingRequest;
	
	protected ChpDriverDetails chpDriverDetails = null;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws HALException 
	 */
	public ChpDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws HALException {
		super(controllerbox, deviceID, driverConfig);
		//NOTHING
	}

	
	protected abstract void sendPowerRequestToChp();
	
	public synchronized void processChpDetailsAndNotify(ChpDriverDetails chpDetails) {
		ChpObserverExchange _ox = new ChpObserverExchange(getDeviceID(), getTimer().getUnixTime());
		_ox.setActivePower((int) Math.round(chpDetails.getCurrentElectricalPower()));
		_ox.setThermalPower((int) Math.round(chpDetails.getCurrentThermalPower()));
		_ox.setElectricityRequest(electricityRequest);
		_ox.setHeatingRequest(heatingRequest);
		_ox.setMinRuntime(minimumRuntime);
		_ox.setMinRuntimeRemaining(getMinimumRuntimeRemaining());
		
		// Current state of CHP is sometimes not given to Observer directly
		if (_ox.getActivePower() != 0) {
			_ox.setRunning(true);
		}
		
		this.notifyObserver(_ox);
	}

	public int getMinimumRuntime() {
		return minimumRuntime;
	}

	protected void setMinimumRuntime(int minimumRuntime) {
		this.minimumRuntime = minimumRuntime;
	}

	public int getMinimumRuntimeRemaining() {
		int returValue = getMinimumRuntime() - getRuntime();
		if (returValue < 0) returValue = 0;
		return returValue;
	}
	
	public int getRuntime() {
		return runtime;
	}

	public boolean isElectricityRequest() {
		return electricityRequest;
	}

	protected void setElectricityRequest(boolean electricityRequest) {
		if ( chpDriverDetails != null ) {
			chpDriverDetails.setPowerGenerationRequest(electricityRequest);
			getDriverRegistry().setState(ChpDriverDetails.class, this, chpDriverDetails);			
		}

		this.electricityRequest = electricityRequest;
	}

	public boolean isHeatingRequest() {
		return heatingRequest;
	}

	protected void setHeatingRequest(boolean heatingRequest) {
		if ( chpDriverDetails != null ) {
			chpDriverDetails.setHeatingRequest(heatingRequest);
			getDriverRegistry().setState(ChpDriverDetails.class, this, chpDriverDetails);			
		}
		this.heatingRequest = heatingRequest;
	}

	public boolean isOperationRequest() {
		return heatingRequest || electricityRequest;
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
