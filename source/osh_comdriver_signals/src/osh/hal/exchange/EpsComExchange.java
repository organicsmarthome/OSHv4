package osh.hal.exchange;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class EpsComExchange extends CALComExchange {

	 private EnumMap<AncillaryCommodity,PriceSignal> priceSignals;
	 private boolean causeScheduling = false;
	 
	 /**
	  * CONSTRUCTOR
	  * @param deviceID
	  * @param timestamp
	  * @param priceSignal
	  */
	 public EpsComExchange(
			 UUID deviceID, 
			 Long timestamp,
			 EnumMap<AncillaryCommodity,PriceSignal> priceSignals) {
		super(deviceID, timestamp);
		
		this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,PriceSignal> e : priceSignals.entrySet()) {
			this.priceSignals.put(e.getKey(), e.getValue().clone());
		}
	}
	 
	 public EpsComExchange(
			 UUID deviceID, 
			 Long timestamp,
			 Map<AncillaryCommodity,PriceSignal> priceSignals,
			 boolean causeScheduling) {
		super(deviceID, timestamp);
		
		this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
		this.causeScheduling = causeScheduling;
		
		for (Entry<AncillaryCommodity,PriceSignal> e : priceSignals.entrySet()) {
			this.priceSignals.put(e.getKey(), e.getValue().clone());
		}
	}
	 
	public boolean causeScheduling() {
		return causeScheduling;
	}


	public Map<AncillaryCommodity,PriceSignal> getPriceSignals() {
		return priceSignals;
	}

}
