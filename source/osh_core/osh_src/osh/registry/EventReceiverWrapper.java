package osh.registry;

import java.util.UUID;

import osh.core.interfaces.IPromiseToEnsureSynchronization;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * FOR INTERNAL USE ONLY
 */
public class EventReceiverWrapper implements IPromiseToEnsureSynchronization {
	private EventReceiverType type;
	private IEventTypeReceiver eventtypereceiver = null;
	
	public EventReceiverWrapper(IEventTypeReceiver eventtypereceiver) {
		super();
		this.type = EventReceiverType.IEVENTTYPERECEIVER;
		this.eventtypereceiver = eventtypereceiver;
	}

	public EventReceiverType getType() {
		return type;
	}
	
	public IEventTypeReceiver getEventTypeReceiver() {
		return eventtypereceiver;
	}

	public UUID getUUID() {
		if (type == EventReceiverType.IEVENTTYPERECEIVER) {
			return eventtypereceiver.getUUID();
		} else {
			throw new NullPointerException("type is null");//should never happen
		}
	}
	
	public Object getSyncObject() {
		if (type == EventReceiverType.IEVENTTYPERECEIVER) {
			return eventtypereceiver.getSyncObject();
		} else {
			throw new NullPointerException("type is null");//should never happen
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (type == EventReceiverType.IEVENTTYPERECEIVER) {
			result = prime
					* result
					+ ((eventtypereceiver == null) ? 0 : eventtypereceiver
							.hashCode());
		}
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventReceiverWrapper other = (EventReceiverWrapper) obj;
		if (type == EventReceiverType.IEVENTTYPERECEIVER) {
			if (eventtypereceiver == null) {
				if (other.eventtypereceiver != null)
					return false;
			} else if (!eventtypereceiver.equals(other.eventtypereceiver))
				return false;
		}
		if (type != other.type)
			return false;
		return true;
	}

}