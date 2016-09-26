package osh.hal.exchange;

import java.util.UUID;

import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.hal.interfaces.chp.IHALChpStaticDetails;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class ChpStaticDetailsObserverExchange extends HALDeviceObserverExchange
													implements IHALChpStaticDetails{	

	// ### IHALChpStaticDetails ###
	private int minRuntime;
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
	private UUID hotWaterTankUuid;
	
	private double fixedCostPerStart;	
	private double forcedOnOffStepMultiplier;
	private int forcedOffAdditionalCost;	
	private double chpOnCervisiaStepSizeMultiplier;
	
	public ChpStaticDetailsObserverExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	
	@Override
	public int getMinRuntime() {
		return minRuntime;
	}

	public void setMinRuntime(int minRuntime) {
		this.minRuntime = minRuntime;
	}
	
	public ChpOperationMode getOperationMode() {
		return operationMode;
	}
	
	public void setOperationMode(ChpOperationMode operationMode) {
		this.operationMode = operationMode;
	}
	
	@Override
	public int getTypicalActivePower() {
		return typicalActivePower;
	}
	
	public void setTypicalActivePower(int typicalActivePower) {
		this.typicalActivePower = typicalActivePower;
	}

	public int getTypicalReactivePower() {
		return typicalReactivePower;
	}

	public void setTypicalReactivePower(int typicalReactivePower) {
		this.typicalReactivePower = typicalReactivePower;
	}

	@Override
	public int getTypicalGasPower() {
		return typicalGasPower;
	}
	
	public void setTypicalGasPower(int typicalGasPower) {
		this.typicalGasPower = typicalGasPower;
	}

	@Override
	public int getTypicalThermalPower() {
		return typicalThermalPower;
	}
	
	public void setTypicalThermalPower(int typicalThermalPower) {
		this.typicalThermalPower = typicalThermalPower;
	}

	public int getRescheduleAfter() {
		return rescheduleAfter;
	}

	public void setRescheduleAfter(int rescheduleAfter) {
		this.rescheduleAfter = rescheduleAfter;
	}

	public long getNewIPPAfter() {
		return newIPPAfter;
	}

	public void setNewIPPAfter(long newIPPAfter) {
		this.newIPPAfter = newIPPAfter;
	}

	public int getRelativeHorizonIPP() {
		return relativeHorizonIPP;
	}

	public void setRelativeHorizonIPP(int relativeHorizonIPP) {
		this.relativeHorizonIPP = relativeHorizonIPP;
	}

	public double getCurrentHotWaterStorageMinTemp() {
		return currentHotWaterStorageMinTemp;
	}

	public void setCurrentHotWaterStorageMinTemp(double currentHotWaterStorageMinTemp) {
		this.currentHotWaterStorageMinTemp = currentHotWaterStorageMinTemp;
	}

	public double getCurrentHotWaterStorageMaxTemp() {
		return currentHotWaterStorageMaxTemp;
	}

	public void setCurrentHotWaterStorageMaxTemp(double currentHotWaterStorageMaxTemp) {
		this.currentHotWaterStorageMaxTemp = currentHotWaterStorageMaxTemp;
	}

	public double getForcedOnHysteresis() {
		return forcedOnHysteresis;
	}

	public void setForcedOnHysteresis(double forcedOnHysteresis) {
		this.forcedOnHysteresis = forcedOnHysteresis;
	}

	public UUID getHotWaterTankUuid() {
		return hotWaterTankUuid;
	}

	public void setHotWaterTankUuid(UUID hotWaterTankUuid) {
		this.hotWaterTankUuid = hotWaterTankUuid;
	}

	public double getFixedCostPerStart() {
		return fixedCostPerStart;
	}

	public void setFixedCostPerStart(double fixedCostPerStart) {
		this.fixedCostPerStart = fixedCostPerStart;
	}

	public double getForcedOnOffStepMultiplier() {
		return forcedOnOffStepMultiplier;
	}

	public void setForcedOnOffStepMultiplier(double forcedOnOffStepMultiplier) {
		this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
	}

	public int getForcedOffAdditionalCost() {
		return forcedOffAdditionalCost;
	}

	public void setForcedOffAdditionalCost(int forcedOffAdditionalCost) {
		this.forcedOffAdditionalCost = forcedOffAdditionalCost;
	}

	public double getChpOnCervisiaStepSizeMultiplier() {
		return chpOnCervisiaStepSizeMultiplier;
	}

	public void setChpOnCervisiaStepSizeMultiplier(double chpOnCervisiaStepSizeMultiplier) {
		this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;
	}
}
