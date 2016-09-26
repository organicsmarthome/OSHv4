package osh.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.threads.EventQueueSubscriberInvoker;
import osh.core.threads.InvokerThreadRegistry;
import osh.core.threads.exceptions.InvokerThreadException;
import osh.core.threads.exceptions.SubscriberNotFoundException;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.registry.interfaces.IRegistry;


/**
 * 
 * @author Till Schuberth, Florian Allerding, Ingo Mauser
 * 
 */
public abstract class Registry extends OSHComponent implements IRegistry {
	
	private Map<Class<? extends StateExchange>, Map<UUID, StateExchange>> states = new HashMap<Class<? extends StateExchange>, Map<UUID, StateExchange>>();
	private Map<Class<? extends StateExchange>, Set<EventReceiverWrapper>> stateListeners = new HashMap<Class<? extends StateExchange>, Set<EventReceiverWrapper>>();

	private Map<Class<? extends EventExchange>, Set<EventReceiverWrapper>> eventListeners = new HashMap<Class<? extends EventExchange>, Set<EventReceiverWrapper>>();

	private Map<EventReceiverWrapper, EventQueue> queues = new HashMap<EventReceiverWrapper, EventQueue>();
	private Map<EventReceiverWrapper, StateChangedEventSet> statechangedeventsets = new HashMap<>();

	private InvokerThreadRegistry invokerRegistry;

	
	/**
	 * CONSTRUCTOR
	 */
	public Registry(IOSH osh) {
		super(osh);
		invokerRegistry = new InvokerThreadRegistry(osh);
	}

	/**
	 * listener.onQueueEventReceived() is called
	 */
	public synchronized EventQueue register(
			Class<? extends EventExchange> type, IEventTypeReceiver subscriber)
			throws OSHException {
		return register(type, new EventReceiverWrapper(subscriber));
	}
	/**
	 * listener.onQueueEventReceived() is called
	 */
	private synchronized EventQueue register(
			Class<? extends EventExchange> type, EventReceiverWrapper subscriber)
			throws OSHException {
		if (type == null || subscriber == null)
			throw new IllegalArgumentException("argument is null");

		// add subscriber to the set of subscribers for this event type
		Set<EventReceiverWrapper> eventtypeSubscribers = eventListeners.get(type);
		if (eventtypeSubscribers == null) {
			eventtypeSubscribers = new HashSet<EventReceiverWrapper>();
			eventListeners.put(type, eventtypeSubscribers);
		}
		eventtypeSubscribers.add(subscriber);

		// create one queue for every subscriber
		EventQueue queue = queues.get(subscriber);
		if (queue == null) {
			queue = new EventQueue(getGlobalLogger(), "EventQueue for "
					+ subscriber.getUUID().toString());
			queues.put(subscriber, queue);
			invokerRegistry.addQueueSubscriber(subscriber, queue);
		}
		return queue;
	}

	public <T extends EventExchange, U extends T> void sendEvent(Class<T> type,
			U ex) {
		if (type == null || ex == null)
			throw new IllegalArgumentException("argument is null");

		sendEvent(type, ex, null);
	}

	public <T extends CommandExchange, U extends T> void sendCommand(
			Class<T> type, U ex) {
		if (type == null || ex == null)
			throw new IllegalArgumentException("argument is null");

		UUID receiver = ex.getReceiver();
		if (receiver == null)
			throw new NullPointerException("CommandExchange: receiver is null");

		sendEvent(type, ex, receiver);
	}

	private synchronized <T extends EventExchange, U extends T> void sendEvent(
			Class<T> type, U ex, UUID receiver) {
		Set<EventReceiverWrapper> listeners = eventListeners.get(type);
		if (listeners == null)
			return; // no listeners

		for (EventReceiverWrapper r : listeners) {
			if (receiver != null) {
				if (!r.getUUID().equals(receiver))
					continue; // not the receiver
			}
			EventQueue queue = queues.get(r);

			U exclone;
			try {
				//a cast to T should be sufficient, but if I
				//can't cast to U, cloning isn't implemented
				//properly anyway...
				exclone = (U) ex.clone();
			} catch (ClassCastException e){
				throw new RuntimeException("You didn't implement cloning properly in your EventExchange-subclass.", e);
			}
			enqueueAndNotify(queue, type, exclone, r);
		}
	}

