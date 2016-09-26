package osh.mgmt.localobserver;

import java.util.UUID;

import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.details.DeviceMetaOCDetails;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.HALObserverExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.interfaces.common.IHALDeviceMetaDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.GenericApplianceDofObserverExchange;
import osh.hal.exchange.MieleApplianceObserverExchange;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALMieleApplianceProgramDetails;
import osh.mgmt.mox.MieleApplianceMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;


/**
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class MieleApplianceLocalObserver 
				extends LocalObserver 
				implements IHasState, IEventTypeReceiver {

	/**
	 * SparseLoadProfile containing different profile with different commodities<br>
	 * IMPORATANT: RELATIVE TIMES!
	 */
	private SparseLoadProfile currentProfile;	
	private EN50523DeviceState currentState;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	
	/** latest start time set by device */
	private long deviceStartTime = -1;
	
	private int lastActivePowerLevel = 0;
	private int lastReactivePowerLevel = 0;

	private long profileStarted = -1;
	private long programmedAt = -1;
	

	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public MieleApplianceLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);		
	}

	
	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		return new MieleApplianceMOX(currentProfile, currentState, profileStarted, programmedAt, compressionType, compressionValue);
	}

	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		boolean programUpdated = false;
		HALObserverExchange _hx = (HALObserverExchange) getObserverDataObject();
		
		if (_hx instanceof MieleApplianceObserverExchange) {
			long currentDeviceStartTime = ((MieleApplianceObserverExchange) _hx).getDeviceStartTime(); 
			if( Math.abs(currentDeviceStartTime - deviceStartTime) >= 300 /* 5 Minutes */ )
				programUpdated = true;
			deviceStartTime = currentDeviceStartTime;
			
			//well, well...
			//TODO what to do with deviceStartTime?!?
		}
		else if (_hx instanceof StaticCompressionExchange) {
			// TODO use config of static compression
		} else if (_hx instanceof GenericApplianceDofObserverExchange) {
			//TODO will be handled below
		}
		else {
			throw new OCUnitException("Miele Device " + getDeviceType() + " received invalid OX!");
		}

		if (_hx instanceof IHALElectricalPowerDetails) {
			IHALElectricalPowerDetails idepd = (IHALElectricalPowerDetails) _hx;
			
			int currentActivePower = idepd.getActivePower();
			int currentReactivePower = idepd.getReactivePower();
			
			if (Math.abs(currentActivePower - lastActivePowerLevel) > 0 
					|| Math.abs(currentReactivePower - lastReactivePowerLevel) > 0) {
				CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
						_hx.getDeviceID(), 
						_hx.getTimestamp(),
						getDeviceType());
				cpse.addPowerState(Commodity.ACTIVEPOWER, currentActivePower);
				cpse.addPowerState(Commodity.REACTIVEPOWER, currentReactivePower);
				this.getOCRegistry().setState(
						CommodityPowerStateExchange.class,
						this,
						cpse);
				
				lastActivePowerLevel = idepd.getActivePower();
				lastReactivePowerLevel = idepd.getReactivePower();
			}
		}

		if (_hx instanceof IHALGenericApplianceDetails
				&& _hx instanceof IHALMieleApplianceProgramDetails) {
			IHALGenericApplianceDetails hgad = ((IHALGenericApplianceDetails) _hx);
			IHALMieleApplianceProgramDetails hgapd = ((IHALMieleApplianceProgramDetails) _hx);
			
			EN50523DeviceState newstate = hgad.getEN50523DeviceState();
			
			if ( currentState != newstate || programUpdated ) {
				if (newstate != null) {
					// set the current state
					this.currentState = newstate;
					
					switch (newstate) {
					case OFF: {
						this.profileStarted = -1;
						this.programmedAt = -1;
						this.currentProfile = new SparseLoadProfile();
					}
						break;
					case STANDBY: {
						this.profileStarted = -1;
						this.programmedAt = -1;
						this.currentProfile = new SparseLoadProfile();
					}					
						break;
					case PROGRAMMEDWAITINGTOSTART: 
					case PROGRAMMED: {
						this.currentProfile = SparseLoadProfile
								.convertToSparseProfile(
										hgapd.getExpectedLoadProfiles(),
										LoadProfileCompressionTypes.DISCONTINUITIES,
										1,
										-1);
						
						this.profileStarted = -1;
						if (programmedAt == -1)
							this.programmedAt = _hx.getTimestamp();
					}
						break;
					case RUNNING: {
						this.currentProfile = SparseLoadProfile
								.convertToSparseProfile(
										hgapd.getExpectedLoadProfiles(),
										LoadProfileCompressionTypes.DISCONTINUITIES,
										1,
										-1);
						if (profileStarted == -1)
							this.profileStarted = _hx.getTimestamp();
					}
						break;
					case ENDPROGRAMMED: {
						this.programmedAt = -1;
						this.profileStarted = -1;
					}
						break;
					default:
						this.programmedAt = -1;
						this.profileStarted = -1;
						break;
					}
					
					getGlobalLogger().logDebug(
							"Appliance " + _hx.getDeviceID().toString()
									+ " " + this.currentState + ": ["
									+ this.programmedAt + "]");
				} 
				else { // newstate == null
					//TODO: Decide about state...
					// 1. based on consumption...
					// 2. based on EMP...
				}
				
			}
		}

		if (_hx instanceof IHALDeviceMetaDetails) {
			IHALDeviceMetaDetails ihdmd = (IHALDeviceMetaDetails) _hx;
			
			DeviceMetaOCDetails _devDetails = new DeviceMetaOCDetails(_hx.getDeviceID(), _hx.getTimestamp());
			_devDetails.setName(ihdmd.getName());
			_devDetails.setLocation(ihdmd.getLocation());
			_devDetails.setDeviceType(ihdmd.getDeviceType());
			_devDetails.setDeviceClassification(ihdmd.getDeviceClassification());
			_devDetails.setConfigured(ihdmd.isConfigured());

			this.getOCRegistry().setState(DeviceMetaOCDetails.class, this, _devDetails);
		}
		
		if (_hx instanceof StaticCompressionExchange) {
			StaticCompressionExchange stDe = (StaticCompressionExchange) _hx;
			
			this.compressionType = stDe.getCompressionType();
			this.compressionValue = stDe.getCompressionValue();
		}
		
		if (_hx instanceof GenericApplianceDofObserverExchange) {
			GenericApplianceDofObserverExchange gadoe = ((GenericApplianceDofObserverExchange) _hx);
			
			DofStateExchange dse = new DofStateExchange(getUUID(), getTimer().getUnixTime());
			dse.setDevice1stDegreeOfFreedom(gadoe.getDevice1stDegreeOfFreedom());
			dse.setDevice2ndDegreeOfFreedom(gadoe.getDevice2ndDegreeOfFreedom());
			
		    this.getOCRegistry().setState(DofStateExchange.class, this, dse);
		}
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T ex) throws OSHException {
		//nothing
	}	

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " for " + getDeviceID()
				+ " (" + getDeviceType() + ")";
	}
}
