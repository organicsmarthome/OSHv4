package osh.datatypes.logger;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.eal.hal.exchange.HALExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class LoggerDetailedCostsHALExchange extends HALExchange {

	private EnumMap<AncillaryCommodity,Integer> powerValueMap;
	
	private EnumMap<AncillaryCommodity,PriceSignal> ps;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public LoggerDetailedCostsHALExchange(
			UUID deviceID, 
			Long timestamp,
			EnumMap<AncillaryCommodity,Integer> map,
			EnumMap<AncillaryCommodity,PriceSignal> ps,
			EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit) {
		super(deviceID, timestamp);
		
		this.powerValueMap = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,Integer> e : map.entrySet()) {
			this.powerValueMap.put(e.getKey(), e.getValue());
		}
		
		this.ps = ps;
		this.pwrLimit = pwrLimit;
	}

	public EnumMap<AncillaryCommodity, Integer> getPowerValueMap() {
		return powerValueMap;
	}
	
	public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
		return ps;
	}

	public EnumMap<AncillaryCommodity,PowerLimitSignal> getPwrLimit() {
		return pwrLimit;
	}

}
