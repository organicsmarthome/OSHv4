package osh.mgmt.localobserver;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.mgmt.ipp.HotWaterTankNonControllableIPP;
import osh.mgmt.ipp.watertank.HotWaterTankPrediction;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class HotWaterTankLocalObserver 
				extends WaterTankLocalObserver
				implements IHasState, IEventTypeReceiver {
	
	private long NEW_IPP_AFTER;
	private double TRIGGER_IPP_IF_DELTATEMP_BIGGER;
	private long lastTimeIPPSent = Long.MIN_VALUE;
	private final double defaultPunishmentFactorPerWsPowerLost = 6.0 / 3600000.0;
	
	private Double lastKnownGasPrice = null;
	
	private double tankCapacity = 100;
	private double tankDiameter = 1.0;
	private double ambientTemperature = 20.0;
	
	private boolean lastMinuteViolated = false;
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	TreeMap<Long, Double> temperaturePrediction = new TreeMap<Long, Double>();

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public HotWaterTankLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		
		this.currentMinTemperature = 60; 
		this.currentMaxTemperature = 80;
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		getTimer().registerComponent(this, 1);
		getOCRegistry().register(EAPredictionCommandExchange.class, this);
		this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
	}
	
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		
		if (now > lastTimeIPPSent + NEW_IPP_AFTER) {
			HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
					getDeviceID(), 
					getGlobalLogger(), 
					now, 
					currentTemperature,
					tankCapacity,
					tankDiameter,
					ambientTemperature,
					(lastKnownGasPrice == null ? defaultPunishmentFactorPerWsPowerLost : (lastKnownGasPrice) / kWhToWsDivisor),
					false,
					compressionType,
					compressionValue);
			getOCRegistry().setState(
					InterdependentProblemPart.class, 
					this, 
					ex);
			lastTimeIPPSent = now;
			temperatureInLastIPP = currentTemperature;
		} else if (now % 60 == 0 && temperaturePrediction != null) {
			Entry<Long, Double> predEntry = temperaturePrediction.floorEntry(now);
			if (predEntry != null 
					&& Math.abs(predEntry.getValue() - currentTemperature) > 2.5
					//if pred is too old don't pay attention to it
					&& (temperaturePrediction.ceilingEntry(now) != null || (now - predEntry.getKey()) < 3600)) {
				if (lastMinuteViolated) {
					getGlobalLogger().logDebug("Temperature prediction was wrong by >2.5 degree for two consecutive minutes, reschedule");
					HotWaterTankNonControllableIPP ex = new HotWaterTankNonControllableIPP(
							getDeviceID(), 
							getGlobalLogger(), 
							now, 
							currentTemperature,
							tankCapacity,
							tankDiameter,
							ambientTemperature,
							(lastKnownGasPrice == null ? defaultPunishmentFactorPerWsPowerLost : (lastKnownGasPrice) / kWhToWsDivisor),
							true,
							compressionType,
							compressionValue);
					getOCRegistry().setState(
							InterdependentProblemPart.class, 
							this, 
							ex);
					lastTimeIPPSent = now;
					temperatureInLastIPP = currentTemperature;
				}
				else {
					lastMinuteViolated = true;
				}
			} else {
				lastMinuteViolated = false;
			}
		}
		
	}
	
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		
//		getGlobalLogger().logDebug("state changed at: " + getTimer().getUnixTime());
		
		// receive new state from driver
		IHALExchange _ihal = getObserverDataObject();
		
		if (_ihal instanceof HotWaterTankObserverExchange) {
			HotWaterTankObserverExchange ox = (HotWaterTankObserverExchange) _ihal;
			
			ox.getTankCapacity();
			this.currentTemperature = ox.getTopTemperature();
			
			if (Math.abs(temperatureInLastIPP - currentTemperature) >= TRIGGER_IPP_IF_DELTATEMP_BIGGER) {
				
				tankCapacity = ox.getTankCapacity();
				tankDiameter = ox.getTankDiameter();
				ambientTemperature = ox.getAmbientTemperature();
				
				HotWaterTankNonControllableIPP ex;
				ex = new HotWaterTankNonControllableIPP(
						getDeviceID(), 
						getGlobalLogger(), 
						getTimer().getUnixTime(), 
						currentTemperature,
						tankCapacity,
						tankDiameter,
						ambientTemperature,
						(lastKnownGasPrice == null ? defaultPunishmentFactorPerWsPowerLost : (lastKnownGasPrice) / kWhToWsDivisor),
						false,
						compressionType,
						compressionValue);
				getOCRegistry().setState(
						InterdependentProblemPart.class, 
						this, 
						ex);
				lastTimeIPPSent = getTimer().getUnixTime();
				temperatureInLastIPP = currentTemperature;
			}
			
			// save current state in OCRegistry (for e.g. GUI)
			WaterStorageOCSX sx = new WaterStorageOCSX(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					this.currentTemperature,
					this.currentMinTemperature, 
					this.currentMaxTemperature,
					ox.getHotWaterDemand(),
					ox.getHotWaterSupply(),
					getDeviceID());
			getOCRegistry().setState(
					WaterStorageOCSX.class, 
					this, 
					sx);
		}  else if (_ihal instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		} else if (_ihal instanceof IPPSchedulingExchange) {
			IPPSchedulingExchange _ise = (IPPSchedulingExchange) _ihal;
			this.NEW_IPP_AFTER = _ise.getNewIppAfter();
			this.TRIGGER_IPP_IF_DELTATEMP_BIGGER = _ise.getTriggerIfDeltaX();
		}		
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		if (event instanceof StateChangedExchange  && ((StateChangedExchange) event).getStatefulentity().equals(getDeviceID())) {
			StateChangedExchange exsc = (StateChangedExchange) event;
			
			if (exsc.getType().equals(EpsStateExchange.class)) {
				EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulentity());
				
				long now = getTimer().getUnixTime();
				double firstPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(now);
				double lastPrice = eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice( 
						eee.getPriceSignals().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPriceUnknownAtAndAfter() - 1);
				
				lastKnownGasPrice = (firstPrice + lastPrice) / 2.0;				
			}
		}		
		
		if (event instanceof EAPredictionCommandExchange) {
			EAPredictionCommandExchange<HotWaterTankPrediction> exs = ((EAPredictionCommandExchange<HotWaterTankPrediction>) event);
			temperaturePrediction = exs.getPrediction().getTemperatureStates();
		}
	}
	
}
