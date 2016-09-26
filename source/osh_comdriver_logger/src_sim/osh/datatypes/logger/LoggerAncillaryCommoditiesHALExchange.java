package osh.datatypes.logger;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.eal.hal.exchange.HALExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class LoggerAncillaryCommoditiesHALExchange extends HALExchange {

	private EnumMap<AncillaryCommodity,Integer> map;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public LoggerAncillaryCommoditiesHALExchange(
			UUID deviceID, 
			Long timestamp,
			EnumMap<AncillaryCommodity,Integer> map) {
		super(deviceID, timestamp);
		
		this.map = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,Integer> e : map.entrySet()) {
			this.map.put(e.getKey(), e.getValue());
		}
	}

	
	public EnumMap<AncillaryCommodity, Integer> getMap() {
		return map;
	}

}
