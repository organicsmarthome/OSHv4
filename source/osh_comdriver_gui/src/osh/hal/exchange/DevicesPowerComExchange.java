package osh.hal.exchange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;


/**
 * 
 * @author Ingo Mauser, Florian Allerding
 *
 */
public class DevicesPowerComExchange extends CALComExchange {
	
	private HashMap<UUID,EnumMap<Commodity,Double>> powerStates;

	
	/**
	 * CONSTRUCTOR
	 */
	public DevicesPowerComExchange(UUID deviceID, Long timestamp, DevicesPowerStateExchange dpsex) {
		super(deviceID, timestamp);

		DevicesPowerStateExchange cloned = dpsex.clone();
		powerStates = cloned.getPowerStateMap();
	}


	public HashMap<UUID, EnumMap<Commodity, Double>> getPowerStates() {
		return powerStates;
	}

}
