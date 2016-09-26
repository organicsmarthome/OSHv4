package osh.mgmt.localobserver;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.GasBoilerObserverExchange;
import osh.mgmt.ipp.GasBoilerNonControllableIPP;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class NonControllableGasBoilerLocalObserver 
					extends LocalObserver
					implements IHasState {
	
	private int NEW_IPP_AFTER;
	private long lastTimeIPPSent = Long.MIN_VALUE;
	private boolean initialStateLastIPP = false;
	private double lastIPPMinTemperature = 60;
	private double lastIPPMaxTemperature = 80;

	private double minTemperature = 60;
	private double maxTemperature = 80;
	private boolean initialState = false;
	
	private int maxHotWaterPower = 15000;
	private int maxGasPower = 15000;
	
	private int typicalActivePowerOn = 100;
	private int typicalActivePowerOff = 0;
	private int typicalReactivePowerOn = 0;
	private int typicalReactivePowerOff = 0;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public NonControllableGasBoilerLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		//NOTHING
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
			GasBoilerNonControllableIPP sipp = new GasBoilerNonControllableIPP(
					getDeviceID(), 
					getGlobalLogger(),
					now,
					minTemperature,
					maxTemperature,
					initialState,
					maxHotWaterPower,
					maxGasPower,
					typicalActivePowerOn,
					typicalActivePowerOff,
					typicalReactivePowerOn,
					typicalReactivePowerOff,
					compressionType,
					compressionValue);
			getOCRegistry().setState(
					InterdependentProblemPart.class, this, sipp);
			lastTimeIPPSent = now;
			lastIPPMaxTemperature = maxTemperature;
			lastIPPMinTemperature = minTemperature;
		}

	}

	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		long now = getTimer().getUnixTime();
		
		IHALExchange _ihal = getObserverDataObject();
		
		if (_ihal instanceof GasBoilerObserverExchange) {
			GasBoilerObserverExchange ox = (GasBoilerObserverExchange) _ihal;
			
			minTemperature = ox.getMinTemperature();
			maxTemperature = ox.getMaxTemperature();
			initialState = ox.getCurrentState();
			maxHotWaterPower = ox.getMaxHotWaterPower();
			maxGasPower = ox.getMaxGasPower();
			typicalActivePowerOn = ox.getTypicalActivePowerOn();
			typicalActivePowerOff = ox.getTypicalActivePowerOff();
			typicalReactivePowerOn = ox.getTypicalReactivePowerOn();
			typicalReactivePowerOff = ox.getTypicalReactivePowerOff();
			NEW_IPP_AFTER = ox.getNewIppAfter();
			
			if (initialStateLastIPP != initialState || lastIPPMaxTemperature != maxTemperature || lastIPPMinTemperature != minTemperature) {
				// build SIPP
				GasBoilerNonControllableIPP sipp = new GasBoilerNonControllableIPP(
						getDeviceID(), 
						getGlobalLogger(),
						now,
						minTemperature,
						maxTemperature,
						initialState,
						maxHotWaterPower,
						maxGasPower,
						typicalActivePowerOn,
						typicalActivePowerOff,
						typicalReactivePowerOn,
						typicalReactivePowerOff,
						compressionType,
						compressionValue);
				getOCRegistry().setState(
						InterdependentProblemPart.class, this, sipp);
				initialStateLastIPP = initialState;
				lastIPPMaxTemperature = maxTemperature;
				lastIPPMinTemperature = minTemperature;
			}
			
			// build SX
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					getTimer().getUnixTime(),
					DeviceTypes.INSERTHEATINGELEMENT);
			
			cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
			cpse.addPowerState(Commodity.REACTIVEPOWER, ox.getReactivePower());
			cpse.addPowerState(Commodity.NATURALGASPOWER, ox.getGasPower());
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, ox.getHotWaterPower());
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
		} else if (_ihal instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}		
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
