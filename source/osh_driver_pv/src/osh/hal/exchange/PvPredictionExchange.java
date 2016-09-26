package osh.hal.exchange;

import java.util.List;
import java.util.UUID;

import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALObserverExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class PvPredictionExchange 
				extends HALObserverExchange {
	
	private List<SparseLoadProfile> powerPredicitions;
	private int pastDaysPrediction;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public PvPredictionExchange(UUID deviceID, Long timestamp, List<SparseLoadProfile> powerPredicitions, int pastDaysPrediction) {
		super(deviceID, timestamp);
		this.powerPredicitions = powerPredicitions;
		this.pastDaysPrediction = pastDaysPrediction;
	}
	
	public List<SparseLoadProfile> getPredicitons() {
		return powerPredicitions;
	}

	public int getPastDaysPrediction() {
		return pastDaysPrediction;
	}

}
