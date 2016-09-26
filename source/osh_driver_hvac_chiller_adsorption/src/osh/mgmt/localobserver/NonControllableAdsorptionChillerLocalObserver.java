package osh.mgmt.localobserver;

import java.util.Map;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChillerObserverExchange;
import osh.mgmt.mox.AdsorptionChillerMOX;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class NonControllableAdsorptionChillerLocalObserver extends LocalObserver {
	
	private boolean running = false;
	private double coldWaterTemperature = Double.MIN_VALUE;
	private double hotWaterTemperature = Double.MIN_VALUE;
	
	// current values
	private int activePower;
	private int reactivePower;
	
	private int hotWaterPower;
	private int coldWaterPower;
	
	// Temporary constants
	private final UUID coldWaterTankUuid = UUID.fromString("441c234e-d340-4c85-b0a0-dbac182b8f81");
	private final UUID hotWaterTankUuid = UUID.fromString("00000000-0000-4857-4853-000000000000");
		
	private Map<Long, Double> temperatureMap = null;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	/**
	 * CONSTRUCTOR 
	 */
	public NonControllableAdsorptionChillerLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}


	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		IHALExchange hx = getObserverDataObject();
		if(hx == null) {
			return;
		}
		if (hx instanceof ChillerObserverExchange) {
			ChillerObserverExchange ox = (ChillerObserverExchange) getObserverDataObject();
			running = ox.isRunning();
			
			activePower = ox.getActivePower();
			reactivePower = ox.getReactivePower();
			
			hotWaterPower = ox.getHotWaterPower();
			coldWaterPower = ox.getColdWaterPower();
			
			//DIRTY HACK
			if (temperatureMap == null) {
				temperatureMap = ox.getOutdoorTemperature().getMap();
			}
			
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					getTimer().getUnixTime(),
					DeviceTypes.ADSORPTIONCHILLER);
			cpse.addPowerState(Commodity.ACTIVEPOWER, activePower);
			cpse.addPowerState(Commodity.REACTIVEPOWER, reactivePower);
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			cpse.addPowerState(Commodity.COLDWATERPOWER, coldWaterPower);
			this.getOCRegistry().setStateOfSender(
					CommodityPowerStateExchange.class,
					cpse);
		} else if (hx instanceof StaticCompressionExchange) {
			StaticCompressionExchange sce = (StaticCompressionExchange) hx;
			compressionType = sce.getCompressionType();
			compressionValue = sce.getCompressionValue();			
		}
		else {
			return;
		}
	}

	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		
		WaterStorageOCSX wssx = getOCRegistry().getState(WaterStorageOCSX.class, coldWaterTankUuid);
		if (wssx != null) {
			coldWaterTemperature = wssx.getCurrenttemp();
		}
		
		WaterStorageOCSX hwssx = getOCRegistry().getState(WaterStorageOCSX.class, hotWaterTankUuid);
		if (hwssx != null) {
			hotWaterTemperature = hwssx.getCurrenttemp();
		}
		
		// TODO: use real prediction
		AdsorptionChillerMOX mox = new AdsorptionChillerMOX(
				coldWaterTemperature,
				hotWaterTemperature,
				running, 
				0,
				temperatureMap,
				compressionType,
				compressionValue);
		
		return mox;
	}
	
	
}
