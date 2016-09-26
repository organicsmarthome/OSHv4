package osh.hal.exchange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author inspired by Ingo Mauser
 *
 */
public class GUIStatesComExchange extends CALComExchange {

	private Set<Class<? extends StateExchange>> types;
	private Map<UUID, ? extends StateExchange> states;
	private Class<? extends StateExchange> driverstatetype;
	private boolean ocmode;

	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public GUIStatesComExchange(
			UUID deviceID, 
			Long timestamp,
			Set<Class<? extends StateExchange>> types,
			Map<UUID, ? extends StateExchange> states) {
		super(deviceID, timestamp);

		ocmode = true;
		if (types == null) {
			this.types = new HashSet<>();
		} else {
			synchronized ( types ) {
				@SuppressWarnings("unchecked")
				Class<? extends StateExchange>[] dte = (Class<? extends StateExchange>[]) types.toArray(new Class<?>[0]);

				Set<Class<? extends StateExchange>> clonedTypes = new HashSet<>();

				for (Class<? extends StateExchange> e : dte) {
					clonedTypes.add(e); //no cloning for class available
				}

				this.types = clonedTypes;
			}
		}

		if (states == null) {
			this.states = new HashMap<>();
		} else {
			synchronized ( states ) {
				@SuppressWarnings("unchecked")
				Entry<UUID, ? extends StateExchange>[] dte = (Entry<UUID, ? extends StateExchange>[]) states.entrySet().toArray(new Entry<?, ?>[0]);

				Map<UUID, StateExchange> clonedStates = new HashMap<>();

				for (Entry<UUID, ? extends StateExchange> e : dte) {
					clonedStates.put(e.getKey(), e.getValue().clone()); //no cloning for key
				}

				this.states = clonedStates;
			}
		}
	}
	
	public GUIStatesComExchange(
			UUID deviceID, 
			Long timestamp,
			Class<? extends StateExchange> driverstatetype) {
		super(deviceID, timestamp);

		ocmode = false;
		this.driverstatetype = driverstatetype; //cloning not possible
	}
	
	public Set<Class<? extends StateExchange>> getTypes() {
		return types;
	}

	public Map<UUID, ? extends StateExchange> getStates() {
		return states;
	}

	public Class<? extends StateExchange> getDriverstatetype() {
		return driverstatetype;
	}

	public boolean isOcMode() {
		return ocmode;
	}

}
