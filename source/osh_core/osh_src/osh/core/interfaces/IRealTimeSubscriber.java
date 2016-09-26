package osh.core.interfaces;

import osh.core.exceptions.OSHException;

/**
 * Interface for the observer pattern between the realtimedriver
 * and any component on this framework. Each component can decide on it's own
 * if it'll be registered as time-observer...
 * @author Florian Allerding
 */
public interface IRealTimeSubscriber extends IPromiseToEnsureSynchronization {
	/**
	 * will be invoked when a decided time period is over
	 * when the component register itself on the timedriver you
	 * can choose the update frequency
	 * 
	 * WARNING: asynchronous invocation, don't forget synchronization!
	 */
	public void onNextTimePeriod() throws OSHException;

}
