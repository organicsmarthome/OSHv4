package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class HotWaterTankSimulationDriver extends WaterTankSimulationDriver {

//	private SimpleHotWaterTank waterTank;
	
	private long newIppAfter;
	private double triggerIppIfDeltaTempBigger;
	
	private boolean log = false;
	private double temperatureLogging = 0;
	private double demandLogging = 0;
	private double supplyLogging = 0;
	private double temperatureLoggingCounter = 0;
	
	/**
	 * CONSTRUCTOR
	 */
	public HotWaterTankSimulationDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
					throws SimulationSubjectException, HALException {
		super(
				controllerbox, 
				deviceID, 
				driverConfig);
		
		// tank capacity in liters
		double tankCapacity;
		try {
			tankCapacity = Double.parseDouble(driverConfig.getParameter("tankCapacity"));
		} catch (Exception e) {
			tankCapacity = 750;
			getGlobalLogger().logWarning("Can't get tankCapacity, using the default value: " + tankCapacity);
		}
		
		double tankDiameter;
		try {
			tankDiameter = Double.parseDouble(driverConfig.getParameter("tankDiameter"));
		} catch (Exception e) {
			tankDiameter = 0.5;
			getGlobalLogger().logWarning("Can't get tankDiameter, using the default value: " + tankDiameter);
		}
		
		double initialTemperature;
		try {
			initialTemperature = Double.parseDouble(driverConfig.getParameter("initialTemperature"));
		} catch (Exception e) {
			initialTemperature = 70.0;
			getGlobalLogger().logWarning("Can't get initialTemperature, using the default value: " + initialTemperature);
		}
		
		double ambientTemperature;
		try {
			ambientTemperature = Double.parseDouble(driverConfig.getParameter("ambientTemperature"));
		} catch (Exception e) {
			ambientTemperature = 20.0;
			getGlobalLogger().logWarning("Can't get ambientTemperature, using the default value: " + ambientTemperature);
		}
		
		try {
			this.newIppAfter = Long.valueOf(getDriverConfig().getParameter("newIppAfter"));
		}
		catch (Exception e) {
			this.newIppAfter = 1 * 3600; // 1 hour
			getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
		}
		
		try {
			this.triggerIppIfDeltaTempBigger = Double.valueOf(getDriverConfig().getParameter("triggerIppIfDeltaTempBigger"));
		}
		catch (Exception e) {
			this.triggerIppIfDeltaTempBigger = 0.5;
			getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
		}
		
		this.waterTank = new SimpleHotWaterTank(
				tankCapacity, 
				tankDiameter, 
				initialTemperature, 
				ambientTemperature);
	}
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		super.onSimulationIsUp();
		
		IPPSchedulingExchange _ise = new IPPSchedulingExchange(getDeviceID(), getTimer().getUnixTime());
		_ise.setNewIppAfter(newIppAfter);
		_ise.setTriggerIfDeltaX(triggerIppIfDeltaTempBigger);
		this.notifyObserver(_ise);
		
		log = DatabaseLoggerThread.isLogWaterTank();
	}
	
	@Override 
	public void onSystemShutdown() {
		if (log) {
			temperatureLogging /= temperatureLoggingCounter;
			demandLogging /= 3600000.0;
			supplyLogging /= 3600000.0;
			
			DatabaseLoggerThread.enqueueWaterTank(temperatureLogging, demandLogging, supplyLogging);
		}
	};


	@Override
	public void onNextTimeTick() {
		
		// reduce be standing loss
		waterTank.reduceByStandingHeatLoss(1);
		double waterDemand = 0;
		double waterSupply = 0;
		
		
		if (commodityInputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
			double power = (-1) * commodityInputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
			double addThermal[] = commodityInputStates.getAdditionalThermal(Commodity.HEATINGHOTWATERPOWER);
			if ( power != 0 ) {
				waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
			}
			if (power < 0) {
				waterDemand += power;
			} else if (power > 0) {
				waterSupply += power;
			}
		} 
		
		if (commodityInputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
			double power = (-1) * commodityInputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);
			double addThermal[] = commodityInputStates.getAdditionalThermal(Commodity.DOMESTICHOTWATERPOWER);
			if ( power != 0 ) {
				waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
			}
			if (power < 0) {
				waterDemand += power;
			} else if (power > 0) {
				waterSupply += power;
			}
		} 
		
		if (log) {
			temperatureLogging += waterTank.getCurrentWaterTemperature();
			demandLogging += waterDemand;
			supplyLogging += waterSupply;
			temperatureLoggingCounter ++;
		}
		
		// communicate to visualization / GUI
		// -> is done via Observer -> OCRegistry -> GuiComMgr -> GuiComDriver
		
		// communicate state to observer
		HotWaterTankObserverExchange observerExchange = 
				new HotWaterTankObserverExchange(
						getDeviceID(), 
						getTimer().getUnixTime(),
						waterTank.getCurrentWaterTemperature(),
						waterTank.getTankCapacity(),
						waterTank.getTankDiameter(),
						waterTank.getAmbientTemperature(),
						waterDemand,
						waterSupply);
		this.notifyObserver(observerExchange);
	}


	@Override
	public LimitedCommodityStateMap getCommodityOutputStates() {
		LimitedCommodityStateMap map = new LimitedCommodityStateMap(new Commodity[] {Commodity.HEATINGHOTWATERPOWER});
		map.setTemperature(Commodity.HEATINGHOTWATERPOWER, waterTank.getCurrentWaterTemperature());
		return map;
	}

	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
