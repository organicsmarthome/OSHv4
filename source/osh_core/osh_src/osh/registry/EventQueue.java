package osh.registry;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
public class EventQueue {
	
	public static final int MAXSIZE = 1024;
	
	private String name;
	private IGlobalLogger logger;
	private Queue<ExchangeWrapper<? extends EventExchange>> queue = new ArrayDeque<>();
	
	public EventQueue(IGlobalLogger logger) {
		this(logger, "anonymous");
	}
	
	public EventQueue(IGlobalLogger logger, String name) {
		if (logger == null) throw new NullPointerException("logger is null");
		this.logger = logger;
		this.name = name;
	}
	
	public synchronized <T extends EventExchange, U extends T> void enqueue(Class<T> type, U ex) {
		queue.add(new ExchangeWrapper<T>(type, ex));
		if (queue.size() > MAXSIZE) {
			
			String append = "";
			if (StateChangedExchange.class.isAssignableFrom(type)) {
				UUID uuid = ((StateChangedExchange) ex).getStatefulentity();
				Class<? extends StateExchange> statetype = ((StateChangedExchange) ex).getType();
				append = " Statefulentity: " + uuid + ", state type: " + statetype;
			} else if (CommandExchange.class.isAssignableFrom(type)) {
				append = " Receiver: " + ((CommandExchange) ex).getReceiver();
			}
			
			logger.logError(
					"Queue overflow for " 
					+ name 
					+  ", size > MAXSIZE (" 
					+ MAXSIZE 
					+ "), throwing away old events. New event "
					+ ex.getClass().getName()
					+ " from " 
					+ ex.getSender()
					+ append);
			//throw away
			while (queue.size() > MAXSIZE) queue.poll();
		}
	}
	
	public synchronized ExchangeWrapper<? extends EventExchange> getNext() {
		return queue.poll();
	}
	
	public synchronized ExchangeWrapper<? extends EventExchange> peekNext() {
		return queue.peek();
	}
	
	public synchronized void removeFirst() {
		queue.poll();
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
}
