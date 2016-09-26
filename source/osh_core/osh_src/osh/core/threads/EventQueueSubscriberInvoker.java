package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.datatypes.registry.EventExchange;
import osh.registry.EventQueue;
import osh.registry.EventReceiverType;
import osh.registry.EventReceiverWrapper;
import osh.registry.ExchangeWrapper;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * Invokes a {@link IEventReceiver} when new events are available.
 * 
 * A concrete strategy in the strategy pattern.
 * 
 * @author Kaibin Bao
 *
 */
public class EventQueueSubscriberInvoker extends InvokerEntry<EventReceiverWrapper> {
	
	private EventQueue eventqueue;

	/* CONSTRUCTOR */
	public EventQueueSubscriberInvoker(EventReceiverWrapper eventQueueSubscriber,
			EventQueue eventqueue) {
		super(eventQueueSubscriber);
		
		this.eventqueue = eventqueue;
	}

	@Override
	public boolean shouldInvoke() {
		if( eventqueue.isEmpty() ) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * This function only exists because the compiler cannot infer the type parameters in this situation
	 */
	private <T extends EventExchange> void wildcardHelper(IEventTypeReceiver receiver, ExchangeWrapper<T> wrapper) throws OSHException {
		receiver.onQueueEventTypeReceived(wrapper.getType(), wrapper.getExchange());
	}
	

	@Override
	public void invoke() throws OSHException {
		ExchangeWrapper<? extends EventExchange> ex;
		while ((ex = eventqueue.getNext()) != null) {
			EventReceiverWrapper sub = getSubscriber();
			synchronized (sub.getSyncObject()) {
				if (sub.getType() == EventReceiverType.IEVENTTYPERECEIVER) {
					wildcardHelper(sub.getEventTypeReceiver(), ex);
				} else {
					throw new NullPointerException("type is null"); //cannot happen if you don't changed something
				}
			}
		}
	}
	
	@Override
	public Object getSyncObject() {
		return eventqueue;
	}

	@Override
	public String getName() {
		return "EventQueueSubscriberInvoker for " + getSubscriber().getClass().getName();
	}
	
	/* Delegate to eventQueueSubscriber for HashMap */
	
	@Override
	public int hashCode() {
		return getSubscriber().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( obj instanceof EventQueueSubscriberInvoker )
			return getSubscriber().equals(((EventQueueSubscriberInvoker) obj).getSubscriber());
		else
			return false;
	}
}
