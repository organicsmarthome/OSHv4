package osh.mgmt.localcontroller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.time.Activation;
import osh.datatypes.time.ActivationList;
import osh.hal.exchange.ChillerControllerExchange;
import osh.mgmt.ipp.ChillerIPP;
import osh.mgmt.mox.AdsorptionChillerMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class AdsorptionChillerLocalController 
				extends LocalController 
				implements IEventTypeReceiver, IHasState {
	
	// static values / constants
	private final int RESCHEDULE_AFTER = 4 * 3600; // 4 hours
	private static final long NEW_IPP_AFTER = 1800; //30 min
			
	private double minColdWaterTemp = 10.0; // [°C]
	private double maxColdWaterTemp = 15.0;	// [Â°C]
	private double hysteresis = 1.0;
	
	private double minHotWaterTemp = 55.0; // [°C]
	private double maxHotWaterTemp = 80.0; // [°C]
	
	// scheduling
	private long lastTimeReschedulingTriggered;
	private long lastTimeIppSent;
	
	private List<Activation> starttimes;
	private Activation currentActivation;
	
	// current values
	private double currentHotWaterTemperature = Double.MIN_VALUE;
	private double currentColdWaterTemperature = Double.MIN_VALUE;
	private boolean currentState = false;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR 
	 */
	public AdsorptionChillerLocalController(IOSHOC controllerbox) {
		super(controllerbox);		
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
		getOCRegistry().register(EASolutionCommandExchange.class, this);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		
		// get new Mox
		AdsorptionChillerMOX mox = (AdsorptionChillerMOX) getDataFromLocalObserver();
		double tempGradient = (mox.getColdWaterTemperature() - this.currentColdWaterTemperature) / 1.0;
		currentColdWaterTemperature = mox.getColdWaterTemperature();
		currentHotWaterTemperature = mox.getHotWaterTemperature();
		currentState = mox.isRunning();
		
		compressionType = mox.getCompressionType();
		compressionValue = mox.getCompressionValue();
		
		Map<Long, Double> temperaturePrediction = mox.getTemperatureMap();
		
		if (getTimer().getUnixTime() % 3600 == 0) {
			getGlobalLogger().logDebug("Cold Water Temperature: " + currentColdWaterTemperature);
			getGlobalLogger().logDebug("Hot Water Temperature : " + currentHotWaterTemperature);
		}
	
		
		//build CX
		ChillerControllerExchange cx = null;
		
		if (currentHotWaterTemperature < minHotWaterTemp) {
			createNewEaPart(
					false,
					temperaturePrediction,
					now, 
					false,
					0);
			
			//TURN OFF Adsorption Chiller
			cx = new ChillerControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					true, 
					false,
					0);
		}
		else if (currentColdWaterTemperature < minColdWaterTemp) {
			
			createNewEaPart(
					false,
					temperaturePrediction,
					now, 
					currentState,
					0);
			
			//TURN OFF Adsorption Chiller
			cx = new ChillerControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					true, 
					false,
					0);
		}
		else if (currentState == true
				&& currentColdWaterTemperature < maxColdWaterTemp
				&& currentColdWaterTemperature > maxColdWaterTemp - hysteresis) {
			// keep on running...
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				if (starttimes.size() == 1) {
					@SuppressWarnings("unused")
					int debug = 0;
				}
				starttimes.remove(0);
			}
			
			int expectedRunningTime = (int) (
					(currentColdWaterTemperature - maxColdWaterTemp + hysteresis) 
					/ tempGradient);
			
			createNewEaPart(
					true,
					temperaturePrediction,
					now, 
					false,
					expectedRunningTime);
			
			//TURN ON Adsorption Chiller
			cx = new ChillerControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					false,
					true, 
					expectedRunningTime);
		}
		else if (currentColdWaterTemperature > maxColdWaterTemp 
				&& !currentState) {
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				if (starttimes.size() == 1) {
					@SuppressWarnings("unused")
					int debug = 0;
				}
				starttimes.remove(0);
			}
			
			int expectedRunningTime = (int) (
					(currentColdWaterTemperature - maxColdWaterTemp + hysteresis) 
					/ tempGradient);
			
			//CHECK WHETER MIN AND MAX TEMPERATURE IS VALID
			if (currentHotWaterTemperature <= maxHotWaterTemp
					&& currentHotWaterTemperature >= minHotWaterTemp) {
				
				createNewEaPart(
						true,
						temperaturePrediction,
						now, 
						true,
						expectedRunningTime);
			
				//TURN ON Adsorption Chiller
				cx = new ChillerControllerExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						false,
						true, 
						expectedRunningTime);
			}
		}
		else {
			// check whether to reschedule...
			long diff = getTimer().getUnixTime() - lastTimeReschedulingTriggered;
			long diff_ipp = getTimer().getUnixTime() - lastTimeIppSent;
			if (diff < 0 || diff >= RESCHEDULE_AFTER) {
				createNewEaPart(
						currentState,
						temperaturePrediction,
						now, 
						true,
						0);
			}
			else if (diff_ipp >= NEW_IPP_AFTER) {
				createNewEaPart(
						currentState,
						temperaturePrediction,
						now, 
						false,
						0);
			}
			
			if (starttimes == null
					|| (starttimes.isEmpty() && currentActivation == null) 
					|| (currentActivation != null && currentActivation.startTime + currentActivation.duration < now) ) {
				cx = new ChillerControllerExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						false,
						false, 
						0);
				currentActivation = null;
			}
			else if (starttimes.size() > 0 
					&& starttimes.get(0).startTime <= now) {
				// set on
				
				// remove old start times... (sanity)
				while (starttimes != null 
						&& starttimes.size() > 0 
						&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
					if (starttimes.size() == 1) {
						@SuppressWarnings("unused")
						int debug = 0;
					}
					starttimes.remove(0);
				}
				
//				try {
//					starttimes.get(0);
//				}
//				catch (Exception e) {
//					@SuppressWarnings("unused")
//					int debug = 0;
//				}
				
				int scheduledRuntime = 0;
				
				//turn on
				if (starttimes == null || starttimes.isEmpty() || starttimes.get(0) == null) {
					getGlobalLogger().logError("starttimes.get(0) == null)");
//					System.exit(0);
				}
				else {
					scheduledRuntime = (int) (now - (starttimes.get(0).startTime + starttimes.get(0).duration));
					currentActivation = starttimes.get(0);
					starttimes.remove(0);
					cx = new ChillerControllerExchange(
							getDeviceID(), 
							getTimer().getUnixTime(), 
							false,
							true, 
							0);
				}
			}
			else if (currentActivation == null) {
				// no CHP is required...but do not send new CX (with starting times)
				@SuppressWarnings("unused")
				int debug = 0;
			}
			else {
				//should not happen..
//				throw new OSHException("BAD!");
//				getGlobalLogger().logDebug("nothing to do..");
			}
		}
		
		if (cx != null) {
			this.updateOcDataSubscriber(cx);
		}		
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		if (event instanceof EASolutionCommandExchange) {
			EASolutionCommandExchange<ActivationList> exs = ((EASolutionCommandExchange<ActivationList>) event);
			starttimes = exs.getPhenotype().getList();
			StringBuilder builder = new StringBuilder();
			
			builder.append("starttimes: {");
			boolean first = true;
			for (Activation a : starttimes) {
				if (!first) {
					builder.append(", ");
				}
				builder.append(a.startTime).append(" for ").append(a.duration);
				first = false;
			}
			builder.append("}");
			
			this.getGlobalLogger().logDebug(builder.toString());
			this.lastTimeReschedulingTriggered = getTimer().getUnixTime();
		}
	}

	private void createNewEaPart(
			boolean currentState,
			Map<Long, Double> temperaturePrediction,
			long now, 
			boolean toBeScheduled,
			long expectedRunningTime) {
		
		if (toBeScheduled) {
			System.out.println("NO");
		}
		
		ChillerIPP ex;
		long remaining = expectedRunningTime;
		if (currentState == false) {
			remaining = 0; // Chiller was shut down because the water is too cold, no remaining time
		}
		
		ex = new ChillerIPP(
				getDeviceID(), 
				getGlobalLogger(), 
				now, 
				toBeScheduled, 
				currentState, 
				temperaturePrediction,
				compressionType,
				compressionValue);
		
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, ex);
		this.lastTimeIppSent = getTimer().getUnixTime();
		if(toBeScheduled){
			this.lastTimeReschedulingTriggered = getTimer().getUnixTime();
		}
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

	
}