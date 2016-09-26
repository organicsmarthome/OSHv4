package osh.busdriver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class WagoWAMPBusDriver extends HALBusDriver implements IEventTypeReceiver {

	
	
	public WagoWAMPBusDriver(IOSH controllerbox, UUID deviceID, OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		//NOTHING
	}
	

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		// NOTHING
	}
	
	@Override
	public void updateDataFromBusManager(IHALExchange exchangeObject) {
		// NOTHING
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
