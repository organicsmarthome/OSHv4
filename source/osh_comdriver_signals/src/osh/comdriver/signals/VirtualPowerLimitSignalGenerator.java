package osh.comdriver.signals;

import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.PowerInterval;

public class VirtualPowerLimitSignalGenerator {
	
	/**
	 * 
	 * @param startTime
	 * @param endTime EXCLUSIVE!

	 * @return
	 */
	public static PowerLimitSignal getConstantPowerLimitSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			PowerInterval complexPowerLimitInterval) {
		
		PowerLimitSignal complexPowerLimitSignal = new PowerLimitSignal();
		
		long numberOfSteps = (endTime - startTime) / constantPeriod;
		
		for (long i = 0; i < numberOfSteps; i++){
			
			long timestamp = startTime + i * constantPeriod;

			complexPowerLimitSignal.setPowerLimit(timestamp, complexPowerLimitInterval);
		}
		complexPowerLimitSignal.setKnownPowerLimitInterval(startTime, endTime);
		
		return complexPowerLimitSignal;
	}
	
	public static PowerLimitSignal getLinearPowerLimitSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			PowerInterval complexPowerLimitIntervalStart,
			PowerInterval complexPowerLimitIntervalEnd) {
		
		PowerLimitSignal complexPowerLimitSignal = new PowerLimitSignal();
		
		long numberOfSteps = (endTime - startTime) / constantPeriod;
		
		for (long i = 0; i < numberOfSteps; i++){
			
			double powerUpperLimit = (
					(numberOfSteps - i) * complexPowerLimitIntervalStart.getPowerUpperLimit()
					+ i * complexPowerLimitIntervalStart.getPowerUpperLimit())
					/ numberOfSteps;
			double powerLowerLimit = (
					(numberOfSteps - i) * complexPowerLimitIntervalStart.getPowerLowerLimit()
					+ i * complexPowerLimitIntervalStart.getPowerLowerLimit())
					/ numberOfSteps;
			
//			double reactivePowerUpperLimit = (
//					(numberOfSteps - i) * complexPowerLimitIntervalStart.getReactivePowerUpperLimit()
//					+ i * complexPowerLimitIntervalStart.getReactivePowerUpperLimit())
//					/ numberOfSteps;
//			double reactivePowerLowerLimit = (
//					(numberOfSteps - i) * complexPowerLimitIntervalStart.getReactivePowerLowerLimit()
//					+ i * complexPowerLimitIntervalStart.getReactivePowerLowerLimit())
//					/ numberOfSteps;
			
			
			PowerInterval complexPowerLimitInterval = new PowerInterval(
					powerUpperLimit, 
					powerLowerLimit);
			
			long timestamp = startTime + i * constantPeriod;

			complexPowerLimitSignal.setPowerLimit(timestamp, complexPowerLimitInterval);
		}
		complexPowerLimitSignal.setKnownPowerLimitInterval(startTime, endTime);
		
		return complexPowerLimitSignal;
	}
	
	

	public static PowerLimitSignal getH0basedPowerLimitSignal(
			PriceSignal priceSignal,
			PowerInterval upperAndLowerPositivInterval) {
		
		PowerLimitSignal complexPowerLimitSignal = new PowerLimitSignal();
		
		long signalStartTime = priceSignal.getPriceUnknownBefore(); 
		long signalEndTime	= priceSignal.getPriceUnknownAtAndAfter();
		double minPrice = Double.MAX_VALUE, maxprice = -Double.MAX_VALUE;
		
		for (long i = signalStartTime; i < signalEndTime; i = priceSignal.getNextPriceChange(i)) {
			
			double currentPrice = priceSignal.getPrice(i);
			
			if (currentPrice < minPrice) minPrice = currentPrice;
			if (currentPrice > maxprice) maxprice = currentPrice;
		}
		
		if (Double.isInfinite(maxprice-minPrice)) {
			maxprice = 0.0;
			minPrice = 0.0;
		}
		
		double powerLimitDiff = upperAndLowerPositivInterval.getPowerUpperLimit() - upperAndLowerPositivInterval.getPowerLowerLimit();
		double priceDiff = maxprice - minPrice;
		
		for (long i = signalStartTime; i < signalEndTime; i = priceSignal.getNextPriceChange(i)) {
			
			double currentPrice = priceSignal.getPrice(i);
			double powerLimit = (1-((currentPrice-minPrice)/priceDiff))*powerLimitDiff+upperAndLowerPositivInterval.getPowerLowerLimit();
			complexPowerLimitSignal.setPowerLimit(i, powerLimit);
		}
		
	
		complexPowerLimitSignal.setKnownPowerLimitInterval(signalStartTime, signalEndTime);
		
		return complexPowerLimitSignal;
	}
	
	
	public static PowerLimitSignal getTriangularPowerLimitSignal(
			long startTime, 
			long peakTime, // peakTime in one go
			long endTime, 
			long constantPeriod,
			PowerInterval complexPowerLimitIntervalStart,
			PowerInterval complexPowerLimitIntervalPeak,
			PowerInterval complexPowerLimitIntervalEnd) {
		
		PowerLimitSignal complexPowerLimitSignal = new PowerLimitSignal();
		
		// adjust peakTime
		peakTime = startTime + (long) Math.round((double)(peakTime - startTime) / constantPeriod) * constantPeriod;
		
		long numberOfStepsStartTillPeak = (peakTime - startTime) / constantPeriod;
		long numberOfStepsPeakTillEnd = (endTime - peakTime) / constantPeriod;
		
		for (long i = 0; i < numberOfStepsStartTillPeak; i++){
			
			double upperLimit = (
					(numberOfStepsStartTillPeak - i) * complexPowerLimitIntervalStart.getPowerUpperLimit()
					+ i * complexPowerLimitIntervalStart.getPowerUpperLimit())
					/ numberOfStepsStartTillPeak;
			double lowerLimit = (
					(numberOfStepsStartTillPeak - i) * complexPowerLimitIntervalStart.getPowerLowerLimit()
					+ i * complexPowerLimitIntervalStart.getPowerLowerLimit())
					/ numberOfStepsStartTillPeak;
			
			PowerInterval complexPowerLimitInterval = new PowerInterval(upperLimit, lowerLimit);
			
			long timestamp = startTime + i * constantPeriod;

			complexPowerLimitSignal.setPowerLimit(timestamp, complexPowerLimitInterval);
		}
		
		for (long i = 0; i < numberOfStepsPeakTillEnd; i++){
			
			double upperLimit = (
					(numberOfStepsPeakTillEnd - i) * complexPowerLimitIntervalStart.getPowerUpperLimit()
					+ i * complexPowerLimitIntervalStart.getPowerUpperLimit())
					/ numberOfStepsPeakTillEnd;
			double lowerLimit = (
					(numberOfStepsPeakTillEnd - i) * complexPowerLimitIntervalStart.getPowerLowerLimit()
					+ i * complexPowerLimitIntervalStart.getPowerLowerLimit())
					/ numberOfStepsPeakTillEnd;
			
			PowerInterval complexPowerLimitInterval = new PowerInterval(upperLimit, lowerLimit);
			
			long timestamp = peakTime + i * constantPeriod;

			complexPowerLimitSignal.setPowerLimit(timestamp, complexPowerLimitInterval);
		}
		
		complexPowerLimitSignal.setKnownPowerLimitInterval(startTime, endTime);
		
		return complexPowerLimitSignal;
	}
	
	
	public static PowerLimitSignal getSinusIntervalPowerLimitSignal(
			long startTime, 
			long endTime,
			long constantPeriod,
			long periodLength,
			PowerInterval positivePeak,
			PowerInterval negativePeak) {
		
		
		PowerLimitSignal powerLimitSignal = new PowerLimitSignal();
		
		long numberOfSteps = (endTime - startTime) / constantPeriod;
		
		for (long i = 0; i < numberOfSteps; i++){
			
			long timestamp = startTime + i * constantPeriod;
			
			double sinus = Math.sin( ((double)(timestamp % periodLength)) / periodLength * 2.0 * Math.PI);
			
			double temp = Math.max(0, sinus + 1.0);
			temp = Math.max(0, temp / 2.0);
			
			double upperLimit = 
					temp * positivePeak.getPowerUpperLimit()
					+ (1 - temp) * negativePeak.getPowerUpperLimit();
			double lowerLimit = 
					temp * positivePeak.getPowerLowerLimit()
					+ (1 - temp) * negativePeak.getPowerLowerLimit();
			
			PowerInterval powerLimitInterval = new PowerInterval(upperLimit, lowerLimit);

			powerLimitSignal.setPowerLimit(timestamp, powerLimitInterval);
		}
		
		powerLimitSignal.setKnownPowerLimitInterval(startTime, endTime);
		return powerLimitSignal;
	}
	
}
