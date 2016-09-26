package osh.utils.slp;

import java.util.Arrays;

import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class H0Profile15Minutes implements IH0Profile {
	
	private String h0ProfileFileName; 
	
	private int year = 0;
	private double yearlyKWh = 0; // in kWh/a
	
	private int numberOfDaysInThisYear;
	private int[] daysPerMonth;
	
	
	private double[] seasonCorrectionFactor = {0.955824945, 1.039432691, 1.003277321};
	private double[][] seasonWeekdayCorrectionFactor = {
			{0.974925926, 0.974925926, 0.974925926, 0.974925926, 0.974925926, 1.101019664, 1.024350705},
			{0.987019511, 0.987019511, 0.987019511, 0.987019511, 0.987019511, 1.063843913, 1.001058532},
			{0.979653683, 0.979653683, 0.979653683, 0.979653683, 0.979653683, 1.095177467, 1.006554118}};
	
	// Season 1
	
	/** [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W */
	private double[][][] h0ProfileArray = new double[3][7][96]; //W
	
	/** [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W, scaled to yearly kWh */
	private double[][][] h0ProfileArrayScaled = new double[3][7][96]; //W
	
	/** [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W, scaled to yearly kWh, reduced by minimum value */
	private double[][][] h0ProfileArrayWithoutMin = new double[3][7][96]; //W
	
	private double[][] seasonWeekdayMinValue = new double[3][7];
	private double[][] seasonWeekdayMaxValue = new double[3][7];
	private double[][] seasonWeekdayAvgValue = new double[3][7];
	
	private double[] correctionFactorForDay;
	

	/**
	 * CONSTRUCTOR
	 * @param h0ProfileFileName
	 * @param yearlyKWhTotal
	 * @param consumptionShare
	 */
	public H0Profile15Minutes(
			int year, 
			String h0ProfileFileName, 
			double yearlyKWh) {
		
		this.h0ProfileFileName = h0ProfileFileName;
		this.year = year;
		this.yearlyKWh = yearlyKWh;
		
		// calculate number of days
		if ( ( (year % 4 == 0) && (year % 100 != 0) ) || (year % 400 == 0) ) {
			// leap year
			numberOfDaysInThisYear = 366;
			int[] daysPerMonth = {31,29,31,30,31,30,31,31,30,31,30,31};
			this.daysPerMonth = daysPerMonth;
		}
		else {
			// normal year
			numberOfDaysInThisYear = 365;
			int[] daysPerMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
			this.daysPerMonth = daysPerMonth;
		}
		
		correctionFactorForDay = new double[numberOfDaysInThisYear];
		
		calculateCorrectionFactorDay();
		
		double[][] h0ProfileFile = CSVImporter.readDouble2DimArrayFromFile(h0ProfileFileName, ";");
		
		// d0 = season, d1 = weekday, d2 = quarter hour
		for (int d0 = 0; d0 < h0ProfileArray.length; d0++) {
			for (int d1 = 0; d1 < h0ProfileArray[d0].length; d1++) {
				for (int d2 = 0; d2 < h0ProfileArray[d0][d1].length; d2++) {
					h0ProfileArray[d0][d1][d2] = h0ProfileFile[d2][d0 * 7 + d1];
					
					//scaling, original profile is for 1000 kWh/a
					h0ProfileArrayScaled[d0][d1][d2] = h0ProfileArray[d0][d1][d2] * (yearlyKWh / 1000.0);
				}
			}
		}
		
		// calculate seasonal weekday min values and array of reduced h0Profile
		for (int d0 = 0; d0 < h0ProfileArray.length; d0++) {
			for (int d1 = 0; d1 < h0ProfileArray[d0].length; d1++) {
				double tempMin = Double.MAX_VALUE;
				double tempMax = Double.MIN_VALUE;
				double tempSum = 0;
				for (int d2 = 0; d2 < h0ProfileArray[d0][d1].length; d2++) {
					tempMin = Math.min(tempMin, h0ProfileArray[d0][d1][d2]);
					tempMax = Math.max(tempMax, h0ProfileArray[d0][d1][d2]);
					tempSum = tempSum + h0ProfileArray[d0][d1][d2];
				}
				seasonWeekdayMinValue[d0][d1] = tempMin;
				seasonWeekdayMaxValue[d0][d1] = tempMax;
				seasonWeekdayAvgValue[d0][d1] = tempSum / h0ProfileArray[d0][d1].length;
				for (int d2 = 0; d2 < h0ProfileArray[d0][d1].length; d2++) {
					h0ProfileArrayWithoutMin[d0][d1][d2] = h0ProfileArray[d0][d1][d2] - tempMin;
				}
			}
		}
		// calculate 
	}
	
	private void calculateCorrectionFactorDay() {
		
		int weekDay = TimeConversion.getCorrectedFirstWeekDayOfYear(year);
		double aggregate = 0;
		
		for (int d0 = 0; d0 < numberOfDaysInThisYear; d0++) {
			int month = TimeConversion.getMonthFromDayOfYearAndYear(d0, year);
			int dayOfMonth = TimeConversion.getDayOfMonthFromDayOfYearAndYear(d0, year);
			int season = getSeasonIndexFromDayMonth(dayOfMonth, month);
			
			correctionFactorForDay[d0] = IH0Profile.getBdewDynamizationValue(d0) * seasonCorrectionFactor[season] * seasonWeekdayCorrectionFactor[season][weekDay];
			
			aggregate += correctionFactorForDay[d0];
			weekDay++;
			weekDay %= 7;
		}
		
		aggregate /= numberOfDaysInThisYear;
		
		if (Math.abs(aggregate - 1.0) > 0.00000001) {			
			final double mult = aggregate;			
			correctionFactorForDay = Arrays.stream(correctionFactorForDay).map(d -> d / mult).toArray();
		}
	}
	
	public double getCorrectionFactorForTimestamp(long timestamp) {
		int month = TimeConversion.convertUnixTime2Month(timestamp).getValue();
		int weekday = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timestamp);
		int dayOfMonth = TimeConversion.convertUnixTime2DayOfMonth(timestamp);
		int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(timestamp);
		
		int season = getSeasonIndexFromDayMonth(dayOfMonth, month);
		
		
		
		return IH0Profile.getBdewDynamizationValue(dayOfYear) * seasonCorrectionFactor[season] * seasonWeekdayCorrectionFactor[season][weekday];
	}

	/** gets the season index from dayOfMonth and month
	 * 
	 * @param day 1. of month = 1
	 * @param month jan = 1, ...
	 * @return the season index
	 */
	private int getSeasonIndexFromDayMonth(int day, int month) {
		
		/* boundarys:
		 * 
		 * Winter: 01.11. - 20.03.
		 * Summer: 15.05. - 14.09.
		 * Intermediate: 21.03. - 14.05. && 15.09. - 31.10
		 */
		
		//def. Winter
		if (month < 3 || month > 10)
			return 0;
		
		//def. summer
		if (month > 5 && month < 9)
			return 1;
		
		//def. intermediate
		if (month == 4 || month == 10)
			return 2;
		
		//winter or intermediate
		if (month == 3) {
			if (day < 21)
				return 0;
			else
				return 2;
		}
		
		//intermediate or summer
		if (month == 5) {
			if (day < 15)
				return 2;
			else
				return 1;
		}
		
		//summer or intermediate
		if (month == 9) {
			if (day < 15)
				return 1;
			else 
				return 2;
		}
		
		//Illegal month/day
		return -1;
	}
	
	
	public double[][][] getH0ProfileArray() {
		return h0ProfileArray;
	}
	
	public double[][][] getH0ProfileArrayScaled() {
		return h0ProfileArrayScaled;
	}
	

	public double getAvgPercentOfDailyMaxWithoutDailyMin(long timestamp) {
		int month = TimeConversion.convertUnixTime2Month(timestamp).getValue();
		int day = TimeConversion.convertUnixTime2DayOfMonth(timestamp);
		int weekday = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timestamp);
		int season = getSeasonIndexFromDayMonth(day, month);
		
		double max = 0;
		for (int i = 0; i < 96; i++) {
			max = Math.max(max, h0ProfileArrayWithoutMin[season][weekday][i]);
		}
		
		double avgPercent = 0;
		for (int i = 0; i < 96; i++) {
			avgPercent += h0ProfileArrayWithoutMin[season][weekday][i] / max / 96;
		}
		
		return avgPercent;
	}

	public double getPercentOfDailyMaxWithoutDailyMin(long timestamp) {
		int month = TimeConversion.convertUnixTime2Month(timestamp).getValue();
		int day = TimeConversion.convertUnixTime2DayOfMonth(timestamp);
		int weekday = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timestamp);
		int season = getSeasonIndexFromDayMonth(day, month);
		int time = TimeConversion.convertUnixTime2SecondsSinceMidnight(timestamp);
		int quarterhour = ((time / (15 * 60)) % 96);
		
		double max = 0;
		for (int i = 0; i < 96; i++) {
			max = Math.max(max, h0ProfileArrayWithoutMin[season][weekday][i]);
		}
		return h0ProfileArrayWithoutMin[season][weekday][quarterhour] / max;
	}
	
	
	public int getActivePowerAt(long timeStamp) {
		int month = TimeConversion.convertUnixTime2Month(timeStamp).getValue();
		int weekday = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timeStamp);
		int dayOfMonth = TimeConversion.convertUnixTime2DayOfMonth(timeStamp);
		int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(timeStamp);
		int quarterhour = ((TimeConversion.convertUnixTime2MinuteOfDay(timeStamp) / 15) % 96);
		
		int season = getSeasonIndexFromDayMonth(dayOfMonth, month);
		
//		double factor1 = IH0Profile.getCorrectedBdewDynamizationValue(dayOfYear);
		double factor3 = IH0Profile.getBdewDynamizationValue(dayOfYear);
		double factor2 = 1.002891587; //H0-Profile and dynamization from csv does not exactly add up to 1000 kWh/a
//		int value = (int) Math.round((h0ProfileArrayScaled[season][weekday][quarterhour] *  factor1 * factor2));
		int value = (int) Math.round((h0ProfileArrayScaled[season][weekday][quarterhour] * factor2 * factor3));

		return value;		
	}


	@Override
	public double[] getCorrectionFactorForDay() {
		return correctionFactorForDay;
	}	
}
