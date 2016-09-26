package osh.utils.slp;

/** interface for methods which a H0-Profile should provide
 * 
 * @author Sebastian Kramer
 *
 */
public interface IH0Profile {

	/** gets the baseload power for the given unix-timestamp
	 * 
	 * @param timeStamp unix-timestamp
	 * @return baseload power for the given unix-timestamp
	 */
	public int getActivePowerAt(long timeStamp);
	
	/** gets the percentage the power of the given time is between the daily maximum and minimum
	 * 
	 * @param timestamp unix-timestamp
	 * @return the percentage the power of the given time is between the daily maximum and minimum
	 */
	public double getPercentOfDailyMaxWithoutDailyMin(long timestamp);	
	
	/** gets the average percentage the power during the whole day of the given timestamp is between the daily maximum and minimum
	 * 
	 * @param timestamp unix-timestamp
	 * @return the average percentage the power during the whole day of the given timestamp is between the daily maximum and minimum
	 */
	public double getAvgPercentOfDailyMaxWithoutDailyMin(long timestamp);
	
	/** gets the probability correction factor to which one has to correct a distribution to follow the H0-Profile
	 * 
	 * @param timestamp unix-timestamp
	 * @return the probability correction factor to which one has to correct a distribution to follow the H0-Profile
	 */
	public double getCorrectionFactorForTimestamp(long timestamp);
	
	/** gets the correction factors as an array for every day in the year
	 * 
	 * February will always be given assumed as having 29 days, in non-leap years the 29th value can just be ignored
	 * 
	 * @return the correction factors as an array for every day in the year
	 */
	public double[] getCorrectionFactorForDay(); 	
	
	/**
	 * EN: Dynamization function of German for H0, mean = 1.004464866
	 * @param dayOfYear 1. Jan = 0
	 * @return
	 */
	public static double getBdewDynamizationValue(int dayOfYear) {
		dayOfYear++;
		return -0.000000000392 * Math.pow(dayOfYear,4) 
				+ 0.00000032 * Math.pow(dayOfYear,3) 
				+ -0.0000702 * Math.pow(dayOfYear,2) 
				+ 0.0021 * Math.pow(dayOfYear,1) + 1.24;
	}
	
	/**
	 * EN: Corrected ynamization function of German for H0, mean = 1.0
	 * @param dayOfYear 1. Jan = 0
	 * @return
	 */
	public static double getCorrectedBdewDynamizationValue(int dayOfYear) {
		return getBdewDynamizationValue(dayOfYear) / 1.004464866; // correction because integral of dynamization function is not 1
	}
}
