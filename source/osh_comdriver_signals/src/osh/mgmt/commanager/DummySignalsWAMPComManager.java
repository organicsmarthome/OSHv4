package osh.mgmt.commanager;

import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.hal.exchange.EpsComExchange;
import osh.hal.exchange.PlsComExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class DummySignalsWAMPComManager extends ComManager implements IHasState, IEventTypeReceiver{

	public DummySignalsWAMPComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();		
		
		getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
		
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		//NOTHING
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		if (event instanceof StateChangedExchange) {
			if (((StateChangedExchange) event).getType().equals(EpsStateExchange.class)) {					
				EpsStateExchange epse = getOCRegistry().getState(EpsStateExchange.class, ((StateChangedExchange) event).getStatefulentity());
				
				EpsComExchange ece = new EpsComExchange(getUUID(), getTimer().getUnixTime(), epse.getPriceSignals());
				this.updateOcDataSubscriber(ece);
			} else if (((StateChangedExchange) event).getType().equals(PlsStateExchange.class)) {					
				PlsStateExchange plse = getOCRegistry().getState(PlsStateExchange.class, ((StateChangedExchange) event).getStatefulentity());
				
				PlsComExchange pce = new PlsComExchange(getUUID(), getTimer().getUnixTime(), plse.getPowerLimitSignals());
				this.updateOcDataSubscriber(pce);
			}
		}
	}

}
