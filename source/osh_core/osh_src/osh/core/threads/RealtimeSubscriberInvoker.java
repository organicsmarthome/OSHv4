package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.eal.hal.HALRealTimeDriver;

/**
 * Invokes a {@link IRealTimeSubscriber} when the time has come.
 * 
 * A concrete strategy in the strategy pattern.
 * 
 * @author Kaibin Bao
 *
 */
public class RealtimeSubscriberInvoker extends InvokerEntry<IRealTimeSubscriber> {
	private HALRealTimeDriver realTimeDriver;

	private long invokeInterval;
	private long lastInvokeTimestamp; //usually 0 (iff: 1.1.1970)

	/* CONSTRUCTOR */
	/**
	 * 
	 * @param realTimeSubscriber
	 * @param invokeInterval
	 * @param realTimeDriver
	 */
	public RealtimeSubscriberInvoker(
			IRealTimeSubscriber realTimeSubscriber,
			long invokeInterval, 
			HALRealTimeDriver realTimeDriver) {
		super(realTimeSubscriber);
		
		this.invokeInterval = invokeInterval;
		this.realTimeDriver = realTimeDriver;
		this.lastInvokeTimestamp = realTimeDriver.getUnixTime();
	}

	@Override
	public boolean shouldInvoke() {
		long now = realTimeDriver.getUnixTime();
		if( now >= (lastInvokeTimestamp + invokeInterval) )
			return true;
		else
			return false;
	}

	@Override
	public void invoke() throws OSHException {
		lastInvokeTimestamp = realTimeDriver.getUnixTime();
		synchronized (getSubscriber().getSyncObject()) {
			getSubscriber().onNextTimePeriod();
		}
	}

	@Override
	public String getName() {
		return "RealtimeSubscriberInvoker for " + getSubscriber().getClass().getName();
	}
	
	/* Delegate to realTimeSubscriber for HashMap */
	
	@Override
	public int hashCode() {
		return getSubscriber().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return getSubscriber().equals(obj);
	}
}
