package osh.mgmt.localobserver;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.hal.exchange.BatteryStorageOX;
import osh.mgmt.mox.BatteryStorageMOX;
import osh.registry.interfaces.IHasState;

/**
 * @author Jan Mueller, Matthias Maerz
 *
 */
public class InverterBatteryStorageObserver 
					extends LocalObserver
					implements IHasState {

	private double batteryStateOfCharge;
	private double batteryStateOfHealth;
	private int batteryStandingLoss;
	private int batteryMinChargingState;
	private int batteryMaxChargingState;
	private int batteryMinChargePower;
	private int batteryMaxChargePower;
	private int batteryMinDischargePower;
	private int batteryMaxDischargePower;
	private int inverterMinComplexPower;
	private int inverterMaxComplexPower; 
	private int inverterMinPower;
	private int inverterMaxPower;	
	private int activePower;
	private int reactivePower;
	
	private int rescheduleAfter;
	private long newIppAfter;
	private int triggerIppIfDeltaSoCBigger;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public InverterBatteryStorageObserver(IOSHOC controllerbox) {
		super(controllerbox);
		//NOTHING
	}
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		long now = getTimer().getUnixTime();
		
		// get OX
		BatteryStorageOX ox = (BatteryStorageOX) getObserverDataObject();
		this.batteryStateOfCharge = ox.getBatteryStateOfCharge();
		this.batteryStateOfHealth = ox.getBatteryStateOfHealth();
		this.batteryStandingLoss = ox.getBatteryStandingLoss();
		this.batteryMinChargingState = ox.getBatteryMinChargingState();
		this.batteryMaxChargingState = ox.getBatteryMaxChargingState();
		this.batteryMinChargePower = ox.getBatteryMinChargePower();
		this.batteryMaxChargePower = ox.getBatteryMaxChargePower();
		this.batteryMinDischargePower = ox.getBatteryMinDischargePower();
		this.batteryMaxDischargePower = ox.getBatteryMaxDischargePower();
		this.inverterMinComplexPower = ox.getInverterMinComplexPower();
		this.inverterMaxComplexPower = ox.getInverterMaxComplexPower(); 
		this.inverterMinPower = ox.getInverterMinPower();
		this.inverterMaxPower = ox.getInverterMaxPower();	
		this.activePower= ox.getActivePower();
		this.reactivePower=ox.getReactivePower();

		this.rescheduleAfter = ox.getRescheduleAfter();
		this.newIppAfter = ox.getNewIppAfter();
		this.triggerIppIfDeltaSoCBigger = ox.getTriggerIppIfDeltaSoCBigger();
		
		this.compressionType = ox.getCompressionType();
		this.compressionValue = ox.getCompressionValue();
		
		
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
				now, 
				ox.getBatteryStateOfCharge(),
//				ox.getBatteryStateOfHealth(),
				ox.getBatteryMinChargingState(),
				ox.getBatteryMaxChargingState(),
				getDeviceID());
		getOCRegistry().setState(
				BatteryStorageOCSX.class, 
				this, 
				sx);									
	}
	
	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		BatteryStorageMOX newMox = new BatteryStorageMOX(
				getDeviceID(), 
				getTimer().getUnixTime(),
						activePower,
						reactivePower,
						batteryStateOfCharge,
						batteryStateOfHealth,
						batteryStandingLoss,
						batteryMinChargingState,
						batteryMaxChargingState,
						batteryMinChargePower,
						batteryMaxChargePower,
						batteryMinDischargePower,
						batteryMaxDischargePower,
						inverterMinComplexPower,
						inverterMaxComplexPower,
						inverterMinPower,
						inverterMaxPower,
						rescheduleAfter,
						newIppAfter,
						triggerIppIfDeltaSoCBigger,
						compressionType,
						compressionValue);
		return newMox;
	}
	
	
	
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
}
