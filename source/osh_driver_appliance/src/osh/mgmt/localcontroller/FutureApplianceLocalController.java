package osh.mgmt.localcontroller;

import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.FutureApplianceControllerExchange;
import osh.mgmt.localcontroller.ipp.FutureApplianceIPP;
import osh.mgmt.localcontroller.ipp.FutureAppliancesStaticIPP;
import osh.mgmt.localcontroller.ipp.GenericApplianceSolution;
import osh.mgmt.mox.GenericApplianceMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class FutureApplianceLocalController 
					extends LocalController 
					implements IEventTypeReceiver, IHasState {
	
	/** State of the device as EN 50523 state */
	private EN50523DeviceState currentState = null;
	
	/**
	 * initially: ACP = null<br>
	 * otherwise: current ACP
	 */
	private ApplianceProgramConfigurationStatus acp = null;
	
	// TDoF variables
	/** Never change this by hand, use setDof() */
	private int current1stTemporalDof = (int) (0 * 3600); 
	
	/** Never change this by hand, use setDof() */
	@Deprecated
	private int max2ndTemporalDof = 0; // currently not in use
	
	/** indicates whether tDoF has been changed by the user (e.g., using the GUI) */
	private boolean tDOFChanged = false;
	
	// scheduling variables
	private UUID acpIDforScheduledTimes = null;
	private Long earliestStartingTime = null;
	private Long latestStartingTime = null;
	private Long originalMaxduration = null;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	private long lastTimeSchedulingCaused = 0L;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public FutureApplianceLocalController(IOSHOC controllerbox) {
		super(controllerbox);
		//currently NOTHING
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		// register to be called every time step
		this.getTimer().registerComponent(this, 1);		
		// register for solutions of the optimization
		this.getOCRegistry().register(EASolutionCommandExchange.class, this);
		
		this.lastTimeSchedulingCaused = getTimer().getUnixTime() - 61;
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		// check whether there is something new...
		// ...and update MOX object for controller
		updateMox(); 
	}
	
	/**
	 * Update MOX
	 */
	private void updateMox() throws OSHException {
		//
		// is new IPP necessary? default: no!
		boolean triggerNewIPP = false;
		// is rescheduling necessary? default: no!
		boolean triggerScheduling = false;
		// get new MOX
		GenericApplianceMOX mox = (GenericApplianceMOX) getDataFromLocalObserver();
		// compression may have changed
		this.compressionType = mox.getCompressionType();
		this.compressionValue = mox.getCompressionValue();
		
//		getGlobalLogger().logDebug("getAcpReferenceTime "+mox.getAcpReferenceTime());
//		getGlobalLogger().logDebug("getAcpID "+mox.getAcpID());
//		getGlobalLogger().logDebug("getAcp "+mox.getAcp());
//		getGlobalLogger().logDebug("getCurrentState "+mox.getCurrentState());
//		getGlobalLogger().logDebug("getDof "+mox.getDof());
		
		// check whether device has been set to OFF
		if (mox.getCurrentState() == EN50523DeviceState.OFF	&& (this.currentState == null || this.currentState != EN50523DeviceState.OFF) ) {
			
			// #1 initial OFF...
			// #2 or appliance is set OFF and has been scheduled before...
			// #3 or device is new...
			
			triggerScheduling = true && this.currentState != null;
			// get state
			this.currentState = mox.getCurrentState();
			// just update IPP
			triggerNewIPP = true;			
			// reset ACP
			this.acp = null;
			// reset scheduling  variables
			this.acpIDforScheduledTimes = null;
			earliestStartingTime = null;
			latestStartingTime = null;
			originalMaxduration = null;
		}
		else if (mox.getCurrentState() == EN50523DeviceState.ENDPROGRAMMED	&& (this.currentState == null || this.currentState != EN50523DeviceState.ENDPROGRAMMED)) {
			// device is finished (waiting for being switched off)
			//
			// get state
			this.currentState = mox.getCurrentState();
			// just update IPP
			triggerNewIPP = true;
			triggerScheduling = false;
			// reset ACP
			this.acp = null;
			// reset scheduling  variables
			this.acpIDforScheduledTimes = null;
			earliestStartingTime = null;
			latestStartingTime = null;
			originalMaxduration = null;
		}
		else if(mox.getDof()!=null && (this.current1stTemporalDof/10)!=(mox.getDof()/10)){
			this.currentState = mox.getCurrentState();
			this.setTemporalDof(mox.getDof().intValue(), null);
		}
		else if ( (mox.getCurrentState() == EN50523DeviceState.OFF && this.currentState == EN50523DeviceState.OFF)
					|| (mox.getCurrentState() == EN50523DeviceState.PROGRAMMED && this.currentState == EN50523DeviceState.PROGRAMMED)
					|| (mox.getCurrentState() == EN50523DeviceState.ENDPROGRAMMED && this.currentState == EN50523DeviceState.ENDPROGRAMMED) ) {
			// device still OFF
			// device still PROGRAMMED
			// device still ENDPOGRAMMED
			// ...do nothing!!!

			
		}
		// check whether there is a new ACP...
		else if (mox.getAcp() != null) {
			// got new ACP...
			// i.e., something has changed 
			// #1 new configuration or 
			// #2 new phase
			
			// get state
			this.currentState = mox.getCurrentState();
			
			// safety check only trigger rescheduling if acp has changed or acp has not been scheduled
			if (this.acpIDforScheduledTimes == null || !this.acpIDforScheduledTimes.equals(mox.getAcpID())) {
				// new ACP -> device has been (re-)configured (changed program, or set off, or ...)
				this.acp = mox.getAcp();
				
				// reset scheduling  variables
				this.acpIDforScheduledTimes = null;
				//earliestStartingTime = null; // NO!
				//latestStartingTime = null; // NO!
				
				if (!acp.isDoNotReschedule()) {
					// trigger scheduling
					triggerNewIPP = true;
					triggerScheduling = true;
				}			
			}
		}
		// device ON, but no new ACP
		else {
			// mox.getAcp() == null
			// no new ACP, check whether ACP available
			if (this.acp != null) {
				// ACP is available
				if (this.currentState != mox.getCurrentState()) {
					getGlobalLogger().logDebug("ERROR: state changed without new ACP! should NOT happen!");
				}
				if ( this.acp.getAcpID() != mox.getAcpID() ) {
					// acpID changed without new ACP!
					// should NOT happen!
					getGlobalLogger().logDebug("ERROR: acpID changed without new ACP! should NOT happen!");
				}
				
				// nothing changed, device is running...
			}
			else {
				getGlobalLogger().logDebug("ERROR: should not happen. Device ON, but no new ACP = null");
			}
		}
		// check whether to update IPP
		if (triggerScheduling || tDOFChanged || triggerNewIPP) {
			//  update of IPP...
			updateIPP(triggerScheduling);
		}
	}
	
	/**
	 * called if:
	 * 1. new tDoF<br>
	 * 2. triggered by MOX<br>
	 * 2.1. new ACP (new configuration or new phase)<br>
	 * 2.2. switched OFF or ENDPROGRAMMED<br>
	 */
	protected void updateIPP(boolean toBeScheduled) throws OSHException {
		
		InterdependentProblemPart<?, ?> ipp = null;
		long now = getTimer().getUnixTime();
		
		// no profile to be scheduled...and no profile has been scheduled...
		if ( acp == null && acpIDforScheduledTimes == null ) {
			// OFF or ENDPROGRAMMED
			if (currentState != EN50523DeviceState.OFF && currentState != EN50523DeviceState.ENDPROGRAMMED) {
				getGlobalLogger().logDebug("ERROR: should not happen. Device is OFF or ENDPROGRAMMED ");
			}
//			if (toBeScheduled) {
				// IPP for OFF or ENDPROGAMMED
				ipp = new FutureAppliancesStaticIPP(
						getDeviceID(), 
						getGlobalLogger(),
						now,
						toBeScheduled,
						now, 
						getLocalObserver().getDeviceType(), 
						now,
						new Schedule(new SparseLoadProfile(), 0.0, getLocalObserver().getDeviceType().toString()),
						compressionType,
						compressionValue);
//			}

		}
		else if ( this.acp == null ) {
			getGlobalLogger().logDebug("ERROR: should NOT happen! acp == null");
		}
		// got ACP, but without DLP -> should NOT happen
		else if ( this.acp != null && acp.getDynamicLoadProfiles() == null ) {
			getGlobalLogger().logDebug("ERROR: acp != null && acp.getDynamicLoadProfiles() == null");
		}
		// there is probably something to be scheduled...
		else {
			
			// should NOT be OFF or ENDPROGRAMMED
			if (currentState == EN50523DeviceState.OFF || currentState == EN50523DeviceState.ENDPROGRAMMED) {
				getGlobalLogger().logDebug("ERROR: should not happen. Device is OFF or ENDPROGRAMMED but something wants to be scheduled");
			}
						
			// check whether ACP has already been scheduled
			if (acpIDforScheduledTimes == null) {
				// new ACP 
				
				//TODO NO new phase within one configuration) from appliance (has not been scheduled, yet)
				
				// triggered by MoOX (resets acpIDForTimes and acpIDforScheduledTimes to NULL)
				
				// reset earliest starting time
				earliestStartingTime = now;
				
				if (currentState == EN50523DeviceState.PROGRAMMED) {
					//OK
					latestStartingTime = now + current1stTemporalDof;
					originalMaxduration = ApplianceProgramConfigurationStatus.getTotalMaxDuration(acp);
//					getGlobalLogger().logDebug("Setting new acp: " + originalMaxduration);
				}
				else if (currentState == EN50523DeviceState.RUNNING) {
					// keep latest starting time
				}
				else {
					getGlobalLogger().logDebug("ERROR: should not happen. acpIDforScheduledTimes == null but device ist not PROGRAMMED or RUNNING");
				}
				
				acpIDforScheduledTimes = acp.getAcpID();
			}
			else {
				// some ACP has already been scheduled...
				// check whether it was the current ACP...
				if (acpIDforScheduledTimes.equals(acp.getAcpID())) {
					// current ACP
					if (tDOFChanged) {
						// triggered by setTemporalDoF
						// rescheduling of old one with new tDoF (dofChanged)
						// reset earliest and latest starting times according to 1st tDoF
						earliestStartingTime = now;
						latestStartingTime = now + current1stTemporalDof;
					}
					else {
						getGlobalLogger().logDebug("ERROR: should not happen (acpID has already been scheduled...no new TDOF...why reschedule?!?)");
					}
				}
				else {
					// other ACP
					getGlobalLogger().logDebug("ERROR: should not happen acpIDforScheduledTimes unequals to acp.getAcpID() ");
				}
			}
			
			// determine longest profile of alternatives (with minimum times of phases)
			long maxDuration = ApplianceProgramConfigurationStatus.getTotalMaxDuration(acp);
			
			// determine absolute optimization horizon (absolute time!)
			if (latestStartingTime == null){
				latestStartingTime = now;
			}
			
			//for interruptible devices, we need to correct the LST
			if (originalMaxduration > maxDuration) {
//				getGlobalLogger().logDebug("Correcting LST by " + (originalMaxduration - maxDuration));
				latestStartingTime += (originalMaxduration - maxDuration);
				// correcting old maxDuration so that the next phase does not cause to much correction
				originalMaxduration = maxDuration;
				
			} 
			else if (originalMaxduration < maxDuration){
				getGlobalLogger().logError("New Maxduration bigger than older duration, this should not happen");
			}
			
			long optimizationHorizon = now + (latestStartingTime - earliestStartingTime) + maxDuration;
			
			//ensure that hybrid devices have a long horizon
			if (acp.getDynamicLoadProfiles().length > 1) {
				optimizationHorizon = Math.max(optimizationHorizon, now + 24 * 60 * 60);
			}
			
//			getGlobalLogger().logDebug("EST: " + earliestStartingTime);
//			getGlobalLogger().logDebug("LST: " + latestStartingTime);
//			getGlobalLogger().logDebug("maxDur: " + maxDuration);
//			getGlobalLogger().logDebug("New horizon: " + optimizationHorizon);
			 
			// construct EAPart
			ipp = new FutureApplianceIPP(
					getDeviceID(), 
					getGlobalLogger(),
					now, 
					FutureApplianceIPP.calculateBitCount(
							earliestStartingTime, 
							latestStartingTime, 
							acp), 
					toBeScheduled,
					optimizationHorizon, 
					getLocalObserver().getDeviceType(), 
					now,
					earliestStartingTime, 
					latestStartingTime,
					acp,
					compressionType,
					compressionValue);
		
		}
		
		// reset tDOF, because it is scheduled...
		this.tDOFChanged = false; //reset
		
		// send EAP to Global O/C-unit
		if (ipp != null) {
//			if (toBeScheduled && (now - lastTimeSchedulingCaused) > 60) {
//				lastTimeSchedulingCaused = now;
//				this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
//			} else if (!toBeScheduled) {
				this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
//			}
		}
	}
	
	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T ex) throws OSHException {
		// EAP Solution
		if (ex instanceof EASolutionCommandExchange) {
			try {
				@SuppressWarnings("unchecked")
				EASolutionCommandExchange<GenericApplianceSolution> solution = 
						(EASolutionCommandExchange<GenericApplianceSolution>) ex;
				
				// must be for somebody else... or solution is empty...
				if (!solution.getReceiver().equals(getDeviceID()) 
						|| solution.getPhenotype() == null) {
					return;
				}
				
				try {
					// solution is for old ACP
					if (acp == null || solution.getPhenotype().acpUUID != acp.getAcpID()) {
						getGlobalLogger().logDebug("received invalid solution: was for old ACP");
						return;
					}
				}
				catch (NullPointerException e) {
					getGlobalLogger().logDebug(e);
				}
				
				long now = getTimer().getUnixTime();
				
				// ### transform solution to phenotype ###
				
				// get selected profile id (e.g. hybrid or normal)
				int selectedProfileId = solution.getPhenotype().profileId;
				
				//calculate start times
				long[] selectedStartTimes = solution.getPhenotype().startingTimes; 
				
				//should never be NULL...
				if (acp != null) {
					FutureApplianceControllerExchange cx = new FutureApplianceControllerExchange(
							getDeviceID(), 
							now, //TODo: maybe change to cx.getTimeStamp() (should be irrelevant, because of absolute times)
							acp.getAcpID(), 
							selectedProfileId, 
							selectedStartTimes);
					// send CX to driver
					updateOcDataSubscriber(cx);
				}
				else {
					// received command although no ACP available...
					getGlobalLogger().logDebug("acp == null");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * Use this method to set new tDoF
	 */
	protected void setTemporalDof(Integer firstTemporalDof, Integer secondTemporalDof) throws OSHException {
		if ((firstTemporalDof != null && firstTemporalDof < 0)
				|| (secondTemporalDof != null && secondTemporalDof < 0)) {
			throw new IllegalArgumentException("firstDof or secondDof < 0");
		}

		if (firstTemporalDof == null && secondTemporalDof == null) {
			throw new IllegalArgumentException("firstDof and secondDof == null");
		}

		if (firstTemporalDof != null && this.current1stTemporalDof != firstTemporalDof) {
			this.current1stTemporalDof = firstTemporalDof;
			tDOFChanged = true;
		}

		if (secondTemporalDof != null && this.max2ndTemporalDof != secondTemporalDof) {
			this.max2ndTemporalDof = secondTemporalDof;
			tDOFChanged = true;
		}

		if (tDOFChanged) {
			// trigger rescheduling if this happens
			if (currentState != EN50523DeviceState.OFF && (currentState != EN50523DeviceState.PROGRAMMED || acp != null))
				updateIPP(true);
		}
	}

	
	@Override
	public UUID getUUID() {
		return this.getDeviceID();
	}

}
