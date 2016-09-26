package osh.mgmt.mox;

import java.util.Map;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class AdsorptionChillerMOX implements IModelOfObservationExchange {
	
	private double coldWaterTemperature;
	private double hotWaterTemperature;
	private boolean running;
	private int remainingRunningTime;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	private Map<Long, Double> temperatureMap;
	
	/**
	 * CONSTRUCTOR
	 */
	public AdsorptionChillerMOX(
			double coldWaterTemperature, 
			double hotWaterTemperature,
			boolean running, 
			int remainingRunningTime,
			Map<Long, Double> temperatureMap,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super();
		
		this.coldWaterTemperature = coldWaterTemperature;
		this.hotWaterTemperature = hotWaterTemperature;
		this.running = running;
		this.remainingRunningTime = remainingRunningTime;
		
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
		
		this.temperatureMap = temperatureMap;
	}


	public double getColdWaterTemperature() {
		return coldWaterTemperature;
	}

	public double getHotWaterTemperature() {
		return hotWaterTemperature;
	}
	
	public boolean isRunning() {
		return running;
	}

	public int getRemainingRunningTime() {
		return remainingRunningTime;
	}	
	
	public Map<Long, Double> getTemperatureMap() {
		return temperatureMap;
	}


	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}


	public int getCompressionValue() {
		return compressionValue;
	}
}