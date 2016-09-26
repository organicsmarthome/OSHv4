package osh.datatypes.registry.oc.state.globalobserver;

import java.util.TreeMap;
import java.util.UUID;

import osh.datatypes.registry.StateExchange;

public class GUIHotWaterPredictionStateExchange extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5935603463467532593L;
	
	private TreeMap<Long, Double> predictedTankTemp = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<Long, Double>();
	
	public GUIHotWaterPredictionStateExchange(
			UUID sender, 
			long timestamp, 
			TreeMap<Long, Double> predictedTankTemp,
			TreeMap<Long, Double> predictedHotWaterDemand,
			TreeMap<Long, Double> predictedHotWaterSupply) {
		super(sender, timestamp);
		this.predictedTankTemp = predictedTankTemp;
		this.predictedHotWaterDemand = predictedHotWaterDemand;
		this.predictedHotWaterSupply = predictedHotWaterSupply;
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
	
	@Override
	public GUIHotWaterPredictionStateExchange clone() {
		GUIHotWaterPredictionStateExchange copy = new GUIHotWaterPredictionStateExchange(
				this.sender,
				this.getTimestamp(),
				new TreeMap<Long, Double>(predictedTankTemp),
				new TreeMap<Long, Double>(predictedHotWaterDemand),
				new TreeMap<Long, Double>(predictedHotWaterSupply));
		//TODO: do proper cloning
		return copy;
	}

}
