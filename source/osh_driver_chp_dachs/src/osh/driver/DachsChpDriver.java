package osh.driver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.commands.ChpElectricityRequest;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChpControllerExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.registry.interfaces.IHasState;
import osh.utils.physics.ComplexPowerUtil;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser, Jan Mueller
 *
 */
public abstract class DachsChpDriver extends ChpDriver implements IHasState {

	private String dachsURL;
	
	//static values
	private double cosPhi;
	private int typicalActivePower;
	private int typicalReactivePower;
	private int typicalThermalPower;
	private int typicalAddditionalThermalPower;
	private int typicalGasPower;
	
//	private double typicalTemperatureIn = 45.0;
//	private double typicalTemperatureOut = 85.0;
//	private double typicalMassFlow = 1;
	private UUID hotWaterTankUuid;
	
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
	
//	private final double standbyActivePower = 20.0;
	
//	private double currentTemperatureIn = 0.0;
//	private double currentTemperatureOut = 0.0;
//	private double currentMassFlow = 0.0;
	
//	private int runtimeRemaining = 0;
//	private int offtimeRemaining = 0;
	
//	private boolean runningRequestFromController = false;
	
	// received from other devices...
//	private double waterInTemperature = 60.0;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public DachsChpDriver(
			IOSH osh, 
			UUID deviceID,
			OSHParameterCollection driverConfig) 
			throws OSHException, HALException {
		super(osh, deviceID, driverConfig);
		
		try {
			this.typicalActivePower = Integer.valueOf(getDriverConfig().getParameter("typicalActivePower"));
		}
		catch (Exception e) {
			this.typicalActivePower = -5500;
			getGlobalLogger().logWarning("Can't get typicalActivePower, using the default value: " + this.typicalActivePower);
		}
		
		try {
			this.typicalThermalPower = Integer.valueOf(getDriverConfig().getParameter("typicalThermalPower"));
		}
		catch (Exception e) {
			this.typicalThermalPower = -12500;
			getGlobalLogger().logWarning("Can't get typicalThermalPower, using the default value: " + this.typicalThermalPower);
		}
		
		try {
			this.typicalAddditionalThermalPower = Integer.valueOf(getDriverConfig().getParameter("typicalAddditionalThermalPower"));
		}
		catch (Exception e) {
			this.typicalAddditionalThermalPower = 0;
			getGlobalLogger().logWarning("Can't get typicalAddditionalThermalPower, using the default value: " + this.typicalAddditionalThermalPower);
		}
		
		try {
			this.typicalGasPower = Integer.valueOf(getDriverConfig().getParameter("typicalGasPower"));
		}
		catch (Exception e) {
			this.typicalGasPower = 20500;
			getGlobalLogger().logWarning("Can't get typicalGasPower, using the default value: " + this.typicalGasPower);
		}
		
		try {
			this.hotWaterTankUuid = UUID.fromString(getDriverConfig().getParameter("hotWaterTankUuid"));
		}
		catch (Exception e) {
			this.hotWaterTankUuid = UUID.fromString("00000000-0000-4857-4853-000000000000");
			getGlobalLogger().logWarning("Can't get hotWaterTankUuid, using the default value: " + this.getHotWaterTankUuid());
		}
		
		try {
			this.rescheduleAfter = Integer.valueOf(getDriverConfig().getParameter("rescheduleAfter"));
		}
		catch (Exception e) {
			this.rescheduleAfter = 4 * 3600; // 4 hours
			getGlobalLogger().logWarning("Can't get rescheduleAfter, using the default value: " + this.rescheduleAfter);
		}
		
		try {
			this.newIPPAfter = Long.valueOf(getDriverConfig().getParameter("newIPPAfter"));
		}
		catch (Exception e) {
			this.newIPPAfter = 1 * 3600; // 1 hour
			getGlobalLogger().logWarning("Can't get newIPPAfter, using the default value: " + this.newIPPAfter);
		}
		
		try {
			this.relativeHorizonIPP = Integer.valueOf(getDriverConfig().getParameter("relativeHorizonIPP"));
		}
		catch (Exception e) {
			this.relativeHorizonIPP = 18 * 3600; // 18 hours
			getGlobalLogger().logWarning("Can't get relativeHorizonIPP, using the default value: " + this.relativeHorizonIPP);
		}
		
		try {
			this.currentHotWaterStorageMinTemp = Double.valueOf(getDriverConfig().getParameter("currentHotWaterStorageMinTemp"));
		}
		catch (Exception e) {
			this.currentHotWaterStorageMinTemp = 60;
			getGlobalLogger().logWarning("Can't get currentHotWaterStorageMinTemp, using the default value: " + this.currentHotWaterStorageMinTemp);
		}
		
		try {
			this.currentHotWaterStorageMaxTemp = Double.valueOf(getDriverConfig().getParameter("currentHotWaterStorageMaxTemp"));
		}
		catch (Exception e) {
			this.currentHotWaterStorageMaxTemp = 80;
			getGlobalLogger().logWarning("Can't get currentHotWaterStorageMaxTemp, using the default value: " + this.currentHotWaterStorageMaxTemp);
		}
		
		try {
			this.forcedOnHysteresis = Double.valueOf(getDriverConfig().getParameter("forcedOnHysteresis"));
		}
		catch (Exception e) {
			this.forcedOnHysteresis = 5.0;
			getGlobalLogger().logWarning("Can't get forcedOnHysteresis, using the default value: " + this.forcedOnHysteresis);
		}
		
		try {
			this.fixedCostPerStart = Double.valueOf(getDriverConfig().getParameter("fixedCostPerStart"));
		}
		catch (Exception e) {
			this.fixedCostPerStart = 8.0;
			getGlobalLogger().logWarning("Can't get fixedCostPerStart, using the default value: " + this.fixedCostPerStart);
		}
		
		try {
			this.forcedOnOffStepMultiplier = Double.valueOf(getDriverConfig().getParameter("forcedOnOffStepMultiplier"));
		}
		catch (Exception e) {
			this.forcedOnOffStepMultiplier = 0.1;
			getGlobalLogger().logWarning("Can't get forcedOnOffStepMultiplier, using the default value: " + this.forcedOnOffStepMultiplier);
		}
		
		try {
			this.forcedOffAdditionalCost = Integer.valueOf(getDriverConfig().getParameter("forcedOffAdditionalCost"));
		}
		catch (Exception e) {
			this.forcedOffAdditionalCost = 10;
			getGlobalLogger().logWarning("Can't get forcedOffAdditionalCost, using the default value: " + this.forcedOffAdditionalCost);
		}
		
		try {
			this.chpOnCervisiaStepSizeMultiplier = Double.valueOf(getDriverConfig().getParameter("chpOnCervisiaStepSizeMultiplier"));
		}
		catch (Exception e) {
			this.chpOnCervisiaStepSizeMultiplier = 0.0000001;
			getGlobalLogger().logWarning("Can't get chpOnCervisiaStepSizeMultiplier, using the default value: " + this.chpOnCervisiaStepSizeMultiplier);
		}
		
		try {
			this.minRuntime = Integer.valueOf(getDriverConfig().getParameter("minRuntime"));
		}
		catch (Exception e) {
			this.minRuntime = 15 * 60;
			getGlobalLogger().logWarning("Can't get minRuntime, using the default value: " + this.minRuntime);
		}
		
		try {
			this.cosPhi = Double.valueOf(getDriverConfig().getParameter("cosPhi"));
		}
		catch (Exception e) {
			this.cosPhi = 0.9;
			getGlobalLogger().logWarning("Can't get cosPhi, using the default value: " + this.cosPhi);
		}
		
		try {
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}
		
		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
		
		try {
			typicalReactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(typicalActivePower, cosPhi, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		String dachsHost = driverConfig.getParameter("dachshost");
		String dachsPort = driverConfig.getParameter("dachsport");
		if ( dachsHost == null 
				|| dachsPort == null 
				|| dachsHost.length() == 0 
				|| dachsPort.length() == 0) {
			throw new OSHException("Invalid Dachs Host or Port");
		}
		this.dachsURL = "http://" + dachsHost + ":" + dachsPort + "/";
		
		this.setMinimumRuntime(30 * 60); // 30 minutes
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		ChpStaticDetailsObserverExchange observerExchange = 
				new ChpStaticDetailsObserverExchange(getDeviceID(), getTimer().getUnixTime());
		observerExchange.setTypicalActivePower(typicalActivePower);
		observerExchange.setTypicalReactivePower(typicalReactivePower);
		observerExchange.setTypicalThermalPower(typicalThermalPower);
		observerExchange.setTypicalGasPower(typicalGasPower);
		observerExchange.setOperationMode(ChpOperationMode.HEAT_AND_ELECTRICITY_LED);
		observerExchange.setHotWaterTankUuid(getHotWaterTankUuid());
		observerExchange.setRescheduleAfter(rescheduleAfter);
		observerExchange.setNewIPPAfter(newIPPAfter);
		observerExchange.setRelativeHorizonIPP(relativeHorizonIPP);
		observerExchange.setCurrentHotWaterStorageMinTemp(currentHotWaterStorageMinTemp);
		observerExchange.setCurrentHotWaterStorageMaxTemp(currentHotWaterStorageMaxTemp);
		observerExchange.setForcedOnHysteresis(forcedOnHysteresis);
		
		observerExchange.setFixedCostPerStart(fixedCostPerStart);
		observerExchange.setForcedOnOffStepMultiplier(forcedOnOffStepMultiplier);
		observerExchange.setForcedOffAdditionalCost(forcedOffAdditionalCost);
		observerExchange.setChpOnCervisiaStepSizeMultiplier(chpOnCervisiaStepSizeMultiplier);
		observerExchange.setMinRuntime(minRuntime);
		
		this.notifyObserver(observerExchange);
		
		StaticCompressionExchange stat = new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime());
		stat.setCompressionType(compressionType);
		stat.setCompressionValue(compressionValue);
		this.notifyObserver(stat);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		// still alive message
		if (getTimer().getUnixTime() % 60 == 0) {
			getGlobalLogger().logDebug("onNextTimePeriod() (getTimer().getUnixTime() % 60 == 0) - I'm still alive");
		}
	} 
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {
		if (controllerRequest instanceof ChpControllerExchange) {
			ChpControllerExchange chpControllerExchange = (ChpControllerExchange) controllerRequest;
//			getGlobalLogger().logDebug("onControllerRequest(ChpControllerExchange)");
			
			setElectricityRequest(chpControllerExchange.isElectricityRequest());
			setHeatingRequest(chpControllerExchange.isHeatingRequest());
			
			sendPowerRequestToChp();
		}
	}
	
	// for callback of DachsInformationRequestThread
	public abstract void processDachsDetails(DachsDriverDetails dachsDetails);
	
	protected Integer parseIntegerStatus(String value) {
		if (value == null || value.equals("")) return null;
		
		int i;
		try {
			i = Integer.parseInt(value);
			return i;
		} 
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	protected Double parseDoubleStatus(String value) {
		if (value == null || value.equals("")) return null;
		
		double i;
		try {
			i = Double.parseDouble(value);
			return i;
		} 
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		
		if (event instanceof ChpElectricityRequest) {
			ChpElectricityRequest ceq = (ChpElectricityRequest) event;
			
			getGlobalLogger().logDebug("onQueueEventReceived(ChpElectricityRequest)");
			getGlobalLogger().logDebug("sendPowerRequestToChp(" + ceq.isOn() + ")");
			
			setElectricityRequest( ceq.isOn() );
			
			sendPowerRequestToChp();
		}		
		
	}

	public UUID getHotWaterTankUuid() {
		return hotWaterTankUuid;
	}


	protected String getDachsURL() {
		return dachsURL;
	}

}