	public synchronized <T extends StateExchange> T getState(
			Class<T> type,
			UUID stateprovider) {
		Map<UUID, StateExchange> map = states.get(type);
		if (map == null)
			return null;

		StateExchange state = map.get(stateprovider);
		if (state == null)
			return null;

		@SuppressWarnings("unchecked")
		T t = (T) (state.clone());

		return t;
	}

	public synchronized <T extends StateExchange> Map<UUID, T> getStates(
			Class<? extends T> type) {
		Map<UUID, StateExchange> map = states.get(type);
		if (map == null)
			return new HashMap<UUID, T>();

		Map<UUID, T> copy = new HashMap<UUID, T>();
		for (Entry<UUID, StateExchange> e : map.entrySet()) {
			UUID uuid = e.getKey();
			StateExchange ex = e.getValue();
			
			@SuppressWarnings("unchecked")
			T clone = (T) ex.clone();
			
			copy.put(uuid, clone);
		}
		return copy;
	}

	public Set<Class<? extends StateExchange>> getTypes() {
		Set<Class<? extends StateExchange>> types = new HashSet<Class<? extends StateExchange>>();

		synchronized (this) {
			types.addAll(states.keySet());
		}

		return types;
	}

	public synchronized <T extends StateExchange, U extends T> void setState(
			Class<T> type, 
			IHasState provider, 
			U state) {

		if (!provider.getUUID().equals(state.getSender()))
			throw new IllegalArgumentException(
					"provider uuid doesn't match sender uuid");

		setState(type, provider.getUUID(), state);
	}

	/**
	 * Set the state of an arbitrary object. !USE WITH CARE! This is used by bus
	 * drivers.
	 * 
	 * @param type
	 * @param state
	 */
	public synchronized <T extends StateExchange, U extends T> void setStateOfSender(
			Class<T> type, U state) {
		setState(type, state.getSender(), state);
	}

	private synchronized <T extends StateExchange, U extends T> void setState(
			Class<T> type, UUID uuid, U state) {
		Map<UUID, StateExchange> map = states.get(type);
		if (map == null) {
			map = new HashMap<UUID, StateExchange>();
			states.put(type, map);
		}
		map.put(uuid, state);

		// inform listeners
		Set<EventReceiverWrapper> listeners = stateListeners.get(type);
		// only one exchange for every listener needed, because StateChangedExchange not modifiable
		StateChangedExchange stChEx = new StateChangedExchange(
				getTimer().getUnixTime(), 
				type, 
				uuid);
		if (listeners != null) {
			for (EventReceiverWrapper r : listeners) {
				notifyStateChange(stChEx, r);
			}
		}
	}

