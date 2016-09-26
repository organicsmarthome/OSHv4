package osh.driver.simulation.thermal;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class VDI6002DomesticHotWaterStatistics {
	
	/** VDI Guideline 6002: Month */
	// VDI 6002 Part 1 Figure D1
	public static final double[] monthlyCorrection = {		
			1.119021, 
			1.135036,
			1.123025,
			1.020931,
			1.036946,
			0.944862,
			0.747682,
			0.897819,
			0.950868,
			0.903825,
			1.048957,
			1.082988
	};

	/** VDI Guideline 6002: Day of week */
	// calculated based on VDI 6002 Part 1 Figure D2
	public static final double[] dayOfWeekCorrection = {
			0.966,
			0.966,
			0.966,
			0.966,
			0.966,
			1.050,
			1.120
	};
	
	/**
	 * 
	 * @param month 0, ..., 11
	 * @return
	 */
	public static double getMonthlyCorrection(int month) {
		return monthlyCorrection[month];
	}
	
	
	/**
	 * 
	 * @param month 0 (= Monday), ..., 6 (= Sunday)
	 * @return
	 */
	public static double getDayOfWeekCorrection(int day) {
		return dayOfWeekCorrection[day];
	}
	
}
