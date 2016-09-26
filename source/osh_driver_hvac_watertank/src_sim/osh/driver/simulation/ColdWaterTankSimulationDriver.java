package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.thermal.SimpleColdWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.ColdWaterTankObserverExchange;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ColdWaterTankSimulationDriver extends WaterTankSimulationDriver {

//	private SimpleColdWaterTank waterTank;
	
//	private RealThermalCommodityState thermalInputState = null;	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public ColdWaterTankSimulationDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// tank capacity in liters
		double tankCapacity;
		try {
			tankCapacity = Double.parseDouble(driverConfig.getParameter("tankCapacity"));
		} catch (Exception e) {
			tankCapacity = 3000.0;
			getGlobalLogger().logWarning("Can't get tankCapacity, using the default value: " + tankCapacity);
		}

		double tankDiameter;
		try {
			tankDiameter = Double.parseDouble(driverConfig.getParameter("tankDiameter"));
		} catch (Exception e) {
			tankDiameter = 1.0;
			getGlobalLogger().logWarning("Can't get tankDiameter, using the default value: " + tankDiameter);
		}

		double initialTemperature;
		try {
			initialTemperature = Double.parseDouble(driverConfig.getParameter("initialTemperature"));
		} catch (Exception e) {
			initialTemperature = 14.0;
			getGlobalLogger().logWarning("Can't get initialTemperature, using the default value: " + initialTemperature);
		}

		double ambientTemperature;
		try {
			ambientTemperature = Double.parseDouble(driverConfig.getParameter("ambientTemperature"));
		} catch (Exception e) {
			ambientTemperature = 20.0;
			getGlobalLogger().logWarning("Can't get ambientTemperature, using the default value: " + ambientTemperature);
		}
		
		this.waterTank = new SimpleColdWaterTank(
				tankCapacity, 
				tankDiameter, 
				initialTemperature, 
				ambientTemperature);
	}

	@Override
	public void onNextTimeTick() {
		
		// reduce be standing loss
		waterTank.reduceByStandingHeatLoss(1);
		double demand = 0, supply = 0;
		
		// add/remove energy from tank...
//		if (thermalInputState != null) {
		if (commodityInputStates.containsCommodity(Commodity.COLDWATERPOWER)) {
			double power = commodityInputStates.getPower(Commodity.COLDWATERPOWER);
			double addThermal[] = commodityInputStates.getAdditionalThermal(Commodity.COLDWATERPOWER);
			if ( power != 0 ) {
//				// (-1) because of energy calculation tank is based on heat not coolness...
//				waterTank.addPowerOverTime( (-1) * power, 1, reflowTemperature, massFlow);
//				waterTank.addPowerOverTime( power, 1, reflowTemperature, massFlow);
				waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
			}
//			thermalInputState = null;
			if (power < 0) {
				demand += power;
			} else if (power > 0) {
				supply += power;
			}
		} 
		
		// communicate to visualization / GUI
		// -> is done via Observer -> OCRegistry -> GuiComMgr -> GuiComDriver
	
		// communicate state to observer
		ColdWaterTankObserverExchange observerExchange = 
				new ColdWaterTankObserverExchange(
						getDeviceID(), 
						getTimer().getUnixTime(),
						waterTank.getCurrentWaterTemperature(),
						waterTank.getTankCapacity(),
						demand,
						supply);
		this.notifyObserver(observerExchange);
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
