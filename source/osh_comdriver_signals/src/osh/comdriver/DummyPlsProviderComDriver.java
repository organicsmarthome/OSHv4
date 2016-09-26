package osh.comdriver;

import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class DummyPlsProviderComDriver extends CALComDriver  {

	public DummyPlsProviderComDriver(IOSH controllerbox, UUID deviceID, OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

}
