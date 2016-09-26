package osh.mgmt.busmanager;

import java.util.UUID;

import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public abstract class LoggerBusManager extends BusManager implements IEventTypeReceiver {

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public LoggerBusManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		//NOTHING
	}

	@Override
	public void onDriverUpdate(IHALExchange exchangeObject) {
		//NOTHING
	}

}
