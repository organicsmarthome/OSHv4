package osh.hal.exchange;

import java.util.TreeMap;
import java.util.UUID;

import osh.cal.CALComExchange;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class GUIHotWaterPredictionComExchange extends CALComExchange {
	
	private TreeMap<Long, Double> predictedTankTemp = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<Long, Double>();

	public GUIHotWaterPredictionComExchange(UUID sender, 
			long timestamp, 
			TreeMap<Long, Double> predictedTankTemp,
			TreeMap<Long, Double> predictedHotWaterDemand,
			TreeMap<Long, Double> predictedHotWaterSupply) {
		super(sender, timestamp);
		this.predictedTankTemp.putAll(predictedTankTemp);
		this.predictedHotWaterDemand.putAll(predictedHotWaterDemand);
		this.predictedHotWaterSupply.putAll(predictedHotWaterSupply);
	}
	
	public TreeMap<Long, Double> getPredictedTankTemp() {
		return predictedTankTemp;
	}

	public TreeMap<Long, Double> getPredictedHotWaterDemand() {
		return predictedHotWaterDemand;
	}

	public TreeMap<Long, Double> getPredictedHotWaterSupply() {
		return predictedHotWaterSupply;
	}

}
