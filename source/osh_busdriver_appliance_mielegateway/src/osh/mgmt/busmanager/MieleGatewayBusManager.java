package osh.mgmt.busmanager;

import java.util.UUID;

import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.eal.hal.exchange.IHALExchange;


/**
 * Dummy busdriver manager for future use
 * @author Ingo Mauser
 *
 */
public class MieleGatewayBusManager extends BusManager {

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public MieleGatewayBusManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
		// NOTHING
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
//		getTimer().registerComponent(this, 1);
		
//		this.ocRegistry.register(NAME.class, this);
//		this.ocRegistry.registerStateChangeListener(NAME.class, this);
	}

	@Override
	public void onDriverUpdate(IHALExchange exchangeObject) {
		// NOTHING
	}

}
