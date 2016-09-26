package osh.driver.simulation.thermal;

import java.io.IOException;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Florian Allerding, Sebastian Kramer, Ingo Mauser, Till Schuberth
 * 
 */
public class ThermalDemandData {
	
	private double[] sumDemand;
	private Commodity hotWaterType;

	
	/**
	 * CONSTRUCTOR
	 */
	public ThermalDemandData(
			String inputFile,
			Commodity hotWaterType) throws IOException {
		
		// red input file
		sumDemand = CSVImporter.readDouble1DimArrayFromFile(inputFile);
		this.hotWaterType = hotWaterType;
	}
	

	/**
	 * 
	 * @param timestamp
	 * @param randomDev e {0, 1, 2} -> +-1h
	 * @param randomDevMax % 2 == 0
	 * @param randomDevMax % 2 == 0
	 * @return
	 */
	public double getTotalThermalDemand(long timestamp, int randomDev, int randomDevMax) {
		int day = TimeConversion.convertUnixTime2CorrectedDayOfYear(timestamp);
		int hour = TimeConversion.convertUnixTime2SecondsSinceMidnight(timestamp) / 3600;
		
		hour = hour + randomDev - (randomDevMax / 2);
		if (hour < 0) {
			hour += 24;
			day -= 1;
		}
		if (hour > 23) {
			hour -= 24;
			day += 1;
		}
		
		if (day < 0 || day >= 365) {
			day = Math.floorMod(day, 365);
		}
		return sumDemand[day * 24 + hour];
	}
	
	public SparseLoadProfile getProfileForDayOfYear(int day) {
		if (day < 0 || day >= 365) {
			day = Math.floorMod(day, 365);
		}
		
		SparseLoadProfile slp = new SparseLoadProfile();
		
		for (int hour = 0; hour < 24; hour++) {
			slp.setLoad(hotWaterType, hour * 3600, (int) sumDemand[day * 24 + hour]);
		}
		
		slp.setEndingTimeOfProfile(86400);
		
		return slp;		
	}
	
}
