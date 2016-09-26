package osh.toolbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import osh.utils.csv.CSVImporter;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class cleanProfile {
	
	private static String inputFileName = "sampleFiles/fzi/raw/eebus_4_fridge_2013-07-08_-_2013-07-14_a.csv";
	private static String outputFileName = "sampleFiles/fzi/clean/clean_eebus_4_fridge_2013-07-08_-_2013-07-14_a.csv";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int[][] profile = loadProfile(inputFileName);
		
		int firstTick = profile[0][0];
		
		int[][] cleanedProfile = new int[profile.length][profile[0].length];
		
		for ( int i = 0; i < profile.length; i++ ) {
			cleanedProfile[i][0] = profile[i][0] - firstTick;
			cleanedProfile[i][1] = profile[i][1];
		}
		
		File outputFile = new File(outputFileName);
		
		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
		
		int lastTick = -1;
		int lastValue = 0;
		
		for ( int i = 0; i < profile.length; i++ ) {
			if ( cleanedProfile[i][0] - lastTick > 1) {
				for ( int j = 0; j < cleanedProfile[i][0] - lastTick - 1; j++ ) {
					writer.println((lastTick + j + 1) + ";" + lastValue);
				}
			}
			
			writer.println(cleanedProfile[i][0] + ";" + cleanedProfile[i][1]);
			
			lastTick = cleanedProfile[i][0];
			lastValue = cleanedProfile[i][1];
		}
		
		writer.close();
		
		System.out.println("DONE");
	}
	
	
	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, ";", "\"");
	}

}
