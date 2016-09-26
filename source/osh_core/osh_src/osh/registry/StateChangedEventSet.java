package osh.registry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Kaibin Bao
 *
 */
public class StateChangedEventSet {

	public static final int MAXSIZE = 1024;
	public boolean overfull = false;
	
	private String name;
	private IGlobalLogger logger;
	private Set<StateChangedExchange> eventset = new HashSet<StateChangedExchange>();
	
	public StateChangedEventSet(IGlobalLogger logger) {
		this(logger, "anonymous");
	}
	
	public StateChangedEventSet(IGlobalLogger logger, String name) {
		if (logger == null) throw new NullPointerException("logger is null");
		this.logger = logger;
		this.name = name;
	}
	
	public synchronized void enqueue(StateChangedExchange ex) {
		eventset.add(ex);
		if (overfull == false && eventset.size() > MAXSIZE) {
			
			Class<? extends StateExchange> type = ex.getType();
			
			logger.logWarning(
					"Queue overfull for " 
					+ name 
					+  ", size > MAXSIZE (" 
					+ MAXSIZE 
					+ "). New event "
					+ ex.getClass().getName()
					+ " for "
					+ type
					+ " from " 
					+ ex.getSender());
			//throw away current
			//while (eventset.size() > MAXSIZE) eventset.poll();

			overfull = true;
		} else if ( overfull == true ) {
			logger.logWarning(
					"Queue for " + name + " normalized again.");
			overfull = false;
		}
	}
	
	public synchronized StateChangedExchange getNext() {
		Iterator<StateChangedExchange> first = eventset.iterator();
		if( first.hasNext() ) {
			StateChangedExchange ex = first.next();
			first.remove();
			return ex;
		} else {
			return null;
		}
	}
	
	public synchronized StateChangedExchange peekNext() {
		Iterator<StateChangedExchange> first = eventset.iterator();
		if( first.hasNext() ) {
			StateChangedExchange ex = first.next();
			return ex;
		} else {
			return null;
		}
	}
	
	public synchronized void removeFirst() {
		Iterator<StateChangedExchange> first = eventset.iterator();
		if( first.hasNext() ) {
			first.next();
			first.remove();
		}
	}
	
	public synchronized boolean isEmpty() {
		return eventset.isEmpty();
	}
	
}
