package osh.mgmt.localcontroller;

import java.util.List;
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
import osh.driver.chp.ChpOperationMode;
import osh.driver.chp.model.GenericChpModel;
import osh.hal.exchange.ChpControllerExchange;
import osh.mgmt.ipp.DachsChpIPP;
import osh.mgmt.mox.DachsChpMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.physics.ComplexPowerUtil;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer, Jan Mueller
 *
 */
public class DachsChpLocalController 
				extends LocalController 
				implements IEventTypeReceiver, IHasState {
	
	// quasi static values
	@SuppressWarnings("unused")
	private ChpOperationMode operationMode = ChpOperationMode.UNKNOWN;
	private int typicalActivePower = Integer.MIN_VALUE;
	private int typicalReactivePower = Integer.MIN_VALUE;
	private int typicalGasPower = Integer.MIN_VALUE;
	private int typicalThermalPower = Integer.MIN_VALUE;
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
	
	// ### variables ###
	
	// scheduling
	private long lastTimeReschedulingTriggered;
	private long lastTimeIppSent;
	
	private List<Activation> starttimes;
	private Activation currentActivation;
	
	// current values
	private double currentWaterTemperature = Double.MIN_VALUE;
	private boolean currentState = false;
	@SuppressWarnings("unused")
	private boolean lastState = false;
	private long runningSince;
	private long stoppedSince;
	private int lastThermalPower;
	private int currentRemainingRunningTime = 0;
	@SuppressWarnings("unused")
	private int currentActivePower = Integer.MIN_VALUE;
	@SuppressWarnings("unused")
	private int currentReactivePower = Integer.MIN_VALUE;
	private int currentThermalPower = Integer.MIN_VALUE;
	@SuppressWarnings("unused")
	private int currentGasPower = Integer.MIN_VALUE;
	
	//TODO move to config file
	private int keepAliveTime;
	
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public DachsChpLocalController(IOSHOC controllerbox) {
		super(controllerbox);
		
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
//		// init before registering for timer messages
//		createNewEaPart(false, 0);
				
		this.getTimer().registerComponent(this, 1);
		getOCRegistry().register(EASolutionCommandExchange.class, this);
		
		long start = this.getTimer().getUnixTimeAtStart();
		lastTimeIppSent = start - 86400;
		lastTimeReschedulingTriggered = start - 86400;
		
		if (getOSH().getOSHstatus().isSimulation()) {
			keepAliveTime = 0;
		} else {
			keepAliveTime = 10 * 60;
		}
	}
	
		
	@Override
	public synchronized void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		
		// get new Mox
		DachsChpMOX mox = (DachsChpMOX) getDataFromLocalObserver();
		double tempGradient = (mox.getWaterTemperature() - this.currentWaterTemperature) / 1.0; // Kelvin/sec
		this.currentWaterTemperature = mox.getWaterTemperature();
		this.currentState = mox.isRunning();
		this.currentActivePower = mox.getActivePower();
		this.currentThermalPower = mox.getThermalPower();
		this.currentGasPower = mox.getGasPower();
		this.currentRemainingRunningTime = mox.getRemainingRunningTime();
		
		this.typicalActivePower = mox.getTypicalActivePower();
		this.typicalReactivePower = mox.getTypicalReactivePower();
		this.typicalGasPower = mox.getTypicalGasPower();
		this.typicalThermalPower = mox.getTypicalThermalPower();
		
		this.rescheduleAfter = mox.getRescheduleAfter();
		this.newIPPAfter = mox.getNewIPPAfter();
		this.relativeHorizonIPP = mox.getRelativeHorizonIPP();
		this.currentHotWaterStorageMinTemp = mox.getCurrentHotWaterStorageMinTemp();
		this.currentHotWaterStorageMaxTemp = mox.getCurrentHotWaterStorageMaxTemp();
		this.forcedOnHysteresis = mox.getForcedOnHysteresis();	
		
		this.fixedCostPerStart = mox.getFixedCostPerStart();
		this.forcedOnOffStepMultiplier = mox.getForcedOnOffStepMultiplier();
		this.forcedOffAdditionalCost = mox.getForcedOffAdditionalCost();
		this.chpOnCervisiaStepSizeMultiplier = mox.getChpOnCervisiaStepSizeMultiplier();
		this.minRuntime = mox.getMinRuntime();
		
		this.compressionType = mox.getCompressionType();
		this.compressionValue = mox.getCompressionValue();
		
		if (currentState)
			lastThermalPower = currentThermalPower;

		// do control...
		ChpControllerExchange cx = null;
		
		if (this.currentWaterTemperature <= currentHotWaterStorageMinTemp) {
			// calculate expected running time (currently not used, legacy code)
 			int expectedRunningTime = (int) (
					(currentHotWaterStorageMinTemp - currentWaterTemperature + forcedOnHysteresis) 
					/ tempGradient);
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				starttimes.remove(0);
			}
			
			//update information before sending IPP
			if (!currentState) {
//				System.out.println("[" + now + "] Forced running request");
				runningSince = now;
				this.getGlobalLogger().logDebug("CHP forced on at " + now);
			}
			
			createNewEaPart(
					true, //ON
					typicalActivePower, 
					typicalReactivePower, 
					typicalThermalPower, 
					typicalGasPower, 
					now,
					!currentState, // toBeScheduled // should be true
					expectedRunningTime);
			
			// force on
			cx = new ChpControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					false, 
					false, 
					true, //ON
					expectedRunningTime);			
		}
		else if ( this.currentWaterTemperature > currentHotWaterStorageMinTemp
					&& this.currentWaterTemperature <= currentHotWaterStorageMinTemp + forcedOnHysteresis
					&& this.currentState == true) {
			//is on...well...stay at least on until a certain temperature is reached...
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				starttimes.remove(0);
			}
			
			// expectedRunningTime = minimum running time
			int expectedRunningTime = (int) (
					(currentHotWaterStorageMinTemp + forcedOnHysteresis - currentWaterTemperature) 
					/ tempGradient);
			
			createNewEaPart(
					true, //ON
					typicalActivePower, 
					typicalReactivePower, 
					typicalThermalPower, 
					typicalGasPower, 
					now, 
					false, // toBeScheduled
					expectedRunningTime);
			
			// force on (still...CHP should be running anyway)
			cx = new ChpControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					false, 
					false, 
					true,
					expectedRunningTime);
				
		}
		else if ( ( this.currentWaterTemperature > currentHotWaterStorageMinTemp
						&& this.currentWaterTemperature <= currentHotWaterStorageMinTemp + forcedOnHysteresis
						&& this.currentState == false )
					|| ( this.currentWaterTemperature > currentHotWaterStorageMinTemp + forcedOnHysteresis
							&& this.currentWaterTemperature <= currentHotWaterStorageMaxTemp) ) {
			// it's in the normal temperature zone
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				starttimes.remove(0);
			}
			
			// check whether to reschedule...
			long diff = now - lastTimeReschedulingTriggered;
			long ipp_diff = now - lastTimeIppSent;
			//don't reschedule too often let's wait first for the solution
			if (diff >= rescheduleAfter && ipp_diff > 10) {
				// force rescheduling
				createNewEaPart(
						this.currentState, 
						this.typicalActivePower, 
						this.typicalReactivePower,
						this.typicalThermalPower, 
						this.typicalGasPower, 
						now, 
						true, // toBeScheduled
						this.currentRemainingRunningTime);
			}
			else if (ipp_diff >= newIPPAfter) {
				// just update...
				createNewEaPart(
						this.currentState, 
						this.typicalActivePower, 
						this.typicalReactivePower,
						this.typicalThermalPower, 
						this.typicalGasPower, 
						now, 
						false, // toBeScheduled
						this.currentRemainingRunningTime);
			}
			
			if (starttimes != null
					&& !starttimes.isEmpty() 
					&& starttimes.get(0).startTime <= now) {
				// switch on
				
				int scheduledRuntime = 0;
				
				// switch on
				if (starttimes == null || starttimes.isEmpty() || starttimes.get(0) == null) {
					getGlobalLogger().logError("starttimes == null || starttimes.isEmpty() || starttimes.get(0) == null -> " + (starttimes!=null ? starttimes.size() : starttimes));
				}
				else {
					scheduledRuntime = (int) ((starttimes.get(0).startTime + starttimes.get(0).duration) - now);
					currentActivation = starttimes.get(0);
					long scheduledDuration = starttimes.get(0).duration;
					starttimes.remove(0);
					
					//update Information before sending IPP
					if (!currentState) {
//						System.out.println("[" + now + "] Scheduled start");
						this.getGlobalLogger().logDebug("CHP scheduled start at " + now + " for: " + scheduledDuration);
						runningSince = now;
					}
					
					createNewEaPart(
							true, 
							this.typicalActivePower, 
							this.typicalReactivePower,
							this.typicalThermalPower, 
							this.typicalGasPower, 
							now, 
							false,
							scheduledRuntime);
					
					cx = new ChpControllerExchange(
							getDeviceID(), 
							getTimer().getUnixTime(), 
							false, 
							false, 
							true, 
							scheduledRuntime);
				}
			}
			else if (( currentActivation != null && currentActivation.startTime + currentActivation.duration - keepAliveTime < now )
						|| ( currentActivation == null && starttimes == null )
						|| ( currentActivation == null && starttimes.isEmpty() ) 
						|| ( currentActivation == null && starttimes.get(0).startTime > now )) {
				// switch off (has only been on because of forced on or scheduled runtime is over)
				cx = new ChpControllerExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						false, 
						false, 
						false, 
						0);
				currentActivation = null;
				
				if (currentState) {
//					System.out.println("[" + now + "] Scheduled stop");
					this.getGlobalLogger().logDebug("CHP scheduled stop at " + now);
					stoppedSince = now;
				}
				
				if (currentState) {
					//send IPP because of switch off
					createNewEaPart(
							false, 
							this.typicalActivePower, 
							this.typicalReactivePower,
							this.typicalThermalPower, 
							this.typicalGasPower, 
							now, 
							false,
							0);
				}
			}
			else {
				//Nothing to do... (current activation is active)
//				if (currentActivation == null) {
//					getGlobalLogger().logDebug("nothing to do..");
//				}
			}
		}
		else if (this.currentWaterTemperature > currentHotWaterStorageMaxTemp) {
//			int expectedRunningTime = (int) ((currentWaterTemperature - currentHotWaterStorageMaxTemp) / tempGradient);
			
			// remove old start times... (sanity)
			while (starttimes != null 
					&& starttimes.size() > 0 
					&& starttimes.get(0).startTime + starttimes.get(0).duration < now) {
				starttimes.remove(0);
			}
			
			//update information before sending IPP
			if (currentState) {
//				System.out.println("[" + now + "] forced stop");
				this.getGlobalLogger().logDebug("CHP forced off at " + now);
				stoppedSince = now;
			}
			
			createNewEaPart(
					false, 
					typicalActivePower, 
					typicalReactivePower, 
					typicalThermalPower, 
					typicalGasPower, 
					now, 
					currentState, // toBeScheduled
					0);
			
			// force off
			cx = new ChpControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					true,
					false, 
					false, 
					0);
		}
		else {
			getGlobalLogger().logError("SHOULD NEVER HAPPEN");
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
			
			long now = getTimer().getUnixTime();
			
			//check if currently running and should be shutdown by the optimization
			if (currentActivation != null) {
				if (starttimes != null && !starttimes.isEmpty() && starttimes.get(0).startTime < now && starttimes.get(0).startTime + starttimes.get(0).duration > now) {
					currentActivation = starttimes.get(0);
					starttimes.remove(0);
				} else {
					currentActivation = null;
				}				
			}
			
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
			
			// scheduling has been triggered by some other device...
			this.lastTimeReschedulingTriggered = exs.getTimestamp();
		}
	}
	
	
	private void createNewEaPart(
			boolean currentState,
			int typicalActivePower,
			int typicalReactivePower,
			int typicalThermalPower,
			int typicalGasPower,
			long now, 
			boolean toBeScheduled,
			long remainingRunningTime) {
		
		DachsChpIPP ex;
//		long remaining = remainingRunningTime;
//		if (currentState == false) {
//			remaining = 0; // CHP was shut down because the water is too hot, no remaining time
//		}
		
		GenericChpModel chpModel = null;
		try {
			chpModel = new GenericChpModel(
					typicalActivePower, 
					typicalReactivePower, 
					typicalThermalPower, 
					typicalGasPower, 
					ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(typicalActivePower, typicalReactivePower), currentState, 
					lastThermalPower, 
					runningSince, 
					stoppedSince);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ex = new DachsChpIPP(
				getDeviceID(), 
				getGlobalLogger(),
				now, 
				toBeScheduled,				
				currentState,
				minRuntime,
				chpModel,
//				- Math.abs(typicalActivePower),
//				Math.abs(typicalGasPower),
				relativeHorizonIPP,
				currentHotWaterStorageMinTemp,
				currentHotWaterStorageMaxTemp,
				forcedOnHysteresis,
				70.0,
				fixedCostPerStart,	
				forcedOnOffStepMultiplier,
				forcedOffAdditionalCost,	
				chpOnCervisiaStepSizeMultiplier,
				compressionType,
				compressionValue); //initial tank temperature for optimization
		this.lastTimeIppSent = now;
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, ex);
		
		
		// IMPORTANT: update of variable is done via onQueueEventTypeReceived()
//		if ( toBeScheduled ) {
//			this.lastTimeReschedulingTriggered = now;
//		}
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}


}
