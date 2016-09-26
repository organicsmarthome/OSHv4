package osh.toolbox.pv.eshl;

import osh.utils.csv.CSVImporter;

public class DetectGapsInPvProfile {
	
//	static String inputFileName = "C:/pv/raw_2_2011_0.csv";
//	static String inputFileName = "C:/pv/raw_2_2012_0.csv";
	static String inputFileName = "C:/pv/raw_3_20112012_0.csv";
	
	static String delimeter = ";";
	
	static int minLengthForGap = 1 * 60 * 60;
	
	static int[][] inputArray;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		inputArray = loadProfile(inputFileName);
		
		int lastTime = 0;
		
		for (int i = 0; i < inputArray.length; i++) {
			int currentTime = inputArray[i][0];
			int diff = currentTime - lastTime;
			if (diff >= minLengthForGap) {
				System.out.println("Gap: " + lastTime + " : " + currentTime);
			}
			lastTime = currentTime;
		}
	}
	
	
	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, delimeter, "\"");
	}

}
