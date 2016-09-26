package osh.mgmt.localcontroller;

import java.util.Map;
import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.hal.exchange.ChillerControllerExchange;
import osh.mgmt.ipp.ChillerNonControllableIPP;
import osh.mgmt.mox.AdsorptionChillerMOX;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class NonControllableAdsorptionChillerLocalController 
				extends LocalController
				implements IHasState {
	
	private double minColdWaterTemp = 10.0;
	private double maxColdWaterTemp = 15.0;
	
	private double minHotWaterTemp = 60.0;
	private double maxHotWaterTemp = 80.0;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public NonControllableAdsorptionChillerLocalController(IOSHOC controllerbox) {
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
		
		// get new Mox
		AdsorptionChillerMOX mox = (AdsorptionChillerMOX) getDataFromLocalObserver();
		
		double currentColdWaterTemp = mox.getColdWaterTemperature();
		double currentHotWaterTemp = mox.getHotWaterTemperature();
		boolean currentState = mox.isRunning();
		
		compressionType = mox.getCompressionType();
		compressionValue = mox.getCompressionValue();
		Map<Long, Double> temperaturePrediction = mox.getTemperatureMap();
		
		if (getTimer().getUnixTime() % 900 == 0) {
			//getGlobalLogger().logDebug("Cold Water Temperature: " + currentColdWaterTemp);
			//getGlobalLogger().logDebug("Hot Water Temperature: " + currentHotWaterTemp);
		}
		
		boolean toBeScheduled = false;
		// #1 Ask for rescheduling if temperature is above a certain threshold
//		if ( !currentState && currentColdWaterTemp >= maxColdWaterTemp - 0.1) {
//			toBeScheduled = true;
//		}
		
		// #2 Ask for rescheduling after a certain time (e.g. 6 hours)
		//TODO
		
		// #3 Ask for rescheduling after BIG changes...
		//TODO
		
		// new IPP
		ChillerNonControllableIPP ipp = new ChillerNonControllableIPP(
				getDeviceID(), 
				getGlobalLogger(), 
				getTimer().getUnixTime(), 
				toBeScheduled,
				currentState, 
				temperaturePrediction,
				compressionType,
				compressionValue);
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, ipp);
		
		//build CX
		ChillerControllerExchange cx = null;
		if (currentColdWaterTemp <= minColdWaterTemp) {
			//TURN OFF Adsorption Chiller
			cx = new ChillerControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					true, 
					false,
					0);
		}
		else if (currentColdWaterTemp >= maxColdWaterTemp) {
			//CHECK WHETER MIN AND MAX TEMPERATURE IS VALID
			if (currentHotWaterTemp <= maxHotWaterTemp
					&& currentHotWaterTemp >= minHotWaterTemp) {
				//TURN ON Adsorption Chiller
				cx = new ChillerControllerExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						false,
						true, 
						15 * 60);
			}
		}
		
		if (cx != null) {
			this.updateOcDataSubscriber(cx);
		}		
	}


	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
}
