package osh.mgmt.localobserver;

import java.util.UUID;

import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ColdWaterTankObserverExchange;
import osh.mgmt.ipp.ColdWaterTankNonControllableIPP;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class ColdWaterTankLocalObserver 
				extends WaterTankLocalObserver
				implements IHasState {
	
	private static final long NEW_IPP_AFTER = 1 * 3600; // at least send new IPP every hour
	private long lastTimeIPPSent = Long.MIN_VALUE;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public ColdWaterTankLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		
		this.currentMinTemperature = 10; 
		this.currentMaxTemperature = 15;
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		getTimer().registerComponent(this, 1);
	}
	
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		
		if (now > lastTimeIPPSent + NEW_IPP_AFTER) {
			ColdWaterTankNonControllableIPP ex;
			ex = new ColdWaterTankNonControllableIPP(
					getDeviceID(), 
					getGlobalLogger(), 
					now, 
					currentTemperature,
					compressionType,
					compressionValue);
			getOCRegistry().setState(
					InterdependentProblemPart.class, 
					this, 
					ex);
			lastTimeIPPSent = now;
			temperatureInLastIPP = currentTemperature;
		}

	}
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		// receive new state from driver
		IHALExchange _ihal = getObserverDataObject();
		
		if (_ihal instanceof ColdWaterTankObserverExchange) {
			ColdWaterTankObserverExchange ox = (ColdWaterTankObserverExchange) _ihal;
			
			ox.getTankCapacity();
			this.currentTemperature = ox.getTopTemperature();

			if (Math.abs(temperatureInLastIPP - currentTemperature) >= 0.1) {
				ColdWaterTankNonControllableIPP ex;
				ex = new ColdWaterTankNonControllableIPP(
						getDeviceID(), 
						getGlobalLogger(), 
						getTimer().getUnixTime(), 
						currentTemperature,
						compressionType,
						compressionValue);
				getOCRegistry().setState(
						InterdependentProblemPart.class, 
						this, 
						ex);
				temperatureInLastIPP = currentTemperature;
				lastTimeIPPSent = getTimer().getUnixTime();
			}
			
			// save current state in OCRegistry (for e.g. GUI)
			WaterStorageOCSX sx = new WaterStorageOCSX(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					this.currentTemperature,
					this.currentMinTemperature, 
					this.currentMaxTemperature,
					ox.getColdWaterDemand(),
					ox.getColdWaterSupply(),
					getDeviceID());
			getOCRegistry().setState(
					WaterStorageOCSX.class, 
					this, 
					sx);
		} 
		else if (_ihal instanceof StaticCompressionExchange) {
				StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
				this.compressionType = _stat.getCompressionType();
				this.compressionValue = _stat.getCompressionValue();
		}		
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
