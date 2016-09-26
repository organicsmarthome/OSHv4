package osh.registry.interfaces;

import java.util.UUID;

import osh.core.interfaces.IPromiseToEnsureSynchronization;
import osh.core.interfaces.IQueueEventTypeSubscriber;


public interface IEventTypeReceiver extends IQueueEventTypeSubscriber, IPromiseToEnsureSynchronization {
	
	/** return null if the element has no UUID, but it won't be able to receive commands */
	public UUID getUUID();
	
}
