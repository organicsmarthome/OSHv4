package osh.mgmt.localobserver;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.SmartHeaterOX;
import osh.mgmt.ipp.SmartHeaterNonControllableIPP;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SmartHeaterLocalObserver 
					extends LocalObserver
					implements IHasState {

	private int temperatureSetting = 70;
	private int currentState = 0;
	private long[] timestampOfLastChangePerSubElement = {0,0,0};
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	private long NEW_IPP_AFTER;
	private int TRIGGER_IPP_IF_DELTATEMP_BIGGER;
	private long lastTimeIppSent = Long.MIN_VALUE;
	private double lastIppTempSetting = Integer.MIN_VALUE;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SmartHeaterLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		//NOTHING
	}
	
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		long now = getTimer().getUnixTime();
		
		IHALExchange _ihal = getObserverDataObject();
		
		if (_ihal instanceof SmartHeaterOX) {
			// get OX
			SmartHeaterOX ox = (SmartHeaterOX) _ihal;
			
			temperatureSetting = ox.getTemperatureSetting();
			currentState = ox.getCurrentState();
			timestampOfLastChangePerSubElement = ox.getTimestampOfLastChangePerSubElement();
			
			long ipp_diff = now - lastTimeIppSent;
			if (ipp_diff >= NEW_IPP_AFTER || Math.abs(temperatureSetting - lastIppTempSetting) > TRIGGER_IPP_IF_DELTATEMP_BIGGER) {
				sendIPP(now);
			}
			
			// build SX
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					getTimer().getUnixTime(),
					DeviceTypes.INSERTHEATINGELEMENT);
			
			cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, ox.getHotWaterPower());
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
		} else if (_ihal instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		} else if (_ihal instanceof IPPSchedulingExchange) {
			IPPSchedulingExchange _ise = (IPPSchedulingExchange) _ihal;
			this.NEW_IPP_AFTER = _ise.getNewIppAfter();
			this.TRIGGER_IPP_IF_DELTATEMP_BIGGER = (int) _ise.getTriggerIfDeltaX();
		}
	}
	
	private void sendIPP(long now) {
		SmartHeaterNonControllableIPP sipp = new SmartHeaterNonControllableIPP(
				getDeviceID(), 
				getGlobalLogger(), 
				now, 
				temperatureSetting, 
				currentState,
				timestampOfLastChangePerSubElement,
				compressionType,
				compressionValue);
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, sipp);
		lastTimeIppSent = now;
		lastIppTempSetting = temperatureSetting;
	}

	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		return null;
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
}
