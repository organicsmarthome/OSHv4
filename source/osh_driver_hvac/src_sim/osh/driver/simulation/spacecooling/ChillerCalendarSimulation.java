package osh.driver.simulation.spacecooling;

import java.util.ArrayList;

import osh.driver.datatypes.cooling.ChillerCalendarDate;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class ChillerCalendarSimulation {
	
	public abstract ArrayList<ChillerCalendarDate> getDate(long timestamp);

}
