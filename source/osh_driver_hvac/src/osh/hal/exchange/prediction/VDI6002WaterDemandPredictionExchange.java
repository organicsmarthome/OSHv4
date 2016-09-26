package osh.hal.exchange.prediction;

import java.util.UUID;

import osh.eal.hal.exchange.HALObserverExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class VDI6002WaterDemandPredictionExchange 
				extends HALObserverExchange {
	
	private double[] correctionFactorMonth;
	private double[] correctionFactorWeekday;
	private double[][] weekDayHourProbabilities;
	private double avgYearlyDemand;
	
	
	/**
	 * @param deviceID
	 * @param timestamp
	 * @param correctionFactorMonth
	 * @param correctionFactorWeekday
	 * @param weekDayHourProbabilities
	 * @param avgYearlyDemand
	 */
	public VDI6002WaterDemandPredictionExchange(UUID deviceID, Long timestamp, double[] correctionFactorMonth,
			double[] correctionFactorWeekday, double[][] weekDayHourProbabilities, double avgYearlyDemand) {
		super(deviceID, timestamp);
		this.correctionFactorMonth = correctionFactorMonth;
		this.correctionFactorWeekday = correctionFactorWeekday;
		this.weekDayHourProbabilities = weekDayHourProbabilities;
		this.avgYearlyDemand = avgYearlyDemand;
	}
	
	public double[] getCorrectionFactorMonth() {
		return correctionFactorMonth;
	}
	public void setCorrectionFactorMonth(double[] correctionFactorMonth) {
		this.correctionFactorMonth = correctionFactorMonth;
	}
	public double[] getCorrectionFactorWeekday() {
		return correctionFactorWeekday;
	}
	public void setCorrectionFactorWeekday(double[] correctionFactorWeekday) {
		this.correctionFactorWeekday = correctionFactorWeekday;
	}
	public double[][] getWeekDayHourProbabilities() {
		return weekDayHourProbabilities;
	}
	public void setWeekDayHourProbabilities(double[][] weekDayHourProbabilities) {
		this.weekDayHourProbabilities = weekDayHourProbabilities;
	}
	public double getAvgYearlyDemand() {
		return avgYearlyDemand;
	}
	public void setAvgYearlyDemand(double avgYearlyDemand) {
		this.avgYearlyDemand = avgYearlyDemand;
	}

}
