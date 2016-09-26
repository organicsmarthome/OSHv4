package osh.mgmt.localobserver;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChpObserverExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.mgmt.mox.DachsChpMOX;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class NonControllableDachsChpLocalObserver 
					extends LocalObserver
					implements IHasState {
	
	// data from WaterTank
	private double waterTemperature;
//	private INeededEnergy neededEnergy;
	
	// data from CHP
	private int activePower;
	private int reactivePower;
	private int hotWaterPower;
	private int gasPower;
	
	private int runtimeRemaining;
	private boolean running;
	
	// quasi static values
	private ChpOperationMode operationMode = ChpOperationMode.UNKNOWN;
	private int typicalActivePower;
	private int typicalReactivePower;
	private int typicalGasPower;
	private int typicalThermalPower;
	private UUID hotWaterTankUuid;
	
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
	 * @param controllerbox
	 */
	public NonControllableDachsChpLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
	}
	

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();

		WaterStorageOCSX sx = (WaterStorageOCSX) getOCRegistry().getState(
				WaterStorageOCSX.class, 
				hotWaterTankUuid);
		this.waterTemperature = sx.getCurrenttemp();
	}
	
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException{
		IHALExchange ihex = getObserverDataObject();
		
		if (ihex instanceof ChpObserverExchange) {
			ChpObserverExchange dox = (ChpObserverExchange) ihex;
			
			// current values...
			this.activePower = dox.getActivePower();
			this.reactivePower = dox.getReactivePower();
			this.hotWaterPower = dox.getHotWaterPower();
			this.gasPower = dox.getGasPower();
			
			this.running = dox.isRunning();
			this.runtimeRemaining = dox.getMinRuntimeRemaining();
			
			
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					getTimer().getUnixTime(),
					DeviceTypes.CHPPLANT);
			
			cpse.addPowerState(Commodity.ACTIVEPOWER, activePower);
			cpse.addPowerState(Commodity.REACTIVEPOWER, reactivePower);
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			cpse.addPowerState(Commodity.NATURALGASPOWER, gasPower);
			
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
			
		} else if (ihex instanceof ChpStaticDetailsObserverExchange) {
			ChpStaticDetailsObserverExchange diox = (ChpStaticDetailsObserverExchange) ihex;
			
			// static details...
			this.typicalActivePower = diox.getTypicalActivePower();
			this.typicalReactivePower = diox.getTypicalReactivePower();
			this.typicalGasPower = diox.getTypicalGasPower();
			this.typicalThermalPower = diox.getTypicalThermalPower();
			this.hotWaterTankUuid = diox.getHotWaterTankUuid();
			this.rescheduleAfter = diox.getRescheduleAfter();
			this.newIPPAfter = diox.getNewIPPAfter();
			this.currentHotWaterStorageMinTemp = diox.getCurrentHotWaterStorageMinTemp();
			this.currentHotWaterStorageMaxTemp = diox.getCurrentHotWaterStorageMaxTemp();
			this.forcedOnHysteresis = diox.getForcedOnHysteresis();
			this.relativeHorizonIPP = diox.getRelativeHorizonIPP();
			
			this.fixedCostPerStart = diox.getFixedCostPerStart();
			this.forcedOnOffStepMultiplier = diox.getForcedOnOffStepMultiplier();
			this.forcedOffAdditionalCost = diox.getForcedOffAdditionalCost();
			this.chpOnCervisiaStepSizeMultiplier = diox.getChpOnCervisiaStepSizeMultiplier();
			this.minRuntime = diox.getMinRuntime();
		} else if (ihex instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) ihex;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}
	
	
	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		DachsChpMOX newMox = new DachsChpMOX(
				waterTemperature, 
				running, 
				runtimeRemaining, 
				activePower,
				reactivePower,
				hotWaterPower, 
				gasPower,
				operationMode,
				typicalActivePower,
				typicalReactivePower,
				typicalGasPower,
				typicalThermalPower,
				rescheduleAfter,
				newIPPAfter,
				relativeHorizonIPP,
				currentHotWaterStorageMinTemp,
				currentHotWaterStorageMaxTemp,
				forcedOnHysteresis,
				fixedCostPerStart,
				forcedOnOffStepMultiplier,
				forcedOffAdditionalCost,
				chpOnCervisiaStepSizeMultiplier,
				minRuntime,
				compressionType,
				compressionValue);
		return newMox;
	}


	@Override
	public UUID getUUID() {
		return getDeviceID();
	}


	// HELPER
	
}