	/**
	 * Registers a listener which is notified whenever a state (from any device)
	 * is changed. This is a legacy function, please use the new version with
	 * IEventTypeReceiver as listener
	 * 
	 * @param type
	 * @param listener
	 * @throws OSHException
	 */
	public synchronized void registerStateChangeListener(
			Class<? extends StateExchange> type, IEventTypeReceiver listener)
					throws OSHException {
		registerStateChangeListener(type, new EventReceiverWrapper(listener));
	}
	/**
	 * Registers a listener which is notified whenever a state (from any device)
	 * is changed. This is a legacy function, please use the new version with
	 * IEventTypeReceiver as listener
	 * 
	 * @param type
	 * @param listener
	 * @throws OSHException
	 */
	private synchronized void registerStateChangeListener(
			Class<? extends StateExchange> type, EventReceiverWrapper listener)
					throws OSHException {
		if (type == null || listener == null)
			throw new IllegalArgumentException("argument is null");
		Set<EventReceiverWrapper> listeners = stateListeners.get(type);
		if (listeners == null) {
			listeners = new HashSet<EventReceiverWrapper>();
			stateListeners.put(type, listeners);
		}
		listeners.add(listener);

		if (!statechangedeventsets.containsKey(listener)) {
			String name;
			if (listener.getUUID() == null)
				name = "StateChangeListenerQueue for "
						+ listener.getClass().getName();
			else
				name = "StateChangeListenerQueue for "
						+ listener.getUUID().toString();
			
			StateChangedEventSet eventset = new StateChangedEventSet(getGlobalLogger(), name);
			statechangedeventsets.put(listener, eventset);
			invokerRegistry.addStateSubscriber(listener, eventset);
		}

		// push all current states (may be optional)
		Map<UUID, StateExchange> map = states.get(type);
		if (map != null) {
			for (Entry<UUID, StateExchange> e : map.entrySet()) {
				long timestamp = getTimer().getUnixTime();
				notifyStateChange(
						new StateChangedExchange(timestamp, type, e.getKey()),
						listener);
			}
		}
	}

	/**
	 * enqueue ex in Queue queue and notify ComponentThread
	 */
	private <T extends EventExchange> void enqueueAndNotify(EventQueue queue, Class<T> eventtype, T ex,
			EventReceiverWrapper receiver) {

		queue.enqueue(eventtype, ex);

		try {
			invokerRegistry.invoke(receiver);
		} catch (SubscriberNotFoundException e) {
			getGlobalLogger().logWarning("receiver has not been found!", e);
		} catch (InvokerThreadException e) {
			getGlobalLogger().logError("thread exception", e);
		}
	}
	
	private void notifyStateChange(StateChangedExchange ex,
			EventReceiverWrapper receiver) {
		StateChangedEventSet eventset = statechangedeventsets.get(receiver);
		if( eventset != null ) {
			eventset.enqueue(ex);
		} else {
			getGlobalLogger().logError("event set of " + receiver + " not found!");
		}
		
		try {
			invokerRegistry.notifyStateSubscriber(receiver);
		} catch (SubscriberNotFoundException e) {
			getGlobalLogger().logWarning("receiver has not been found!", e);
		} catch (InvokerThreadException e) {
			getGlobalLogger().logError("thread exception", e);
		}
	}

	/**
	 * Let the {@link EventQueueSubscriberInvoker}s process all queues. Only
	 * during simulation, all queue processing is done after this call. Use this
	 * for the simulation engine.
	 * 
	 */
	public synchronized void flushAllQueues() {
		invokerRegistry.triggerInvokers();
	}

	/**
	 * 
	 * Use this for the simulation engine.
	 * 
	 * @return false iff there is at least one event in some queue
	 */
	public synchronized boolean areAllQueuesEmpty() {
		for (EventQueue queue : queues.values()) {
			if (!queue.isEmpty())
				return false;
		}

		return true;
	}

	// TODO: WE SHOULD SYNCHRONIZE with queues, not always with the complete
	// registry
	/**
	 * Use the returned object to perform one atomic operation consisting of
	 * multiple method calls. Example:
	 * 
	 * synchronized (registry.getSyncObject()) { Type bla =
	 * registry.getState(..); if (bla.func()) { registry.setState(...); } }
	 * 
	 * @return Object for synchronization
	 */
	public Object getSyncObject() {
		return this;
	}

	/**
	 * Starts threads of the internal invoker thread registry.
	 */
	public void startQueueProcessingThreads() {
		invokerRegistry.startThreads();
	}

	// FIXME: registry unregister functions
	// TODO: unregister functions unimplemented. Be careful, because you also have to
	// remove all queues that are not longer needed, otherwise they will get
	// filled and never be emptied.
}
