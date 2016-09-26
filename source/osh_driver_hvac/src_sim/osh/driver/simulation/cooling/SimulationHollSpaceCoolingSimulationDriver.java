package osh.driver.simulation.cooling;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.driver.model.BuildingThermalModel;
import osh.driver.model.FZIThermalModel;
import osh.driver.simulation.spacecooling.SimulationHollChillerCalendar;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.SpaceCoolingObserverExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class SimulationHollSpaceCoolingSimulationDriver 
					extends SpaceCoolingSimulationDriver {

	BuildingThermalModel model = new FZIThermalModel();
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SimulationHollSpaceCoolingSimulationDriver(
			IOSH osh,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
					throws SimulationSubjectException, HALException {
		super(osh, deviceID, driverConfig);
	}

	@Override
	public void onNextTimeTick() {
		
		if(getTimer().getUnixTime() % 86400 == 0) {
			//SIMULATE REAL CALENDER
			SimulationHollChillerCalendar calendar = new SimulationHollChillerCalendar(getRandomGenerator());
			dates = calendar.getDate(getTimer().getUnixTime());
		}
		
		if(!dates.isEmpty()) {
			ChillerCalendarDate date = dates.get(0);
			if (date.getStartTimestamp() <= getTimer().getUnixTime() 
					&& date.getStartTimestamp() + date.getlength() >= getTimer().getUnixTime()) {
				
//				getGlobalLogger().logDebug("start: " + date.startTimestamp);
//				getGlobalLogger().logDebug("length: " + date.length);
//				getGlobalLogger().logDebug("amountPersons: " + date.amountOfPerson);
				
				// calculate demand
				double currentOutdoorTemperature = 
						outdoorTemperature.getTemperature(getTimer().getUnixTime());
				coldWaterPowerDemand = model.calculateCoolingDemand(currentOutdoorTemperature);
				
//				if (demand < 0) {
//					getGlobalLogger().logDebug("Demand:" + demand + "outdoor: " + currentOutdoorTemperature);
//				}
			}
			else if(date.getStartTimestamp() + date.getlength() < getTimer().getUnixTime()) {
				dates.remove(0);
				coldWaterPowerDemand = 0;
			}
		}
		else {
//			getGlobalLogger().logDebug("There are no appointments today.");
		}
		
		this.setPower(Commodity.COLDWATERPOWER, (int) coldWaterPowerDemand);
		
		SpaceCoolingObserverExchange ox = 
				new SpaceCoolingObserverExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						dates, 
						outdoorTemperature.getMap(),
						(int) Math.round(coldWaterPowerDemand));
		this.notifyObserver(ox);
	}

}
