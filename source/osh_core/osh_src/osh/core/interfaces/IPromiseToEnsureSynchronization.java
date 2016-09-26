package osh.core.interfaces;


/**
 * This enforces that the programmer takes care of synchronization.<br>
 * <br>
 * Implement this interface to provide a synchronization object for a synchronization domain.
 * If you cross domains, you have to synchronize your calls and all associated and used objects
 * yourself, normally you use the registry for this stuff. So normally you should not have to implement
 * this interface.
 * 
 * @author Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public interface IPromiseToEnsureSynchronization {
	/**
	 * Must return a common synchronization object for callbacks like:
	 *  - {@link IRealTimeSubscriber}.onNextTimePeriod()
	 *  - {@link IEventReceiver}.onQueueEvent()
	 */
	public Object getSyncObject();
}
