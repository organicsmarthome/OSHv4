package osh.toolbox.pv.eshl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;


/**
 * IMPORTANT: first day of input file must be completely available and valid!
 * @author Ingo Mauser
 *
 */
public class SimplePVProfileCleaner {
	
	// ### configuration ###
	
	static String inputFileName = "C:/pv/raw_3_20112012_0.csv";
	
	static String outputFileName =  "C:/pv/cleaned_20112012_";
	static String outputFileExtension = ".csv";
	
	static String delimeter = ";";
	
	static int columnTimestamp = 0;
	static int columnActivePower = 1;
	static int columnReactivePower = -1;
	
	static boolean outputFileHasHeader = false;
	
	static boolean inputPowerIsNegative = true;
	static boolean outputPowerIsNegative = true;
	
	static int timestep = 1; // in seconds
	static int days = 365;
	
	static int inputFirstTimestamp = 0; // first second of year
	static int outputFirstTimestamp = 0;
	
	static int firstHourWithSun = 5;
	static int lastHourWithSun = 21;
	
	// ### for later usage ###
	
	static int[] lastDayPower;
	static int[] currentDayPower;
	
	static int[][] inputArray;
	static Integer[][][] outputArray;
	
	static int[] biggestGapAtDay;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		try {
			
			lastDayPower = new int[86400];
			currentDayPower = new int[86400];
			
			inputArray = loadProfile(inputFileName);
			outputArray = new Integer[86400][days][2];
			
			biggestGapAtDay = new int[days];
			
			int currentDay = 0;
			int currentSecondOfDay = 0;
			
			System.out.println("START: fill existing data");
			// fill existing data
			for (int i = 0; i < inputArray.length; i++) {
				int currentInputTime = inputArray[i][0];
				currentDay = TimeConversion.convertUnixTime2CorrectedDayOfYear(currentInputTime);
				currentSecondOfDay = TimeConversion.convertUnixTime2SecondsSinceMidnight(currentInputTime);
				outputArray[currentSecondOfDay][currentDay][0] = 
						outputFirstTimestamp + currentDay * 86400 + currentSecondOfDay;
				outputArray[currentSecondOfDay][currentDay][1] = inputArray[i][1];
			}
			System.out.println("END: fill existing data");
			
			inputArray = null;
			
			System.out.println("START: fill new array with timestamps (if no already filled in)");
			// fill new array with timestamps (if no already filled in)
			for (int i = 0; i < days; i++) {
				for (int j = 0; j < 86400; j++) {
					if (outputArray[j][i][0] == null) {
						outputArray[j][i][0] = i * 86400 + j;
					}
				}
			}
			System.out.println("END: fill new array with timestamps (if no already filled in)");
			
			System.out.println("START: set values bigger 0 to 0 or v.v.");
			// set values bigger 0 to 0 or v.v.
			for (int i = 0; i < days; i++) {
				for (int j = 0; j < 86400; j++) {
					if (outputArray[j][i][1] != null) {
						if (inputPowerIsNegative) {
							if (outputArray[j][i][1] > 0) {
//								outputArray[j][i][1] = 0;
								outputArray[j][i][1] = null;
							}
						}
						else {
							if (outputArray[j][i][1] < 0) {
//								outputArray[j][i][1] = 0;
								outputArray[j][i][1] = null;
							}
						}
					}
				}
			}
			System.out.println("END: set values bigger 0 to 0 or v.v.");
			
//			// at midnight : always no power
//			for (int i = 0; i < days; i++) {
//				outputArray[0][i][1] = 0;
//				outputArray[86400 - 1][i][1] = 0;
//			}
			
			System.out.println("START: set time without pv power to 0");
			// set time without pv power to 0
			for (int i = 0; i < days ; i++) {
				for (int j = 0; j < firstHourWithSun; j++) {
					outputArray[j][i][1] = 0;
				}
				for (int j = lastHourWithSun * 3600; j < 86400; j++) {
					outputArray[j][i][1] = 0;
				}
			}
			System.out.println("END: set time without pv power to 0");
			
			System.out.println("START: calculate max gaps");
			// calculate max gaps
			for (int i = 0; i < days; i++) {
				System.out.println("START: calculate max gaps. day: " + i);
				int counter = 0;
				for (int j = 0; j < 86400; j++) {
					if (outputArray[j][i][1] != null) {
						counter = 0;
					}
					else {
						counter++;
						biggestGapAtDay[i] = Math.max(counter, biggestGapAtDay[i]);
					}
				}
			}
			System.out.println("END: calculate max gaps");
			
			System.out.println("START: data cleansing");
			// data cleansing
			for (int i = 0; i < days; i++) {
				System.out.println("START: data cleansing. day: " + i);
				// interpolate if max gap is < 1 * 3600 seconds
				if ( biggestGapAtDay[i]  < 1 * 3600 ) {
					
//					boolean gapLeft = false;
//					for (int j = 0; j < 86400; j++) {
//						if (outputArray[j][i][1] == null) {
//							gapLeft = true;
//							break;
//						}
//					}
					
//					while (gapLeft) {
						
//						// get first null value
//						int firstNull = 86400;
//						for (int j = 0; j < 86400; j++) {
//							if (outputArray[j][i][1] == null) {
//								firstNull = j;
//								break;
//							}
//						}
					
					// get first null value
					
					int firstNull = 86400;
					for (int j = 0; j < 86400; j++) {
						if (outputArray[j][i][1] == null) {
							firstNull = j;
							break;
						}
					}
					
					while(firstNull < 86400) {
						
						// get first number after null value
						int firstNotNullAfterNull = 86400;
						for (int j = firstNull; j < 86400; j++) {
							if (outputArray[j][i][1] != null) {
								firstNotNullAfterNull = j;
								break;
							}
						}
						
						int divisor = firstNotNullAfterNull - firstNull + 1;
						
						// interpolate
						for (int j = firstNull; j < firstNotNullAfterNull; j++) {
							outputArray[j][i][1] = 
									(outputArray[firstNull - 1][i][1]  *  ( firstNotNullAfterNull - j)
									+ outputArray[firstNotNullAfterNull][i][1] * (j - firstNull + 1))
									/ divisor;
						}
						
//						gapLeft = false;
//						// check if gap left
//						for (int j = firstNotNullAfterNull; j < 86400; j++) {
//							if (outputArray[j][i][1] == null) {
//								gapLeft = true;
//								break;
//							}
//						}
						
						// get first null value
						firstNull = 86400;
						for (int j = firstNotNullAfterNull; j < 86400; j++) {
							if (outputArray[j][i][1] == null) {
								firstNull = j;
								break;
							}
						}
						
					}
					
				}
				else {
					// use previous day
					for (int j = 0; j < 86400; j++) {
						outputArray[j][i][1] = outputArray[j][i - 1][1];
					}
				}
			}

			// reverse sign of power value if necessary
			if (inputPowerIsNegative != outputPowerIsNegative) {
				for (int i = 0; i < days; i++) {
					for (int j = 0; j < 86400; j++) {
						outputArray[j][i][1] = (-1) * outputArray[j][i][1];
					}
				}
			}
			
			// data output
			for (int i = 0; i < days; i++) {
				File outputFile = new File(outputFileName + i + outputFileExtension);
				PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
				
				for (int j = 0; j < 86400; j++) {
//					String line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
					String line;
					
					if (j > 10000 && j < 45000) {
						line = "" + outputArray[j][i][0] + delimeter + outputArray[45000 + (45000 - j)][i][1];
					}
					else {
						line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
					}
					
					writer.println(line);
				}
				
				writer.close();
			}
			
			// full
			File outputFile = new File(outputFileName + "full" + outputFileExtension);
			PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
			for (int i = 0; i < days; i++) {
				for (int j = 0; j < 86400; j++) {
					String line;
//					line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
					if (j > 10000 && j < 45000) {
						line = "" + outputArray[j][i][0] + delimeter + outputArray[45000 + (45000 - j)][i][1];
					}
					else {
						line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
					}
					writer.println(line);
				}
			}
			writer.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, delimeter, "\"");
	}
	
}
