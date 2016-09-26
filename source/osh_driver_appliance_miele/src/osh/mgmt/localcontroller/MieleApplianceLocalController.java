package osh.mgmt.localcontroller;

import java.util.UUID;

import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.datatypes.registry.oc.state.IAction;
import osh.datatypes.registry.oc.state.LastActionExchange;
import osh.datatypes.registry.oc.state.MieleDofStateExchange;
import osh.en50523.EN50523DeviceState;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.hal.exchange.GenericApplianceStarttimesControllerExchange;
import osh.hal.exchange.MieleApplianceControllerExchange;
import osh.mgmt.localobserver.ipp.MieleApplianceIPP;
import osh.mgmt.localobserver.ipp.MieleSolution;
import osh.mgmt.localobserver.ipp.MieleApplianceNonControllableIPP;
import osh.mgmt.localobserver.miele.MieleAction;
import osh.mgmt.mox.MieleApplianceMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class MieleApplianceLocalController 
				extends LocalController 
				implements IEventTypeReceiver, IHasState {
	

	/**
	 * SparseLoadProfile containing different profile with different commodities<br>
	 * IMPORATANT: RELATIVE TIMES!
	 */
	private SparseLoadProfile currentProfile;
	
	private EN50523DeviceState currentState;
	
	/** use private setter setStartTime() */
	private long startTime = -1;
	
	// used for EA planning
	private long expectedStarttime = -1;
	private long profileStarted = -1;
	private long programmedAt = -1;
		
	/** Never change this by hand, use setDof() */
	private int firstDof = 0;
	/** Never change this by hand, use setDof() */
	private int secondDof = 0;
	/** Never change this by hand, use setDof() */
	private long latestStart = 0;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public MieleApplianceLocalController(IOSHOC controllerbox) {
		super(controllerbox);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		// register for onNextTimePeriod()
		getTimer().registerComponent(this, 1);
		
		getOCRegistry().register(EASolutionCommandExchange.class, this);		
		getOCRegistry().registerStateChangeListener(DofStateExchange.class, this);
		
		//workaround bc this controller may not have this data from the driver->observer chain
		if (compressionType == null) {
			compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			compressionValue = 100;
		}		
		updateIPPExchange();
	}

	private void callDevice() {
		MieleApplianceControllerExchange halControllerExchangeObject
			= new MieleApplianceControllerExchange(
					this.getDeviceID(), 
					getTimer().getUnixTime(), 
					EN50523OIDExecutionOfACommandCommands.START);
		this.updateOcDataSubscriber(halControllerExchangeObject);
	}
	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T ex) throws OSHException {
		if (ex instanceof EASolutionCommandExchange) {
			@SuppressWarnings("unchecked")
			EASolutionCommandExchange<MieleSolution> solution = (EASolutionCommandExchange<MieleSolution>) ex;
			if (!solution.getReceiver().equals(getDeviceID()) || solution.getPhenotype() == null) return;
			getGlobalLogger().logDebug("getting new starttime: " + solution.getPhenotype().startTime);
			setStartTime(solution.getPhenotype().startTime);
			setWAMPStarttime(solution.getPhenotype().startTime);
			updateDofExchange();

			//System.out.println(getDeviceID() + " got new start time: " + startTime);
		}
		if (ex instanceof StateChangedExchange && ((StateChangedExchange) ex).getStatefulentity().equals(getDeviceID())) {
			StateChangedExchange exsc = (StateChangedExchange) ex;
			
			// 1st DoF and 2nd DoF may be NULL 
						// (no DoF for device available yet and the change is because of another device)
						// DoF from Com Manager
			if (exsc.getType().equals(DofStateExchange.class)) {
				DofStateExchange dse = this.getOCRegistry().getState(
						DofStateExchange.class, exsc.getStatefulentity());
					this.setDof(
							dse.getDevice1stDegreeOfFreedom(), 
							dse.getDevice2ndDegreeOfFreedom());
			}
		}
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {

		long now = getTimer().getUnixTime();
		
		EN50523DeviceState oldstate = currentState;
		updateMOX();
		
		if (oldstate != currentState) {
			getGlobalLogger().logDebug(getLocalObserver().getDeviceType() + " statechange from: " + oldstate + " to : " + currentState);
			switch(currentState) {
			case PROGRAMMED:
			case PROGRAMMEDWAITINGTOSTART: {
				updateIPPExchange();
			}
				break;
			case RUNNING: {
				setStartTime(profileStarted);
				updateIPPExchange();
			}
				break;
			default: {
				this.startTime = -1;
			}
			}
		}
		
		if (( currentState == EN50523DeviceState.PROGRAMMED 
				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART )
				&& getStartTime() != -1 && getStartTime() <= now) {
			callDevice();			
		}
		
//		if (( currentState == EN50523DeviceState.PROGRAMMED 
//				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART )
//				&& getStartTime() == -1) {
//			setStartTime(Long.MAX_VALUE);
//			
//		}		
//		else if ( ( currentState == EN50523DeviceState.PROGRAMMED 
//				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART )
//				&& getStartTime() > 0 ) {
//			if (getStartTime() <= now) { //already to be started?
//				//start device
//				callDevice();
//				setStartTime(-1);
//			}
//		}
//		//IMA @2016-05-13
//		else if ( currentState == EN50523DeviceState.RUNNING
//				&& getStartTime() == -1) {
//			setStartTime(profileStarted);
//			updateIPPExchange();
//		}
//		//IMA @2016-05-13
//		else if ( currentState == EN50523DeviceState.RUNNING
//				&& getStartTime() >= 0) {
////			setStartTime(profileStarted);
////			updateIPPExchange();
//		}
//		else {
//			setStartTime(-1);
//			if (oldstate != currentState) { //IMA @2016-05-20
//				updateIPPExchange(); //IMA @2016-05-20
//			}
//			
//		}
	}

	/**
	 * Get MOX from Observer
	 */
	private void updateMOX() {
		MieleApplianceMOX mox = (MieleApplianceMOX) getDataFromLocalObserver();
		this.currentProfile = mox.getCurrentProfile();
		this.currentState = mox.getCurrentState();
		this.profileStarted = mox.getProfileStarted();
		this.programmedAt = mox.getProgrammedAt();
		this.compressionType = mox.getCompressionType();
		this.compressionValue = mox.getCompressionValue();
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

	private long getStartTime() {
		return this.startTime;
	}
	
	private void setStartTime(long startTime) {
		this.startTime = startTime;

		getOCRegistry().setState(
				ExpectedStartTimeExchange.class,
				this,
				new ExpectedStartTimeExchange(
						getUUID(),
						getTimer().getUnixTime(), 
						startTime));
		
		getOCRegistry().sendEvent(
				ExpectedStartTimeChangedExchange.class, 
				new ExpectedStartTimeChangedExchange(
						getUUID(), 
						getTimer().getUnixTime(), 
						startTime));
		

	}
	
	private void setWAMPStarttime(long startTime) {		
		GenericApplianceStarttimesControllerExchange halControllerExchangeObject
			= new GenericApplianceStarttimesControllerExchange(
					this.getDeviceID(),
					getTimer().getUnixTime(),
					startTime);
		this.updateOcDataSubscriber(halControllerExchangeObject);		
	}

	@Override
	public OSHComponent getSyncObject() {
		return this;
	}
	
	public void setDof(Integer firstDof, Integer secondDof) {

		boolean dofChanged = false;

		if ((firstDof != null && firstDof < 0)
				|| (secondDof != null && secondDof < 0)) {
			throw new IllegalArgumentException("firstDof or secondDof < 0");
		}

		if (firstDof == null && secondDof == null) {
			throw new IllegalArgumentException("firstDof and secondDof == null");
		}

		if (firstDof != null && this.firstDof != firstDof) {
			this.firstDof = firstDof;
			dofChanged = true;
		}

		if (secondDof != null && this.secondDof != secondDof) {
			this.secondDof = secondDof;
			dofChanged = true;
		}

		if (dofChanged && (currentState == EN50523DeviceState.PROGRAMMED 
				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART)) {
			updateIPPExchange();
		}

	}


	public void updateDofExchange() {
		// state for REST and logging
		this.getOCRegistry().setState(
						MieleDofStateExchange.class,
						this,
						new MieleDofStateExchange(
								getDeviceID(),
								getTimer().getUnixTime(), 
								this.firstDof,
								Math.min(getTimer().getUnixTime(), this.latestStart), 
								this.latestStart,
								expectedStarttime));
	}
	

	protected void updateIPPExchange() {
		InterdependentProblemPart<?, ?> ipp = null;

		long now = getTimer().getUnixTime();

		if (currentState == EN50523DeviceState.PROGRAMMED 
				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART) {
			assert programmedAt >= 0;
			
//			if( deviceStartTime != -1 )
//				latestStart = deviceStartTime;
//			else
				latestStart = programmedAt + firstDof;
				
			ipp = new MieleApplianceIPP(
					getDeviceID(),
					getGlobalLogger(),
					now, //now
					now, //earliest starting time
					latestStart,
					currentProfile.clone(),
					true, //reschedule
					false, 
					latestStart + currentProfile.getEndingTimeOfProfile(),
					getLocalObserver().getDeviceType(),
					compressionType,
					compressionValue);
			
			IAction mieleAction = new MieleAction(
					this.getDeviceID(), 
					programmedAt, 
					(MieleApplianceIPP) ipp);

			this.getOCRegistry().setState(
					LastActionExchange.class,
					this,
					new LastActionExchange(
							getUUID(),
							getTimer().getUnixTime(), 
							mieleAction));
		} 
		else {
			if (profileStarted > 0) {
				ipp = new MieleApplianceNonControllableIPP(
						getDeviceID(),
						getGlobalLogger(),
						now,
						new SparseLoadProfile().merge(currentProfile, profileStarted),
						true, // reschedule
						getLocalObserver().getDeviceType(),
						compressionType,
						compressionValue
						);
			} 
			else {
				// for a real Smart Home we should reschedule, because the user
				// could have aborted an action.
				// for a simulation we don't need that, because nobody will (at
				// the moment) abort anything
				
				// IMA: StaticEAProblemPartExchange causes rescheduling


				if ( ipp == null ) {
					ipp = new MieleApplianceNonControllableIPP(
							getDeviceID(),
							getGlobalLogger(),
							now,
							new SparseLoadProfile(),
							true, // reschedule
							getLocalObserver().getDeviceType(),
							compressionType,
							compressionValue
							);
				}			
			}
		}
		this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
		updateDofExchange();
	}	
}
