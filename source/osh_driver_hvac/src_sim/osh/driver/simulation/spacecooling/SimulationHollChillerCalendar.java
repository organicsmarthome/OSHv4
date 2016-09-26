package osh.driver.simulation.spacecooling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import osh.core.OSHRandomGenerator;
import osh.driver.datatypes.cooling.ChillerCalendarDate;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class SimulationHollChillerCalendar 
					extends ChillerCalendarSimulation {
	
	private OSHRandomGenerator random;

	//SETTINGS
	static int minDatesPerDay = 1;
	static int maxDatesPerDay = 2;
	
	static int minPersonsPerDate = 2;
	static int maxPersonsPerDate = 5;
	
	static int minLengthOfDate = 2;
	static int maxLenghtOfDate = 4;
	
	static int minPause = 2;
	static int maxPause = 3;
	
	private long minTimeToAddNextDate = 0;
	
	//HELPER VARIABLES
	private ArrayList<ChillerCalendarDate> dates = new ArrayList<ChillerCalendarDate>();
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SimulationHollChillerCalendar(OSHRandomGenerator random) {
		super();
		this.random = new OSHRandomGenerator(new Random(random.getNextLong()));
	}

	
	public ArrayList<ChillerCalendarDate> getDate(long timestamp) {
	
		//TERMINE FÜR DEN TAG GENERIEREN
		long initialNumber = random.getNextLong();
		OSHRandomGenerator newRandomGen = new OSHRandomGenerator(new Random(initialNumber));
		
		int datesPerDay = newRandomGen.getNextInt(maxDatesPerDay - minDatesPerDay + 1) + minDatesPerDay;
		
		System.out.println("Termine für diesen Tag: " + datesPerDay);
		
		minTimeToAddNextDate = (timestamp + 3600 * 8) + (3600 * (newRandomGen.getNextInt(maxPause - 0 + 1) + 0));
		
		for(int i = 0; i < datesPerDay; i++) {
			
			long length =  3600 * (newRandomGen.getNextInt(maxLenghtOfDate - minLengthOfDate + 1) + minLengthOfDate);
			int personsPerDate = newRandomGen.getNextInt(maxPersonsPerDate - minPersonsPerDate + 1) + minPersonsPerDate;
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			calendar.setTimeInMillis(minTimeToAddNextDate * 1000);

			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			
			System.out.println("Termin um: " + hour + " Uhr | " + length/3600 + " Stunden");
			
			ChillerCalendarDate date = new ChillerCalendarDate(minTimeToAddNextDate, length, personsPerDate, 22.0, Integer.MAX_VALUE);
			
			int pause = 3600 * (newRandomGen.getNextInt(maxPause - minPause + 1) + minPause);
			
			minTimeToAddNextDate += length + pause; 
			
			dates.add(date);
		}
		
		return dates;
	}
	
}
