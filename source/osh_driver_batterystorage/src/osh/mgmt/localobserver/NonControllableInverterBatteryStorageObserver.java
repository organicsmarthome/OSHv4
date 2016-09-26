package osh.mgmt.localobserver;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.hal.exchange.BatteryStorageOX;
import osh.mgmt.ipp.BatteryStorageNonControllableIPP;
import osh.registry.interfaces.IHasState;

/**
 * @author Jan Müller, Sebastian Kramer
 *
 */
public class NonControllableInverterBatteryStorageObserver 
					extends LocalObserver
					implements IHasState {

	
	private long NEW_IPP_AFTER;
	private long lastTimeIPPSent = Long.MIN_VALUE;
	private double lastSOCIPP = Integer.MIN_VALUE;
	private int TRIGGER_IPP_IF_DELTASoC_BIGGER;
	
	/**
	 * CONSTRUCTOR
	 */
	public NonControllableInverterBatteryStorageObserver(IOSHOC controllerbox) {
		super(controllerbox);
		//NOTHING
	}
	
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		
		long now = getTimer().getUnixTime();
		
		// get OX
		BatteryStorageOX ox = (BatteryStorageOX) getObserverDataObject();
		this.NEW_IPP_AFTER = ox.getNewIppAfter();
		this.TRIGGER_IPP_IF_DELTASoC_BIGGER = ox.getTriggerIppIfDeltaSoCBigger();
		
		if (lastTimeIPPSent + NEW_IPP_AFTER < now || Math.abs((ox.getBatteryStateOfCharge() - lastSOCIPP)) > TRIGGER_IPP_IF_DELTASoC_BIGGER) {
			// build SIPP
			BatteryStorageNonControllableIPP sipp = new BatteryStorageNonControllableIPP(
					getDeviceID(), 
					getGlobalLogger(), 
					now, 
					ox.getBatteryStateOfCharge(), 
					ox.getBatteryStateOfHealth(), 
					ox.getBatteryStandingLoss(),
					ox.getBatteryMinChargingState(),
					ox.getBatteryMaxChargingState(),
					ox.getBatteryMinChargePower(),
					ox.getBatteryMaxChargePower(),
					ox.getBatteryMinDischargePower(),
					ox.getBatteryMaxDischargePower(),
					ox.getInverterMinComplexPower(),
					ox.getInverterMaxComplexPower(), 
					ox.getInverterMinPower(),
					ox.getInverterMaxPower(),
					ox.getCompressionType(),
					ox.getCompressionValue()
					);
			getOCRegistry().setState(
					InterdependentProblemPart.class, this, sipp);
			lastTimeIPPSent = now;
			lastSOCIPP = ox.getBatteryStateOfCharge();
		}
		
		// build SX
		CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
				getDeviceID(), 
				now,
				DeviceTypes.BATTERYSTORAGE);
		
		cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
		cpse.addPowerState(Commodity.REACTIVEPOWER, ox.getReactivePower());
		this.getOCRegistry().setState(
				CommodityPowerStateExchange.class,
				this,
				cpse);
		
		// save current state in OCRegistry (for e.g. GUI)
				BatteryStorageOCSX sx = new BatteryStorageOCSX(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						ox.getBatteryStateOfCharge(),
//						ox.getBatteryStateOfHealth(),
						ox.getBatteryMinChargingState(),
						ox.getBatteryMaxChargingState(),
						getDeviceID());
				getOCRegistry().setState(
						BatteryStorageOCSX.class, 
						this, 
						sx);		
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
