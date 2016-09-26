package osh.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import osh.OSHComponent;
import osh.OSH;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.Exchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.registry.Registry;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * 
 * @author Sebastian Kramer, Ingo Mauser
 *
 */
public class DataBroker extends OSHComponent implements ILifeCycleListener, IEventTypeReceiver {

	private UUID uuid;

	private Map<Class<? extends Exchange>, List<UUIDRegistryPair>> dataMapping 
	= new HashMap<Class<? extends Exchange>, List<UUIDRegistryPair>>();

	private Registry comRegistry;
	private Registry ocRegistry;
	private Registry driverRegistry;

	public DataBroker(UUID uuid, IOSH theOrganicSmartHome) {
		super(theOrganicSmartHome);

		this.uuid = uuid;
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		OSH osh = (OSH) getOSH();
		comRegistry = osh.getComRegistry();
		ocRegistry = osh.getOCRegistry();
		driverRegistry = osh.getDriverRegistry();		
	}

	public void registerDataReachThroughState(UUID reciever, Class<? extends StateExchange> type, 
			RegistryType source, RegistryType drain) throws OSHException {

		List<UUIDRegistryPair> typeList = dataMapping.get(type);

		if (typeList == null) {
			typeList = new ArrayList<UUIDRegistryPair>();
			dataMapping.put(type, typeList);
		} else {
			if (!typeList.stream().allMatch(e -> e.source == source)) {
				throw new OSHException("data custodian does not support reachthrough "
						+ "for multiple source registrys for the same stateExchange");
			}
		}
		typeList.add(new UUIDRegistryPair(reciever, drain, source));

		Registry toRegister = getRegistryFromType(source);

		try {
			toRegister.registerStateChangeListener(type, this);
		}  catch (OSHException e) {
			// nop. happens.
			getGlobalLogger().logError("should not happen", e);
		}
	}

	public void registerDataReachThroughEvent(UUID reciever, Class<? extends EventExchange> type, 
			RegistryType source, RegistryType drain) {

		List<UUIDRegistryPair> typeList = dataMapping.get(type);

		if (typeList == null) {
			typeList = new ArrayList<UUIDRegistryPair>();
			dataMapping.put(type, typeList);
		}
		typeList.add(new UUIDRegistryPair(reciever, drain, source));

		Registry toRegister = getRegistryFromType(source);

		try {
			toRegister.register(type, this);
		}  catch (OSHException e) {
			// nop. happens.
			getGlobalLogger().logError("should not happen", e);
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {

		if (event instanceof StateChangedExchange) {

			Class<? extends StateExchange> stateType = ((StateChangedExchange) event).getType();
			
			List<UUIDRegistryPair> listForType = dataMapping.get(stateType);

			if (listForType != null && !listForType.isEmpty()) {

				for(UUIDRegistryPair pair : listForType) {					

					Object state = getRegistryFromType(pair.source).getState(stateType, ((StateChangedExchange) event).getStatefulentity());

					StateExchange test = stateType.cast(state);
					test.setSender(pair.identifier);

					getRegistryFromType(pair.drain).setStateOfSender(stateType.asSubclass(StateExchange.class), test);

					//					getRegistryFromType(pair.drain).setStateOfSender(stateType, stateType.cast(test));
				}
			}
		} else {				
			List<UUIDRegistryPair> listForType = dataMapping.get(type);

			if (listForType != null && !listForType.isEmpty()) {

				for(UUIDRegistryPair pair : listForType) {

					T ex = (T) event.clone();
					ex.setSender(pair.identifier);
					getRegistryFromType(pair.drain).sendEvent(type, ex);

				}		
			}
		}			
	}		


	private Registry getRegistryFromType(RegistryType type) {
		switch(type) {
		case COM: return comRegistry;
		case OC: return ocRegistry;
		case DRIVER: return driverRegistry;
		default: return null;
		}
	}

	@Override
	public Object getSyncObject() {
		return this;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public void onSystemRunning() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemShutdown() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemHalt() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemResume() throws OSHException {
		//NOTHING
	}

	@Override
	public void onSystemError() throws OSHException {
		//NOTHING
	}

	private class UUIDRegistryPair {
		public UUID identifier;
		public RegistryType drain;
		public RegistryType source;
		public UUIDRegistryPair(UUID identifier, RegistryType type, RegistryType source) {
			this.identifier = identifier;
			this.drain = type;
			this.source = source;
		}
	}
}
