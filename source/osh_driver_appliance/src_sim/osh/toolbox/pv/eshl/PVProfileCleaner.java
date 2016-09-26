package osh.toolbox.pv.eshl;

import java.util.Calendar;
import java.util.TimeZone;

import osh.utils.csv.CSVImporter;


/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@Deprecated
public class PVProfileCleaner {
	
	// ### configuration ###
	
	static String inputFileName = "sampleFiles/pv/*.csv";
	static String outputFileName = "configFiles/pv/*.csv";
	
	static String delimeter = ";";
	
	static int columnTimestamp = 0;
	static int columnActivePower = 1;
	static int columnReactivePower = -1;
	
	static boolean outputFileHasHeader = false;
	
	static boolean inputPowerIsNegative = true;
	static boolean outputPowerIsNegativ = true;
	
	static int timestep = 1; // in seconds
	static int days = 365;
	
	static int inputFirstTimestamp = 0; // first second of year
	static int outputFirstTimestamp = 0;
	
	// ### for later usage ###
	
	static int[] lastDayPower;
	static int[] currentDayPower;
	
	static int[][] inputArray;
	static int[][][] outputArray;
	
	static int[] biggestGapAtDay;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		lastDayPower = new int[86400];
		currentDayPower = new int[86400];
		
		inputArray = loadProfile(inputFileName);
		outputArray = new int[86400][days][2];
		
		biggestGapAtDay = new int[days];
		
//		int diffInputOutput = inputFirstTimestamp - outputFirstTimestamp;
		
		int lastTimestamp = inputArray[0][0];
		int lastSecondOfDay = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(lastTimestamp*1000L);
		int lastDayOfAra = cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR)*1000;
		
		for (int i = 0; i < inputArray.length; i++) {
			int timestamp = inputArray[i][0];
			int diff = timestamp - lastTimestamp;
			
			cal.setTimeInMillis(timestamp*1000L);
			int currentDayOfAra = cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR)*1000;
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			int currentDayStartTimestamp = (int) (cal.getTimeInMillis() / 1000L);
			int currentSecondOfDay = timestamp - currentDayStartTimestamp;
			
			if( currentDayOfAra != lastDayOfAra ) {
				// TAGESWECHSEL!
				// ...
			} else {
				int diffSeconds = currentSecondOfDay - lastSecondOfDay;
				
			}
			
			
		}
		
		
	}

	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, ";", "\"");
	}
	
}
