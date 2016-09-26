package osh.mgmt.localobserver;

import java.util.EnumMap;
import java.util.UUID;

import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.HALObserverExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.interfaces.common.IHALDeviceMetaDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALThermalPowerDetails;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.FutureApplianceObserverExchange;
import osh.hal.interfaces.appliance.IHALGenericApplianceDOF;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALGenericApplianceProgramDetails;
import osh.mgmt.mox.GenericApplianceMOX;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser , Matthias Maerz
 *
 */
public class FutureApplianceLocalObserver 
				extends LocalObserver 
				implements IEventTypeReceiver, IHasState {
	
	// ### State variables ###
	
	private EN50523DeviceState currentState;
	
	/** current power consumption */
	private EnumMap<Commodity,Integer> currentPower = new EnumMap<>(Commodity.class); 
	
	// ### Parameter variables, e.g. for handling of data ###
	
	private ApplianceProgramConfigurationStatus applianceConfigurationProfile;
	private UUID acpID; // save ID separately for detection of new ACP
	private Long acpReferenceTime;
	
	private Long dof;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public FutureApplianceLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		//NOTHING
	}
	

	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		
		// ### get OX and check validity ###
		HALObserverExchange _hx = (HALObserverExchange) getObserverDataObject();
		if ( ! (_hx instanceof FutureApplianceObserverExchange) && !(_hx instanceof StaticCompressionExchange) ) {
			throw new OCUnitException("Generic Device " + getDeviceType() + " received invalid OX!");
		}
		
		// ### Power states of commodities ###
		CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
				_hx.getDeviceID(), 
				_hx.getTimestamp(),
				getDeviceType());
		
		// set electric power
		if (_hx instanceof IHALElectricalPowerDetails) {
			IHALElectricalPowerDetails idepd = (IHALElectricalPowerDetails) _hx;
			int currentActivePower = idepd.getActivePower();
			int currentReactivePower = idepd.getReactivePower();
			this.currentPower.put(Commodity.ACTIVEPOWER, currentActivePower);
			this.currentPower.put(Commodity.REACTIVEPOWER, currentReactivePower);
			cpse.addPowerState(Commodity.ACTIVEPOWER, currentActivePower);
			cpse.addPowerState(Commodity.REACTIVEPOWER, currentReactivePower);
		}
		// set hot water power
		if (_hx instanceof IHALThermalPowerDetails) {
			// heating hot water
			IHALThermalPowerDetails ihgpd = (IHALThermalPowerDetails) _hx;
			int hotWaterPower = ihgpd.getHotWaterPower();
			this.currentPower.put(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			// domestic hot water (potable)
			int domesticHotWaterPower = ihgpd.getDomesticHotWaterPower();
			this.currentPower.put(Commodity.DOMESTICHOTWATERPOWER, domesticHotWaterPower);
			cpse.addPowerState(Commodity.DOMESTICHOTWATERPOWER, domesticHotWaterPower);
		}
		// set gas power
		if (_hx instanceof IHALGasPowerDetails) {
			IHALGasPowerDetails ihgpd = (IHALGasPowerDetails) _hx;
			int currentGasPower = ihgpd.getGasPower();
			this.currentPower.put(Commodity.NATURALGASPOWER, currentGasPower);
			cpse.addPowerState(Commodity.NATURALGASPOWER, currentGasPower);
		}
		// get the dof from OX
		if (_hx instanceof IHALGenericApplianceDOF) {
			IHALGenericApplianceDOF ihgad = (IHALGenericApplianceDOF) _hx;
			
			if(ihgad.getDOF()!=null){
				this.dof = ihgad.getDOF();
			}
			else{
				this.dof= 0L ;
			}
			
		}
		
		this.getOCRegistry().setState(
				CommodityPowerStateExchange.class,
				this,
				cpse);
		
		
		// ### ApplianceDetails ###
		EN50523DeviceState oldState = currentState;
		if (_hx instanceof IHALGenericApplianceDetails) {
			IHALGenericApplianceDetails hgad = ((IHALGenericApplianceDetails) _hx);
			this.currentState = hgad.getEN50523DeviceState();
			
			if (oldState != currentState) {
				getGlobalLogger().logDebug(
						getDeviceType() +
						" :" //+ _hx.getDeviceID().toString()
						+ " changed state from state: " + oldState
						+ " to state: " + currentState);
			}
		}
		
		// ### Appliance Program Details ###
		if (_hx instanceof IHALGenericApplianceProgramDetails) {
			IHALGenericApplianceProgramDetails hgapd = ((IHALGenericApplianceProgramDetails) _hx);
			this.applianceConfigurationProfile = hgapd.getApplianceConfigurationProfile();
			this.acpID = hgapd.getAcpID();
			this.acpReferenceTime = hgapd.getAcpReferenceTime();
		}
		
		// ### Device MetaDetails ###
		if (_hx instanceof IHALDeviceMetaDetails) {
			IHALDeviceMetaDetails ihdmd = (IHALDeviceMetaDetails) _hx;
			DeviceMetaDriverDetails _devDetails = new DeviceMetaDriverDetails(_hx.getDeviceID(), _hx.getTimestamp());
			_devDetails.setName(ihdmd.getName());
			_devDetails.setLocation(ihdmd.getLocation());
			_devDetails.setDeviceType(ihdmd.getDeviceType());
			_devDetails.setDeviceClassification(ihdmd.getDeviceClassification());
			_devDetails.setConfigured(ihdmd.isConfigured());
			this.getOCRegistry().setState(DeviceMetaDriverDetails.class, this, _devDetails);
		}
		if (_hx instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _hx;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}
	
	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		GenericApplianceMOX mox = new GenericApplianceMOX(
				this.currentState,
				this.applianceConfigurationProfile,
				this.acpID,
				this.acpReferenceTime,
				this.dof,
				this.compressionType,
				this.compressionValue
				);
		return mox;
	}
	

	@Override
	public UUID getUUID() {
		return this.getDeviceID();
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() 
				+ " for " + getDeviceID()
				+ " (" + getDeviceType() + ")";
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		//NOTHING
	}
	
}
