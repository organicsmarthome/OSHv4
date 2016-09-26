package osh.driver.simulation.spacecooling;

import java.util.ArrayList;

import osh.driver.datatypes.cooling.ChillerCalendarDate;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class July2013HollChillerCalendar {

	
	/**
	 * CONSTRUCTOR
	 */
	public July2013HollChillerCalendar() {
		//NOTHING
	}
	
	//HELPER VARIABLES
	private ArrayList<ChillerCalendarDate> dates = new ArrayList<ChillerCalendarDate>();
	
	public ArrayList<ChillerCalendarDate> getDate() {
		
		
		{
			long time = 1373024187 - 1356998400; // 16025787
			int length = 26520;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1880);
			dates.add(date);
		}
		
		{
			long time = 1373101887 - 1356998400;
			int length = 19380;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1770);
			dates.add(date);
		}
		
		{
			long time = 1373290386 - 1356998400;
			int length = 30240;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1460);
			dates.add(date);
		}
		
		{
			long time = 1373353207 - 1356998400;
			int length = 18060;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1290);
			dates.add(date);
		}
		
		{
			long time = 1373555009 - 1356998400;
			int length = 14760;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1590);
			dates.add(date);
		}
		
		{
			long time = 1373626910 - 1356998400;
			int length = 21060;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 2370);
			dates.add(date);
		}
		
		{
			long time = 1373964277 - 1356998400;
			int length = 26100;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1620);
			dates.add(date);
		}
		
		{
			long time = 1374056738 - 1356998400;
			int length = 16740;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 1420);
			dates.add(date);
		}
		
		{
			long time = 1374474760 - 1356998400;
			int length = 16020;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 3630);
			dates.add(date);
		}
		
		{
			long time = 1374645688 - 1356998400;
			int length = 31140;
			int persons = 1;
			ChillerCalendarDate date = 
					new ChillerCalendarDate(time, length, persons, 22.0, 2720);
			dates.add(date);
		}
		
		return dates;
	}
}
