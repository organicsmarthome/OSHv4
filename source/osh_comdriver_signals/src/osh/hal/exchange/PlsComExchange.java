package osh.hal.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PlsComExchange extends CALComExchange {

	 private Map<AncillaryCommodity,PowerLimitSignal> powerLimitSignals;
	 
	 /**
	  * CONSTRUCTOR
	  * @param deviceID
	  * @param timestamp
	  * @param priceSignal
	  */
	 public PlsComExchange(
			 UUID deviceID, 
			 Long timestamp,
			 Map<AncillaryCommodity,PowerLimitSignal> powerLimitSignals) {
		super(deviceID, timestamp);
		
		this.powerLimitSignals = new HashMap<>();
		
		for (Entry<AncillaryCommodity,PowerLimitSignal> e : powerLimitSignals.entrySet()) {
			this.powerLimitSignals.put(e.getKey(), e.getValue().clone());
		}
	}


	public Map<AncillaryCommodity,PowerLimitSignal> getPowerLimitSignals() {
		return powerLimitSignals;
	}

}
