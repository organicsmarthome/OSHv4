package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.chp.ChpOperationMode;
import osh.driver.chp.model.GenericChpModel;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;
import osh.hal.exchange.ChpControllerExchange;
import osh.hal.exchange.ChpObserverExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;

/**
 * Simulates the Dachs microCHP
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class DachsChpSimulationDriver 
					extends ChpSimulationDriver {
	
	// static values
	private double cosPhi;
	private int typicalActivePower;
	private int typicalReactivePower;
	private int typicalThermalPower;
	private int typicalAddditionalThermalPower;
	private int typicalGasPower;
	
	@SuppressWarnings("unused")
	private double typicalTemperatureIn = 45.0;
	private double typicalTemperatureOut = 85.0;
	private double typicalMassFlow = 1;
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
	
	//private final double standbyActivePower = 20.0;
	
	// real variables
	@SuppressWarnings("unused")
	private double currentTemperatureIn = 0.0;
	private double currentTemperatureOut = 0.0;
	private double currentMassFlow = 0.0;
	
	private boolean electricityRequest = false;
	private boolean heatingRequest = false;
	private int runtimeRemaining = 0;
	@SuppressWarnings("unused")
	private int offtimeRemaining = 0;
	
	private boolean runningRequestFromController = false;
	
	// received from other devices...
	private double waterInTemperature = 60.0;
	
	private GenericChpModel chpModel;
	
	private double supply = 0;
	private int starts = 0;
	private boolean log = false;
	

	/**
	 * CONSTRUCTOR
	 */
	public DachsChpSimulationDriver(
			IOSH osh, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
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
			getGlobalLogger().logWarning("Can't get hotWaterTankUuid, using the default value: " + this.hotWaterTankUuid);
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
			typicalReactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(typicalActivePower, cosPhi, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		chpModel = new GenericChpModel(
				typicalActivePower, 
				typicalReactivePower, 
				typicalThermalPower, 
				typicalGasPower, 
				cosPhi, 
				false, 
				0, 
				null, 
				null);
	}

	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		super.onSimulationIsUp();
		
		ChpStaticDetailsObserverExchange observerExchange = 
				new ChpStaticDetailsObserverExchange(getDeviceID(), getTimer().getUnixTime());
		observerExchange.setTypicalActivePower(typicalActivePower);
		observerExchange.setTypicalReactivePower(typicalReactivePower);
		observerExchange.setTypicalThermalPower(typicalThermalPower);
		observerExchange.setTypicalGasPower(typicalGasPower);
		observerExchange.setOperationMode(ChpOperationMode.HEAT_AND_ELECTRICITY_LED);
		observerExchange.setHotWaterTankUuid(hotWaterTankUuid);
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
		
		log = DatabaseLoggerThread.isLogWaterTank();
	}
	
	
	@Override
	public void onSystemShutdown() {
		if (log) {
			supply /= 3600000.0;
			DatabaseLoggerThread.enqueueChp(supply, starts);
		}
	};
	

	@Override
	public void onNextTimeTick() {
		
		// do device control
		if (electricityRequest || heatingRequest || runningRequestFromController) {
			if (!isRunning()) {
				if (runningRequestFromController) {
					runtimeRemaining = 30 * 60;
				}
				else {
					runtimeRemaining = 0 * 60;
				}				
			}
			else {
				if (runtimeRemaining > 0) {
					runtimeRemaining = runtimeRemaining - 1;
				}
			}
			setRunning(true);
		}
		else {
			setRunning(false);
			runtimeRemaining = 0;
		}
		
		chpModel.calcPower(getTimer().getUnixTime());
		int activePower = chpModel.getActivePower();
		int reactivePower = chpModel.getReactivePower();
		int thermalPower = chpModel.getThermalPower();
		int gasPower = chpModel.getGasPower();
		this.setPower(Commodity.ACTIVEPOWER, activePower);
		this.setPower(Commodity.REACTIVEPOWER, reactivePower);
		this.setPower(Commodity.HEATINGHOTWATERPOWER, thermalPower);
		this.setPower(Commodity.NATURALGASPOWER, gasPower);
		
		if (log) {
			supply += thermalPower;
		}
		
		// set power
		if (isRunning()) {			
			// calculate mass flow
			//TODO
			this.currentMassFlow = this.typicalMassFlow;
			
			this.currentTemperatureIn = this.waterInTemperature;
			this.currentTemperatureOut = this.typicalTemperatureOut;
		}
		else {			
			this.currentMassFlow = 0;
			this.currentTemperatureIn = 0;
			this.currentTemperatureOut = 0;
		}
		
		// send ObserverExchange
		ChpObserverExchange observerExchange = new ChpObserverExchange(
				getDeviceID(), 
				getTimer().getUnixTime());
		
		observerExchange.setActivePower((int) Math.round(this.getPower(Commodity.ACTIVEPOWER)));
		observerExchange.setReactivePower((int) Math.round(this.getPower(Commodity.REACTIVEPOWER))); 
		observerExchange.setThermalPower((int) Math.round(this.getPower(Commodity.HEATINGHOTWATERPOWER)));
		observerExchange.setGasPower((int) Math.round(this.getPower(Commodity.NATURALGASPOWER)));
		
		observerExchange.setTemperatureOut(currentTemperatureOut);
		observerExchange.setElectricityRequest(electricityRequest || runningRequestFromController);
		observerExchange.setHeatingRequest(heatingRequest);		
		observerExchange.setRunning(isRunning());
		observerExchange.setMinRuntimeRemaining(runtimeRemaining);
		this.notifyObserver(observerExchange);
		
	}
	

	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest)
			throws HALException {
		super.onControllerRequest(controllerRequest);
		
		ChpControllerExchange cx = (ChpControllerExchange) controllerRequest;
		boolean stop = cx.isStopGenerationFlag();
		boolean er = cx.isElectricityRequest();
		boolean hr = cx.isHeatingRequest();
		
		if (stop) {
			// actually not possible with Dachs...
			runningRequestFromController = false;
			electricityRequest = false;
			heatingRequest = false;
		}
		else {
			if (er || hr) {
				runningRequestFromController = true;
			}
			else  {
				runningRequestFromController = false;
			}
		}
	}
	
	
	@Override
	protected void setRunning(boolean running) {
		// for logging
		if (log && running != isRunning() && running) {
			starts++;
		}
		super.setRunning(running);
		chpModel.setRunning(running, getTimer().getUnixTime());
	}
	
	@Override
	public LimitedCommodityStateMap getCommodityOutputStates() {
//		EnumMap<Commodity, RealCommodityState> map = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
		LimitedCommodityStateMap map = new LimitedCommodityStateMap(usedCommodities);
//		map.put(
//				Commodity.ACTIVEPOWER, 
//				new RealElectricalCommodityState(
//						Commodity.ACTIVEPOWER, 
//						this.getPower(Commodity.ACTIVEPOWER) != null 
//								? (double) this.getPower(Commodity.ACTIVEPOWER) 
//								: 0.0, 
//						null));
//		map.put(
//				Commodity.REACTIVEPOWER, 
//				new RealElectricalCommodityState(
//						Commodity.REACTIVEPOWER, 
//						this.getPower(Commodity.REACTIVEPOWER) != null 
//								? (double) this.getPower(Commodity.REACTIVEPOWER)
//								: 0.0,
//						null));
//		map.put(
//				Commodity.HEATINGHOTWATERPOWER,
//				new RealThermalCommodityState(
//						Commodity.HEATINGHOTWATERPOWER, 
//						this.getPower(Commodity.HEATINGHOTWATERPOWER) != null 
//								? (double) this.getPower(Commodity.HEATINGHOTWATERPOWER)
//								: 0.0, 
//						this.currentTemperatureOut, 
//						this.currentMassFlow));
//		map.put(
//				Commodity.NATURALGASPOWER,
//				new RealThermalCommodityState(
//						Commodity.NATURALGASPOWER, 
//						this.getPower(Commodity.NATURALGASPOWER) != null 
//								? (double) this.getPower(Commodity.NATURALGASPOWER)
//								: 0.0, 
//						0.0, 
//						null));
		map.setPower(Commodity.ACTIVEPOWER, this.getPower(Commodity.ACTIVEPOWER) != null 
				? (double) this.getPower(Commodity.ACTIVEPOWER) : 0.0);
		
		map.setPower(Commodity.REACTIVEPOWER, this.getPower(Commodity.REACTIVEPOWER) != null 
				? (double) this.getPower(Commodity.REACTIVEPOWER) : 0.0);
		
		map.setAllThermal(Commodity.HEATINGHOTWATERPOWER, this.getPower(Commodity.HEATINGHOTWATERPOWER) != null 
				? (double) this.getPower(Commodity.HEATINGHOTWATERPOWER) : 0.0,
						new double[]{this.currentTemperatureOut, this.currentMassFlow});
		
		map.setPower(Commodity.NATURALGASPOWER, this.getPower(Commodity.NATURALGASPOWER) != null 
				? (double) this.getPower(Commodity.NATURALGASPOWER) : 0.0);
		return map;
	}

	
	@Override
	public void setCommodityInputStates(
			LimitedCommodityStateMap inputStates,
//			EnumMap<AncillaryCommodity,AncillaryCommodityState> ancillaryInputStates) {
			AncillaryMeterState ancillaryMeterState) throws EnergySimulationException {
		super.setCommodityInputStates(inputStates, ancillaryMeterState);
		// TODO temperature in (later...)
		if (inputStates != null) {
			if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
//				RealCommodityState cs = inputStates.get(Commodity.HEATINGHOTWATERPOWER);
//				RealThermalCommodityState tcs = (RealThermalCommodityState) cs;
				double temp = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
//				if (temp != null) {
					this.waterInTemperature = temp;
//				}
			}
		}
	}
	
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
	
}
