package osh.toolbox.pv.eshl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import osh.utils.csv.CSVImporter;


public class ReduceTimeStampBy {
	
//	static String inputFileName = "C:/pv/raw_2011.csv";
//	static String outputFileName = "C:/pv/raw_2_2011_0.csv";
	
	static String inputFileName = "C:/pv/raw_2012.csv";
	static String outputFileName = "C:/pv/raw_2_2012_0.csv";

	static String delimeter = ";";
	
	static int y2011 = 1293836400;
	static int y2012 = 1325372400;
	static int y2013 = 1356994800;
	
//	static boolean leapYear = false;
	
//	static int yearCorrection = y2011;
	static int yearCorrection = y2012;
	
	static int[][] inputArray;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		inputArray = loadProfile(inputFileName);
		
		File outputFile = new File(outputFileName);
		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
		
		for (int i = 0; i < inputArray.length; i++) {
			String line = "" + (inputArray[i][0] - yearCorrection) + delimeter + inputArray[i][1];
			writer.println(line);
		}
		
		writer.close();
		
	}
	
	
	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, delimeter, "\\\"");
	}

}
