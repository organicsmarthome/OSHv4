package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;

/**
 * Invoker thread for callback functions.
 * Which function is invoked under which condition is handled
 * via strategy pattern.
 * @see RealtimeSubscriberInvoker
 * 
 * @author Kaibin Bao
 *
 */
public class InvokerThread extends Thread {
	public InvokerEntry<?> entry;
	public IGlobalLogger log;
	private boolean dead = false;
	
	public InvokerThread(IGlobalLogger log, InvokerEntry<?> entry) throws OSHException {
		super();
		
		setName("InvokerThread for " + entry.getName());
		
		if (log == null) {
			throw new OSHException("CBGlobalLogger log == null");
		}
		this.log = log;
		this.entry = entry;
	}

	@Override
	public void run() {
		try {
			while ( !entry.shouldExit() ) {
				synchronized ( entry.getSyncObject() ) {
					// wait for invocation condition
					while ( !entry.shouldInvoke() ) {
						try {
							entry.getSyncObject().wait();
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
							log.logError("Thread interrupted. Should not happen! Will DIE now.", e);
							return;
						}
					}
				} /* synchronized */

				// invoke callback
				if ( InvokerThreadRegistry.invoke(entry, log, getName()) )
					break; // if a really bad error happens
			}
		} finally {
			//do this in any case
			dead = true;
			entry.threadDied();
		}
	}
	
	public boolean isDead() {
		return dead;
	}
}
