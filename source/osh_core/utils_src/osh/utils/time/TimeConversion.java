package osh.utils.time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Requires Java 8
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 *
 */
public class TimeConversion {

	//Theoretically this should be adjusted for where the system is running
	private final static ZoneId zone = ZoneId.of("UTC");


	public static int convertUnixTime2Year(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		return zdt.getYear();
	}

	/**
	 * 
	 * @param unixTime
	 * @return 1. Jan = 1
	 */
	public static int convertUnixTime2DayOfYear(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		return zdt.getDayOfYear();
	}
	
	/**
	 * 
	 * @param unixTime
	 * @return 1. Jan = 0
	 */
	public static int convertUnixTime2CorrectedDayOfYear(long unixTime) {
		return convertUnixTime2DayOfYear(unixTime) - 1;
	}
	
	/**
	 * 
	 * @param unixTime
	 * @return 1. day of month = 1
	 */
	public static int convertUnixTime2DayOfMonth(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		return zdt.getDayOfMonth();
	}

	/**
	 * 
	 * @param unixTime
	 * @return mon=0, tue=1...
	 */
	public static int convertUnixTime2CorrectedWeekdayInt(long unixTime) {	
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		return zdt.getDayOfWeek().getValue() - 1; //DoW indexes from Mon=1 to Sun=7 so we need to adjust
	}

	/**
	 * 
	 * @param unixTime
	 * @return 01.01. 00:01 = 60
	 */
	public static int convertUnixTime2SecondsFromYearStart(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		ZonedDateTime yearStart = 
				zdt.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);
		return (int) (unixTime - yearStart.toEpochSecond());
	}

	/**
	 * 
	 * @param unixTime
	 * @return jan=1 (as ENUM -> getValue()), ...
	 */
	public static Month convertUnixTime2Month(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		return zdt.getMonth();
	}

	/**
	 * 
	 * @param unixTime
	 * @return jan=0, ... , dec=11
	 */
	public static int convertUnixTime2MonthInt(long unixTime) {
		//Month enum indexes from Jan=1 to Dec=12
		return convertUnixTime2Month(unixTime).getValue() - 1;
	}

	/**
	 * 
	 * @param unixTime
	 * @return seconds since midnight (1am=3600)
	 */
	public static int convertUnixTime2SecondsSinceMidnight(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		ZonedDateTime midnight = zdt.withHour(0).withMinute(0).withSecond(0).withNano(0);
		return (int) (unixTime - midnight.toEpochSecond());
	}


	/**
	 * 
	 * @param currentUnixTime
	 * @return UnixTime this day 00:00
	 */
	public static long getUnixTimeStampCurrentDayMidnight(long currentUnixTime) {
		Instant time = Instant.ofEpochSecond(currentUnixTime);
		ZonedDateTime zdt = time.atZone(zone);
		ZonedDateTime midnight = zdt.withHour(0).withMinute(0).withSecond(0).withNano(0);
		return midnight.toEpochSecond();
	}

	/**
	 * 1-based (i.e., month = 1, ..., and so on
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @return
	 */
	public static long componentTimeToTimestamp(int year, int month, int day, int hour) {
		ZonedDateTime zdt = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zone);
		return zdt.toEpochSecond();
	}

	public static long addPeriodToUnixTime(long unixTime, Period period) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		zdt = zdt.plus(period);
		return zdt.toEpochSecond();
	}
	
	/**
	 * 
	 * @param unixTime
	 * @param days Day X
	 * @return StartOfXthDayAfterToday
	 */
	public static long getStartOfXthDayAfterToday(long unixTime, long days) {
		long midnight = getUnixTimeStampCurrentDayMidnight(unixTime);
		Instant time = Instant.ofEpochSecond(midnight);
		ZonedDateTime zdt = time.atZone(zone);
		zdt = zdt.plusDays(days);
		return zdt.toEpochSecond();
	}
	
	
	public static long getStartOfXthWeek(long unixTime, long weeks) {
		long midnight = getUnixTimeStampCurrentDayMidnight(unixTime);
		Instant time = Instant.ofEpochSecond(midnight);
		ZonedDateTime zdt = time.atZone(zone);
		zdt = zdt.plusWeeks(weeks);
		DayOfWeek dow = zdt.getDayOfWeek();
		int daysSinceMonday = dow.getValue() - 1;
		zdt = zdt.minusDays(daysSinceMonday);
		return zdt.toEpochSecond();
	}
	
	public static long getStartOfXthMonth(long unixTime, long months) {
		long midnight = getUnixTimeStampCurrentDayMidnight(unixTime);
		Instant time = Instant.ofEpochSecond(midnight);
		ZonedDateTime zdt = time.atZone(zone);
		zdt = zdt.plusMonths(months);
		zdt = zdt.withDayOfMonth(1);
		return zdt.toEpochSecond();
	}
	
	/** gets the index of the first weekday of the year
	 * 
	 * @param year
	 * @return mon = 0, ...
	 */
	public static int getCorrectedFirstWeekDayOfYear(int year) {
		ZonedDateTime zdt = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zone);
		return zdt.getDayOfWeek().getValue() - 1;
	}
	
	/** get month of dayOfYear and year
	 * 
	 * @param dayOfYear 1. jan = 0, ...
	 * @param year
	 * @return jan = 1, ... dec = 12
	 */
	public static int getMonthFromDayOfYearAndYear(int dayOfYear, int year) {
		ZonedDateTime zdt = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zone);
		zdt = zdt.plusDays(dayOfYear);
		
		return zdt.getMonthValue();
	}
	
	/** get day of month of dayOfYear and year
	 * 
	 * @param dayOfYear 1. jan = 0, ...
	 * @param year
	 * @return 1. day of month = 1
	 */
	public static int getDayOfMonthFromDayOfYearAndYear(int dayOfYear, int year) {
		ZonedDateTime zdt = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zone);
		zdt = zdt.plusDays(dayOfYear);
		
		return zdt.getDayOfMonth();
	}
	
	/** gets minuteOfDay
	 * 
	 * @param unixTime
	 * @return minuteOfDay, 00:01 = 1
	 */
	public static int convertUnixTime2MinuteOfDay(long unixTime) {
		Instant time = Instant.ofEpochSecond(unixTime);
		ZonedDateTime zdt = time.atZone(zone);
		
		return ((zdt.getHour() * 60 + zdt.getMinute()) % 1440);
	}
	
	public static int getNumberOfDaysInYearFromTimeStamp(long timeStamp) {
		int year = convertUnixTime2Year(timeStamp);
		
		if ( ( (year % 4 == 0) && (year % 100 != 0) ) || (year % 400 == 0) ) {
			// leap year
			return 366;
		}
		else {
			// normal year
			return 365;
		}
	}

}
