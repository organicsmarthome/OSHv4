package osh.mgmt.mox;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.driver.chp.ChpOperationMode;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class DachsChpMOX implements IModelOfObservationExchange {
	
	// current values
	private double waterTemperature;
//	private INeededEnergy neededEnergy;
	
	private boolean running;
	private int remainingRunningTime;
	
	private int activePower;
	private int reactivePower;
	private int thermalPower;
	private int gasPower;
	
	// quasi static values
	private ChpOperationMode operationMode;
	private int typicalActivePower;
	private int typicalReactivePower;
	private int typicalGasPower;
	private int typicalThermalPower;
	
	private int rescheduleAfter;
	private long newIPPAfter;
	private int relativeHorizonIPP;
	private double currentHotWaterStorageMinTemp;
	private double currentHotWaterStorageMaxTemp;
	private double forcedOnHysteresis;
	
	private double fixedCostPerStart;	
	private double forcedOnOffStepMultiplier;
	private int forcedOffAdditionalCost;	
	private double chpOnCervisiaStepSizeMultiplier;
	private int minRuntime;
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public DachsChpMOX(double waterTemperature, 
			boolean running, 
			int remainingRunningTime, 
			int activePower,
			int reactivePower, 
			int thermalPower, 
			int gasPower, 
			ChpOperationMode operationMode, 
			int typicalActivePower,
			int typicalReactivePower, 
			int typicalGasPower, 
			int typicalThermalPower, 
			int rescheduleAfter, 
			long newIPPAfter,
			int relativeHorizonIPP,
			double currentHotWaterStorageMinTemp, 
			double currentHotWaterStorageMaxTemp, 
			double forcedOnHysteresis,
			double fixedCostPerStart,
			double forcedOnOffStepMultiplier,
			int forcedOffAdditionalCost,
			double chpOnCervisiaStepSizeMultiplier,
			int minRunTime,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super();
		
		this.waterTemperature = waterTemperature;
		this.running = running;
		this.remainingRunningTime = remainingRunningTime;
		this.activePower = activePower;
		this.reactivePower = reactivePower;
		this.thermalPower = thermalPower;
		this.gasPower = gasPower;
		
		this.operationMode = operationMode;
		this.typicalActivePower = typicalActivePower;
		this.typicalReactivePower = typicalReactivePower;
		this.typicalGasPower = typicalGasPower;
		this.typicalThermalPower = typicalThermalPower;
		
		this.rescheduleAfter = rescheduleAfter;
		this.newIPPAfter = newIPPAfter;
		this.relativeHorizonIPP = relativeHorizonIPP;
		this.currentHotWaterStorageMinTemp = currentHotWaterStorageMinTemp;
		this.currentHotWaterStorageMaxTemp = currentHotWaterStorageMaxTemp;
		this.forcedOnHysteresis = forcedOnHysteresis;
		
		this.fixedCostPerStart = fixedCostPerStart;		
		this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
		this.forcedOffAdditionalCost = forcedOffAdditionalCost;		
		this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;
		this.minRuntime = minRunTime;
		
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	}


	public double getWaterTemperature() {
		return waterTemperature;
	}

	public boolean isRunning() {
		return running;
	}

	public int getRemainingRunningTime() {
		return remainingRunningTime;
	}

	public int getActivePower() {
		return activePower;
	}
	
	public int getReactivePower() {
		return reactivePower;
	}

	public int getThermalPower() {
		return thermalPower;
	}

	public int getGasPower() {
		return gasPower;
	}
	
	public int getTypicalActivePower() {
		return typicalActivePower;
	}
	public int getTypicalReactivePower() {
		return typicalReactivePower;
	}
	public int getTypicalGasPower() {
		return typicalGasPower;
	}
	public int getTypicalThermalPower() {
		return typicalThermalPower;
	}
	public ChpOperationMode getOperationMode() {
		return operationMode;
	}

	public int getRescheduleAfter() {
		return rescheduleAfter;
	}


	public long getNewIPPAfter() {
		return newIPPAfter;
	}


	public int getRelativeHorizonIPP() {
		return relativeHorizonIPP;
	}

	public double getCurrentHotWaterStorageMinTemp() {
		return currentHotWaterStorageMinTemp;
	}


	public double getCurrentHotWaterStorageMaxTemp() {
		return currentHotWaterStorageMaxTemp;
	}


	public double getForcedOnHysteresis() {
		return forcedOnHysteresis;
	}


	public double getFixedCostPerStart() {
		return fixedCostPerStart;
	}


	public double getForcedOnOffStepMultiplier() {
		return forcedOnOffStepMultiplier;
	}


	public int getForcedOffAdditionalCost() {
		return forcedOffAdditionalCost;
	}


	public double getChpOnCervisiaStepSizeMultiplier() {
		return chpOnCervisiaStepSizeMultiplier;
	}


	public int getMinRuntime() {
		return minRuntime;
	}


	public void setMinRuntime(int minRuntime) {
		this.minRuntime = minRuntime;
	}


	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}


	public int getCompressionValue() {
		return compressionValue;
	}

//	public INeededEnergy getNeededEnergy() {
//		return neededEnergy;
//	}
	
}
