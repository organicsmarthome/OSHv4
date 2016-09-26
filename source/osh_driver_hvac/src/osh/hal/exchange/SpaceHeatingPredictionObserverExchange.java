package osh.hal.exchange;

import java.util.Map;
import java.util.UUID;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

/**
 * 
 * @author Jan Mueller
 *
 */
public class SpaceHeatingPredictionObserverExchange extends HALDeviceObserverExchange {

	private Map<Long, Double> predictedHeatConsumptionMap;

	public SpaceHeatingPredictionObserverExchange(UUID deviceID, Long timestamp,
			Map<Long, Double> predictedHeatConsumptionMap) {
		super(deviceID, timestamp);
		this.predictedHeatConsumptionMap = predictedHeatConsumptionMap;
	}

	public Map<Long, Double> getPredictedHeatConsumptionMap() {
		return predictedHeatConsumptionMap;
	}

}
