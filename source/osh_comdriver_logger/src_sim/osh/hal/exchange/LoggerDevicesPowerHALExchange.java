package osh.hal.exchange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exchange.HALExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class LoggerDevicesPowerHALExchange extends HALExchange {
	
	private HashMap<UUID,EnumMap<Commodity,Double>> powerStates;

	
	/**
	 * CONSTRUCTOR
	 */
	public LoggerDevicesPowerHALExchange(
			UUID deviceID, 
			Long timestamp, 
			HashMap<UUID,EnumMap<Commodity,Double>> powerStates) {
		super(deviceID, timestamp);
		
		this.powerStates = new HashMap<>();
		
		for (Entry<UUID,EnumMap<Commodity,Double>> e : powerStates.entrySet()) {
			EnumMap<Commodity,Double> current = new EnumMap<Commodity,Double>(Commodity.class);
			this.powerStates.put(e.getKey(), current);
			
			for (Entry<Commodity,Double> f: e.getValue().entrySet()) {
				current.put(f.getKey(), f.getValue());
			}
		}
	}

	
	public HashMap<UUID, EnumMap<Commodity, Double>> getPowerStates() {
		return powerStates;
	}
	
}
