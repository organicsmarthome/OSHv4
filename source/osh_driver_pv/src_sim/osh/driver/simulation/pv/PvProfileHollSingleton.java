package osh.driver.simulation.pv;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.utils.csv.CSVImporter;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser
 * TODO make singleton
 */
public class PvProfileHollSingleton {
	
	private static int[][] pvProfileArray;
	
	private final double cosPhiMax;
	
	// TODO Make Singleton!
	
	/** 
	 * CONSTRUCTOR
	 */
	public PvProfileHollSingleton(
			int nominalPower,
			String pathToProfile,
			double profileNominalPower,
			double cosPhiMax) {
		
		this.cosPhiMax = cosPhiMax;
		init(pathToProfile, nominalPower, profileNominalPower);
	}
	
	
	private void init(String pvProfileFilename, int nominalPower, double profileNominalPower) {
		int[][] pvProfileFile = CSVImporter.readInteger2DimArrayFromFile(pvProfileFilename, ";", null);
		pvProfileArray = new int[365][1440];
		for (int day = 0; day < 365; day++) {
			for (int minute = 0; minute < 1440; minute++) {
				pvProfileArray[day][minute] = (int) Math.round((pvProfileFile[day * 1440 + minute][0] / profileNominalPower * nominalPower));
			}
		}
	}
	
	
	/**
	 * IMPORTANT: Value <= 0 (Generating Power!)
	 * @param timeStamp
	 * @return Value <= 0 in W
	 */
	public int getPowerAt(long timeStamp) {
		int day = TimeConversion.convertUnixTime2CorrectedDayOfYear(timeStamp);
		if (day > 365 ) {
			day = 365;
		}
		int minute = TimeConversion.convertUnixTime2SecondsSinceMidnight(timeStamp) / 60;
		
		int power = pvProfileArray[day][minute];
		
		// to be safe...
		if (power > 0) {
			power = (-1) * power;
		}
		
		return power;
	}
	
	public SparseLoadProfile getPowerForDay(int dayOfYear) {
		if (dayOfYear > 365 ) {
			dayOfYear = 365;
		}
		
		SparseLoadProfile slp = new SparseLoadProfile();
		
		for (int minute = 0; minute < 1440; minute++) {
			int activePower = pvProfileArray[dayOfYear][minute];
			// to be safe...
			if (activePower > 0) {
				activePower = (-1) * activePower;
			}
			
			slp.setLoad(Commodity.ACTIVEPOWER, minute * 60, activePower);
			try {
				slp.setLoad(Commodity.REACTIVEPOWER, minute * 60, (int) ComplexPowerUtil.convertActiveToReactivePower(
						activePower, 
						this.cosPhiMax, 
						true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		slp.setEndingTimeOfProfile(86400);	
		
		return slp;
	}
	
	public int[][] getPvProfileArray() {
		return pvProfileArray;
	}
	
}
