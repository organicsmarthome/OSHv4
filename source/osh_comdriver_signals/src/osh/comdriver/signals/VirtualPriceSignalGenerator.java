package osh.comdriver.signals;

import org.apache.commons.math3.distribution.BinomialDistribution;

import osh.core.OSHRandomGenerator;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.utils.slp.IH0Profile;



/**
 * 
 * @author Ingo Mauser
 *
 */
public class VirtualPriceSignalGenerator {
	
	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param constantPeriod
	 * @param minPrice
	 * @param avgPrice
	 * @param maxPrice
	 * @param profile
	 * @param randomGenerator
	 * @param maxDeviationMinPriceInPercent
	 * @param maxDeviationMaxPriceInPercent
	 * @return
	 */
	public static PriceSignal getRandomH0BasedPriceSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			double minPrice,
			double avgPrice,
			double maxPrice,
			IH0Profile profile,
			OSHRandomGenerator randomGenerator,
			boolean deviatePrice,
			double maxDeviationMinPriceInPercent,
			double maxDeviationMaxPriceInPercent,
			AncillaryCommodity commodity) {
		
		// set deviation to zero if random deviation is off
		if (!deviatePrice) {
			maxDeviationMinPriceInPercent = 0;
			maxDeviationMaxPriceInPercent = 0;
		}
		
		PriceSignal complexPriceSignal = new PriceSignal(commodity);
		
		double newComplexPowerPriceMin = minPrice + maxDeviationMinPriceInPercent * minPrice;
		double newComplexPowerPriceMax = maxPrice - maxDeviationMaxPriceInPercent * maxPrice;
		
		// transformation to cents * 10;
		int activeMin = (int) Math.round(10 * newComplexPowerPriceMin);
		int activeAvg = (int) Math.round(10 * avgPrice);
		int activeMax = (int) Math.round(10 * newComplexPowerPriceMax);
		int activeDiff = activeMax - activeMin;

		
		for (long i = startTime; i < endTime; i = i + constantPeriod) {
			// calculate average percent of H0 profile above daily min
			double avgPercent = profile.getAvgPercentOfDailyMaxWithoutDailyMin(i);
			
			// ### generate new active price ###
			int avgActiveAbsolute = (int) Math.round(avgPercent * activeDiff);
			
			// check whether there is some correction necessary to achieve average price
			double activeMinCorrectionFactor = 1;
			double activeMaxCorrectionFactor = 1;
			if (activeMin + avgActiveAbsolute < activeAvg) {
				activeMinCorrectionFactor = 
						(avgPercent * activeMax - activeAvg)
						/ (avgPercent * activeMin - activeMin);
			}
			else if (activeMin + avgActiveAbsolute > activeAvg) {
				activeMaxCorrectionFactor =
						(activeAvg + (avgPercent - 1) * activeMin)
						/ (avgPercent * activeMax);
			}
			else {
				// no correction necessary
			}
			
			int newActiveMin = (int) Math.round(activeMinCorrectionFactor * activeMin);
			int newActiveMax = (int) Math.round(activeMaxCorrectionFactor * activeMax);
			int newActiveDiff = newActiveMax - newActiveMin;
			
			// calculate price
			double currentPercentOfMaxWithoutMin = profile.getPercentOfDailyMaxWithoutDailyMin(i);
			int newActiveValue = 
					newActiveMin + (int) Math.round(currentPercentOfMaxWithoutMin * newActiveDiff);
			
			// deviate
			//  calculate deviation at this price
			double newCurrentDeviationActiveMinPercent = ((double) (newActiveMin - activeMin)) / activeDiff;
			double newCurrentDeviationActiveMaxPercent = ((double) (activeMax - newActiveMax)) / activeDiff;
			double newCurrentDevActiveMinAbsolute = 
					(1 - newCurrentDeviationActiveMinPercent) * maxDeviationMinPriceInPercent
						+ newCurrentDeviationActiveMinPercent * maxDeviationMaxPriceInPercent;
			double newCurrentDevActiveMaxAbsolute = 
					newCurrentDeviationActiveMaxPercent * maxDeviationMinPriceInPercent
						+ (1 - newCurrentDeviationActiveMaxPercent) * maxDeviationMaxPriceInPercent;
			
			int newMaxDeviationActiveMinAbsolute = 
					(int) Math.min(newCurrentDevActiveMinAbsolute * newActiveMin, 
									(int) Math.floor(newActiveMin - minPrice * 10));
			int newMaxDeviationActiveMaxAbsolute =
					(int) Math.min(newCurrentDevActiveMaxAbsolute * newActiveMax,
									(int) Math.floor(maxPrice * 10 - newActiveMax));
			
			// calc absolute max deviation
			int newCurrentDeviationActiveAbsolute =
					(int) Math.round((1 - currentPercentOfMaxWithoutMin) * newMaxDeviationActiveMinAbsolute
										+ currentPercentOfMaxWithoutMin * newMaxDeviationActiveMaxAbsolute);
			
			// reduce newActiveValue with absolute max deviation
			newActiveValue = newActiveValue - newCurrentDeviationActiveAbsolute;
			
			// generate random deviation
			//  E(x) = n * p = trials * p = newCurrentDeviationActiveAbsolute
			BinomialDistribution binDistributionActive = 
					new BinomialDistribution(2 * newCurrentDeviationActiveAbsolute, 0.5);
			double randActiveMin = randomGenerator.getNextDouble();
			for (int j = 0; j <= 2 * newCurrentDeviationActiveAbsolute; j++) {
				if (binDistributionActive.cumulativeProbability(j) > randActiveMin) {
					newActiveValue += j;
					break;
				}
			}
			
			// transform back from cents * 10 to cents
			double finalActiveValue = (double) newActiveValue / 10.0;
			complexPriceSignal.setPrice(i, finalActiveValue);
		}

		complexPriceSignal.setKnownPriceInterval(startTime, endTime);
		return complexPriceSignal;
	}
	
	
	public static PriceSignal getConstantPriceSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			double price,
			AncillaryCommodity commodity ) {
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++){
			
			long timestamp = startTime + i * constantPeriod;

			priceSignal.setPrice(timestamp, price);
		}
		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		return priceSignal;
	}
	
	public static PriceSignal getLinearComplexPriceSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			double powerPriceStart,
			double powerPriceEnd,
			AncillaryCommodity commodity ) {
		
		PriceSignal complexPriceSignal = new PriceSignal(commodity);
		
		long numberOfPriceSteps = (endTime - startTime) / constantPeriod;
		
		for (long i = 0; i < numberOfPriceSteps; i++){
			
			double powerPrice = (
					(numberOfPriceSteps - i) * powerPriceStart
					+ i * powerPriceEnd)
					/ numberOfPriceSteps;
			
			long timestamp = startTime + i * constantPeriod;

			complexPriceSignal.setPrice(timestamp, powerPrice);
		}
		complexPriceSignal.setKnownPriceInterval(startTime, endTime);
		
		return complexPriceSignal;
	}
	
	public static PriceSignal getTriangularComplexPriceSignal(
			long startTime, 
			long peakTime, // peakTime in one go
			long endTime, 
			long constantPeriod,
			double complexPowerPriceStart,
			double complexPowerPricePeak,
			double complexPowerPriceEnd,
			AncillaryCommodity commodity) {
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		// adjust peakTime
		peakTime = startTime + (long) Math.round((double)(peakTime - startTime) / constantPeriod) * constantPeriod;
		
		long numberOfPriceStepsStartTillPeak = (peakTime - startTime) / constantPeriod;
		long numberOfPriceStepsPeakTillEnd = (endTime - peakTime) / constantPeriod;
		
		for (long i = 0; i < numberOfPriceStepsStartTillPeak; i++){
			
			double powerPrice = (
					(numberOfPriceStepsStartTillPeak - i) * complexPowerPriceStart
					+ i * complexPowerPricePeak)
					/ numberOfPriceStepsStartTillPeak;
			
			long timestamp = startTime + i * constantPeriod;

			priceSignal.setPrice(timestamp, powerPrice);
		}
		
		for (long i = 0; i < numberOfPriceStepsPeakTillEnd; i++){
			
			double powerPrice = (
					(numberOfPriceStepsPeakTillEnd - i) * complexPowerPricePeak
					+ i * complexPowerPriceEnd)
					/ numberOfPriceStepsPeakTillEnd;
			
			long timestamp = peakTime + i * constantPeriod;

			priceSignal.setPrice(timestamp, powerPrice);
		}
		
		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		return priceSignal;
	}
	
}
