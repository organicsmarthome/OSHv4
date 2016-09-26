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
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class DummySchedulesWAMPComManager extends ComManager implements IHasState, IEventTypeReceiver{

	public DummySchedulesWAMPComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();		
		
		getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(GUIScheduleStateExchange.class, this);
		
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		//NOTHING
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		if (event instanceof StateChangedExchange) {
			StateChangedExchange exsc = (StateChangedExchange) event;
			
			if (exsc.getType().equals(GUIScheduleStateExchange.class)) {
				GUIScheduleStateExchange se = 
						(GUIScheduleStateExchange) getOCRegistry().getState(GUIScheduleStateExchange.class, exsc.getStatefulentity());
				GUIScheduleComExchange gsce = new GUIScheduleComExchange(
						getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
				updateOcDataSubscriber(gsce);
			}	
			else if (exsc.getType().equals(EpsStateExchange.class)) {
				EpsStateExchange eee = getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulentity());
				GUIEpsComExchange gece = new GUIEpsComExchange(getUUID(), eee.getTimestamp());
				gece.setPriceSignals(eee.getPriceSignals());
				updateOcDataSubscriber(gece);
			}
			else if (exsc.getType().equals(PlsStateExchange.class)) {
				PlsStateExchange pse = getOCRegistry().getState(PlsStateExchange.class, exsc.getStatefulentity());
				GUIPlsComExchange gpce = new GUIPlsComExchange(getUUID(), pse.getTimestamp());
				gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
				updateOcDataSubscriber(gpce);
			}
		} 	
	}

}
