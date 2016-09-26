package osh.driver.simulation.pv;

import java.util.Calendar;
import java.util.TimeZone;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;


/**
 * Resolution = 15 minutes (intervals)
 * @author Ingo Mauser
 */
public class PvProfile {

	/** [Month][Day(Mo,Tu/We/Th,Fr,Sa,Su)][Hour] */
	private int[][][] pvProfileArray;
	
	private int nominalPower;
	
	
	/**
	 * 
	 * @param pvProfileFilename
	 * @param nominalPower in W
	 */
	public PvProfile(String pvProfileFilename, int nominalPower) {
		this.nominalPower = nominalPower;
		init(pvProfileFilename, nominalPower);
	}
	
	private void init(String pvProfileFilename, int nominalPower) {
		double[][] pvProfileFile = CSVImporter.readDouble2DimArrayFromFile(pvProfileFilename, ";");
		pvProfileArray = new int[12][1][96];
		for (int j = 0; j < 96; j++) {
			for (int k = 0; k < 12; k++) {
				pvProfileArray[k][0][j] = (int) Math.round((pvProfileFile[j][k] * nominalPower));
			}
		}
	}
	
	/**
	 * IMPORTANT: Value <= 0 (Generating Power!)
	 * @param timeStamp
	 * @return Value <= 0 in W
	 */
	public int getPowerAt(long timeStamp) {
		int month = TimeConversion.convertUnixTime2MonthInt(timeStamp);
		int time = TimeConversion.convertUnixTime2SecondsSinceMidnight(timeStamp);
		
		// Do NOT use Math.round()!!!
		int interval = (int) ((double) time / (60 * 15));
		
		int power = pvProfileArray[month][0][interval];
		
//		// randomize
//		int day = (int) (timeStamp / 86400);
//		day = day % randomDay.length;
//		power = (int) (2 * power * randomDay[day]);
		
		// to be safe...
		if (power > 0) {
			power = (-1) * power;
		}
		
		return power;
	}
	
	public SparseLoadProfile getProfileForDayOfYear(int dayOfYear) {
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		_calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
		
		int month = _calendar.get(Calendar.MONTH) - 1;
		int startIntervall = (_calendar.get(Calendar.DAY_OF_MONTH) - 1) * 96;
		
		SparseLoadProfile slp = new SparseLoadProfile();
		
		for (int i = 0; i < 96; i++) {
			
			int power = pvProfileArray[month][0][startIntervall + i];
			
			// to be safe...
			if (power > 0) {
				power = (-1) * power;
			}
			
			slp.setLoad(Commodity.ACTIVEPOWER, i * 15, power);
		}

		slp.setEndingTimeOfProfile(86400);
		return slp;
	}
	
	public int getNominalPower() {
		return nominalPower;
	}
	
	public int[][][] getPvProfileArray() {
		return pvProfileArray;
	}
	
}
