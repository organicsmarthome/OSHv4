package osh.driver.simulation.cooling;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.driver.simulation.spacecooling.July2013HollChillerCalendar;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.SpaceCoolingObserverExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class July2013HollSpaceCoolingSimulationDriver 
					extends SpaceCoolingSimulationDriver {

	
	/**
	 * CONSTRUCTOR
	 */
	public July2013HollSpaceCoolingSimulationDriver(
			IOSH osh,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
					throws SimulationSubjectException, HALException {
		super(osh, deviceID, driverConfig);
	}

	
	@Override
	public void onSystemIsUp() {
		super.onSystemIsUp();
		
		July2013HollChillerCalendar calendar = new July2013HollChillerCalendar();
		dates = calendar.getDate();
	}

	@Override
	public void onNextTimeTick() {
		if(!dates.isEmpty()) {
			ChillerCalendarDate date = dates.get(0);
			if (date.getStartTimestamp() <= getTimer().getUnixTime() 
					&& date.getStartTimestamp() + date.getlength() >= getTimer().getUnixTime()) {
				
//				getGlobalLogger().logDebug("start: " + date.startTimestamp);
//				getGlobalLogger().logDebug("length: " + date.length);
//				getGlobalLogger().logDebug("amountPersons: " + date.amountOfPerson);
				
				// get real demand from file
				coldWaterPowerDemand = date.getKnownPower();
				
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
