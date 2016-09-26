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
public class BaseloadPredictionExchange 
				extends HALObserverExchange {
	
	List<SparseLoadProfile> powerPredicitions;
	private float weightForOtherWeekday = 1.0f;
	private float weightForSameWeekday = 5.0f;
	private int usedDaysForPrediction = 14;
	
	
	/**
	 * CONSTRUCTOR 1
	 * @param deviceID
	 * @param timestamp
	 */
	public BaseloadPredictionExchange(UUID deviceID, Long timestamp, List<SparseLoadProfile> powerPredicitions,
			int usedDaysForPrediction, float weightForOtherWeekday, float weightForSameWeekday) {
		super(deviceID, timestamp);
		this.powerPredicitions = powerPredicitions;
		this.usedDaysForPrediction = usedDaysForPrediction;
		this.weightForOtherWeekday = weightForOtherWeekday;
		this.weightForSameWeekday = weightForSameWeekday;
	}
	
	public List<SparseLoadProfile> getPredicitons() {
		return powerPredicitions;
	}

	public float getWeightForOtherWeekday() {
		return weightForOtherWeekday;
	}
	
	public float getWeightForSameWeekday() {
		return weightForSameWeekday;
	}

	public int getUsedDaysForPrediction() {
		return usedDaysForPrediction;
	}

}
