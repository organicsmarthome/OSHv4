package osh.comdriver.signals;

import java.time.Period;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class PriceSignalGenerator {
	
	public static PriceSignal getConstantPriceSignal(
			AncillaryCommodity commodity,
			long startTime, 
			long endTime, 
			long constantPeriod,
			double price) {
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++){
			
			long timestamp = startTime + i * constantPeriod;

			priceSignal.setPrice(timestamp, price);
		}
		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		priceSignal.compress();
		
		return priceSignal;
	}
	
	public static PriceSignal getPriceSignalOfTreeMap(
			AncillaryCommodity commodity,
			long startTime, 
			long endTime,
			long constantPeriod,
			TreeMap<Long, Double> prices) {
		
		Double lastValue = null;
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++){
			
			long timestamp = startTime + i * constantPeriod;
			long timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(timestamp);
			Entry<Long, Double> en = prices.floorEntry(timeFromMidnight);
			
			if (lastValue != null) {
				if (en.getValue() != lastValue) {
					priceSignal.setPrice(timestamp - (timeFromMidnight - en.getKey()), en.getValue());
					lastValue = en.getValue();
				}					
			} else {
				priceSignal.setPrice(timestamp, en.getValue());
				lastValue = en.getValue();
			}			
		}

		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		priceSignal.compress();
		
		return priceSignal;
	}
	
	public static PriceSignal getPriceSignalWeekday(
			AncillaryCommodity commodity,
			long startTime, 
			long endTime,
			long constantPeriod,
			Double[] pricesPerWeekDay) {
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		long time = startTime;
		priceSignal.setPrice(time, pricesPerWeekDay[TimeConversion.convertUnixTime2CorrectedWeekdayInt(time)]);
		time = time - TimeConversion.convertUnixTime2SecondsSinceMidnight(time);
		
		while (true) { //Yeah, Yeah I know ...
			time = TimeConversion.addPeriodToUnixTime(time, Period.ofDays(1));
			if (time > endTime)
				break;
			
			priceSignal.setPrice(time, pricesPerWeekDay[TimeConversion.convertUnixTime2CorrectedWeekdayInt(time)]);
		}

		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		priceSignal.compress();
		
		return priceSignal;
	}
	
	public static PriceSignal getFlexiblePriceSignal(
			AncillaryCommodity commodity,
			long secondsFromYearStart,
			long startTime, 
			long endTime, 
			long constantPeriod,
			List<Double> priceSignalYear) {
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		double price = 0;
		
		int startIndex = (int) (secondsFromYearStart / constantPeriod);
		long steps = ((endTime - startTime) / constantPeriod);		
		
		for (int i = 0; i < steps; i++){
			
			long timestamp = startTime + i * constantPeriod;
			
			int actualIndex = (int) (i + startIndex);
			if (actualIndex > priceSignalYear.size() - 1) 
				actualIndex %= priceSignalYear.size();
			
			price= priceSignalYear.get(actualIndex);
			priceSignal.setPrice(timestamp, price);
		}
		priceSignal.setKnownPriceInterval(startTime, endTime);
		
		priceSignal.compress();
		
		return priceSignal;
	}
	
	
	public static PriceSignal getStepPriceSignal(
			long startTime, 
			long endTime, 
			long constantPeriod,
			int periodsToSwitch,
			AncillaryCommodity commodity,
			double priceMin,
			double priceMax,
			boolean startWithMinPrice) {
		
		boolean lowPricePeriod = startWithMinPrice;
		
		PriceSignal priceSignal = new PriceSignal(commodity);
		
		int counter = 0;
		
		for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++){
			
			long timestamp = startTime + i * constantPeriod;

			if (lowPricePeriod) {
				priceSignal.setPrice(timestamp, priceMin);
			}
			else {
				priceSignal.setPrice(timestamp, priceMax);
			}
			
			counter++;
			
			if (counter >= periodsToSwitch) {
				counter = 0;
				lowPricePeriod = !lowPricePeriod;
			}
		}
		
		priceSignal.setKnownPriceInterval(startTime, endTime);
		priceSignal.compress();
		
		return priceSignal;
	}
	
}
