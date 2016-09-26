package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.datatypes.registry.StateChangedExchange;
import osh.registry.EventReceiverType;
import osh.registry.EventReceiverWrapper;
import osh.registry.StateChangedEventSet;

/**
 * Invokes a {@link IEventReceiver} when new events are available.
 * 
 * A concrete strategy in the strategy pattern.
 * 
 * @author Kaibin Bao
 *
 */
public class StateSubscriberInvoker extends InvokerEntry<EventReceiverWrapper> {
	
	private StateChangedEventSet eventset;

	/* CONSTRUCTOR */
	public StateSubscriberInvoker(EventReceiverWrapper eventQueueSubscriber,
			StateChangedEventSet eventset) {
		super(eventQueueSubscriber);
		
		this.eventset = eventset;
	}

	@Override
	public boolean shouldInvoke() {
		if( eventset.isEmpty() ) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void invoke() throws OSHException {
		StateChangedExchange ex;
		while ((ex = eventset.getNext()) != null) {
			EventReceiverWrapper receiver = getSubscriber();
			synchronized (receiver.getSyncObject()) {
				if (receiver.getType() == EventReceiverType.IEVENTTYPERECEIVER) {
					receiver.getEventTypeReceiver().onQueueEventTypeReceived(StateChangedExchange.class, ex);
				} else {
					throw new IllegalStateException("type is not known");
				}
			}
		}
	}
	
	@Override
	public Object getSyncObject() {
		return eventset;
	}

	@Override
	public String getName() {
		return "StateSubscriberInvoker for " + getSubscriber().getClass().getName();
	}
	
	/* Delegate to eventQueueSubscriber for HashMap */
	
	@Override
	public int hashCode() {
		return getSubscriber().hashCode() ^ 0x50000000;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( obj instanceof StateSubscriberInvoker )
			return getSubscriber().equals(((StateSubscriberInvoker) obj).getSubscriber());
		else
			return false;
	}
}
