package osh.mgmt.localcontroller;

import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.PvCommandExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PvLocalController extends LocalController implements IEventTypeReceiver {

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public PvLocalController(IOSHOC controllerbox) {
		super(controllerbox);
	}

	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
		
		getOCRegistry().register(PvCommandExchange.class, this);
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		PvCommandExchange _cmd = (PvCommandExchange) event;
		if (!_cmd.getReceiver().equals(getDeviceID())) return;
		
		PvControllerExchange _cx = new PvControllerExchange(
				this.getDeviceID(), 
				getTimer().getUnixTime(), 
				_cmd.getNewPvSwitchedOn(), 
				(int) Math.round(_cmd.getReactivePowerTargetValue()));
		this.updateOcDataSubscriber(_cx);
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}


}
