package osh.driver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PvDriver extends HALDeviceDriver {
	
	
	/**
	 * CONSTRUCTOR
	 * @throws OSHException
	 * @throws HALException 
	 */
	public PvDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
	}


	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {
		//NOTHING
	}

}
