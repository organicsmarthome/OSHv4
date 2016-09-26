package osh.hal.exchange;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exchange.HALExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class LoggerCommodityPowerHALExchange extends HALExchange {
	
	EnumMap<Commodity,Double> powerState;
	

	/**
	 * CONSTRUCTOR
	 */
	public LoggerCommodityPowerHALExchange(
			UUID deviceID, Long timestamp, 
			EnumMap<Commodity,Double> powerState) {
		super(deviceID, timestamp);
		
		this.powerState = new EnumMap<>(Commodity.class);
		for (Entry<Commodity,Double> e : powerState.entrySet()) {
			this.powerState.put(e.getKey(), e.getValue());
		}
	}


	public EnumMap<Commodity, Double> getPowerState() {
		return powerState;
	}

}
