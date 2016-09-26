package osh.utils.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


/**
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class CSVImporter {

	//reads a List from a single-row document
	public static ArrayList<Integer> readIntegerList(String filename, String delimiter)
	{
		ArrayList<Integer> outputList = new ArrayList<Integer>();
		
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filename)));
			String _tmpString;
			while ((_tmpString = csvReader.readLine()) != null) {
				outputList.add(Integer.parseInt(_tmpString.split(delimiter)[0]));
			}
			csvReader.close();
		}
		catch (Exception ex)
		{
			System.out.println("Error reading csv-file: " +ex.getMessage());
		}
		
		return outputList;
	}
	
	//reads a List from a multi-row document
	public static ArrayList<ArrayList<String>> readMultirowList(String filename, String delimiter) {
		ArrayList<String> lineList = new ArrayList<String>();
		ArrayList<ArrayList<String>> outputList = new ArrayList<ArrayList<String>>();
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(new File(filename)));
			String _tmpString;
			while ((_tmpString = csvReader.readLine()) != null) {
				String[] _tmpLine = _tmpString.split(delimiter);
				for (String rowItem: _tmpLine) {
					//add it to the line...
					lineList.add(rowItem);
				}
				outputList.add(lineList);
			}
			
		}
		catch (Exception ex) 		{
			System.out.println("Error reading csv-file: " +ex.getMessage());
		}
		finally {
			try {
				csvReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return outputList;
	}
	
	/**
	 * 1-dim: lines
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public static double[] readDouble1DimArrayFromFile(String filename) {
		ArrayList<String> lines = new ArrayList<String>();
		double[] array = null;
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filename)));
			String line;
			
			while ((line = csvReader.readLine()) != null) {
				lines.add(line);
			}
			array = new double[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				array[i] = Double.valueOf(lines.get(i));
			}
			csvReader.close();
		}
		catch (Exception ex) {
			System.out.println("Error reading csv-file: " + ex.getMessage());
		}
		return array;
	}
	
	/**
	 * 1-dim: lines / rows # 2-dim: columns
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public static double[][] readDouble2DimArrayFromFile(String filename, String delimiter) {
		ArrayList<String> lines = new ArrayList<String>();
		
		double[][] array = null;
		
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filename)));
			String line;
			
			while ((line = csvReader.readLine()) != null) {
				lines.add(line);
			}
			
			array = new double[lines.size()][];
			
			for (int i = 0; i < lines.size(); i++) {
				String[] splittedLine = lines.get(i).split(delimiter);
				array[i] = new double[splittedLine.length];
				
				for (int j = 0; j < splittedLine.length; j++) {
					array[i][j] = Double.valueOf(splittedLine[j]);
				}
			}
			
			csvReader.close();
			
		}
		catch (Exception ex) {
			System.out.println("Error reading csv-file: " + ex.getMessage());
		}
		
		return array;
	}
	
	/**
	 * 1-dim: columns # 2-dim: lines / rows
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public static double[][] readAndTransposeDouble2DimArrayFromFile(String filename, String delimiter) {
		double[][] arr = readDouble2DimArrayFromFile(filename, delimiter);
		
		double[][] transposedArray = new double[arr[0].length][arr.length];
		
		for (int d0 = 0; d0 < arr.length; d0++) {
			for (int d1 = 0; d1 < arr[d0].length; d1++) {
				transposedArray[d1][d0] = arr[d0][d1];
			}
		}
		
		return transposedArray;
	}
	
	/**
	 * 1-dim: lines / rows # 2-dim: columns
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public static int[][] readInteger2DimArrayFromFile(String filename, String delimiter, String toBeDeleted) {
		ArrayList<String> lines = new ArrayList<String>();
		
		int[][] array = null;
		
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filename)));
			String line;
			
			while ((line = csvReader.readLine()) != null) {
				if (toBeDeleted != null) {
					line = line.replaceAll(toBeDeleted, "");
				}
				lines.add(line);
//				System.out.println(line);
			}
			
			array = new int[lines.size()][];
			
			for (int i = 0; i < lines.size(); i++) {
				String[] splittedLine = lines.get(i).split(delimiter);
				array[i] = new int[splittedLine.length];
				
				for (int j = 0; j < splittedLine.length; j++) {
					array[i][j] = Integer.valueOf(splittedLine[j]);
				}
			}
			
			csvReader.close();
			
		}
		catch (Exception ex) {
			System.out.println("Error reading csv-file: " + ex.getMessage());
		}
		
		return array;
	}
	
	
	/**
	 * 1-dim: lines / rows # 2-dim: columns
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public static String[][] readString2DimArrayFromFile(String filename, String delimiter, String toBeDeleted) {
		ArrayList<String> lines = new ArrayList<String>();
		
		String[][] array = null;
		
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filename)));
			String line;
			
			while ((line = csvReader.readLine()) != null) {
				if (toBeDeleted != null) {
					line = line.replaceAll(toBeDeleted, "");
				}
				lines.add(line);
//				System.out.println(line);
			}
			
			array = new String[lines.size()][];
			
			for (int i = 0; i < lines.size(); i++) {
				String[] splittedLine = lines.get(i).split(delimiter);
				array[i] = new String[splittedLine.length];
				
				for (int j = 0; j < splittedLine.length; j++) {
					array[i][j] = splittedLine[j];
				}
			}
			
			csvReader.close();
			
		}
		catch (Exception ex) {
			System.out.println("Error reading csv-file: " + ex.getMessage());
		}
		
		return array;
	}
}
