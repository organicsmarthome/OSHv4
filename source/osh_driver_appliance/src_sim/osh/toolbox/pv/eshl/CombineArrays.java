package osh.toolbox.pv.eshl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import osh.utils.csv.CSVImporter;


public class CombineArrays {
	
	static String delimeter = ";";
	
	static String inputFileName1 = "C:/pv/raw_2_2011_0.csv";
	static String inputFileName2 = "C:/pv/raw_2_2012_0.csv";
	
	static String outputFileName = "C:/pv/raw_3_20112012_0.csv";
	
	static int[][] inputArray1;
	static int[][] inputArray2;
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		inputArray1 = loadProfile(inputFileName1);
		inputArray2 = loadProfile(inputFileName2);
		
		File outputFile = new File(outputFileName);
		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
		
		export(inputArray2,0,4572319,writer);
		export(inputArray1,4572319 + 1,4702612 - 1,writer);
		
		export(inputArray2,4702612,6894087,writer);
		export(inputArray1,6894087 + 1,6942567 - 1,writer);
		
		export(inputArray2,6942567,7932910,writer);
		export(inputArray1,7932910 + 1,8168314 - 1,writer);
		
		export(inputArray2,8168314,8471299,writer);
		export(inputArray1,8471299 + 1,8678613 - 1,writer);
		
		export(inputArray2,8678613,10939222 ,writer);
		export(inputArray1,10939222 + 1,11003374 - 1,writer);
		
		export(inputArray2,11003374,12285632 ,writer);
		export(inputArray1,12285632 + 1,12307603 - 1,writer);
		
		export(inputArray2,12307603,12421688 ,writer);
		export(inputArray1,12421688 + 1,12529555 - 1,writer);
		
		export(inputArray2,12529555,12805347 ,writer);
		export(inputArray1,12805347 + 1,12836392 - 1,writer);
		
		export(inputArray2,12836392,15509260 ,writer);
		export(inputArray1,15509260 + 1,15514250 - 1,writer);
		
		export(inputArray2,15514250,16020717 ,writer);
		export(inputArray1,16020717 + 1,16032236 - 1,writer);
		
		export(inputArray2,16032236,16037319 ,writer);
		export(inputArray1,16037319 + 1,16042054 - 1,writer);
		
		export(inputArray2,16042054,16811523 ,writer);
		export(inputArray1,16811523 + 1,16875202 - 1,writer);
		
		export(inputArray2,16875202,16889056 ,writer);
		export(inputArray1,16889056 + 1,17150491 - 1,writer);
		
		export(inputArray2,17150491,17405163,writer);
		export(inputArray1,17405163 + 1,99999999 - 1,writer);
		
		writer.close();

	}
	
	private static void export(int[][] arr, int from, int to, PrintWriter writer) {
		int[][] fromTo = getFromTo(arr, from, to);
		if (fromTo == null) {
			return;
		}
		for (int[] a : fromTo) {
			writer.println(a[0] + ";" + a[1]);
		}
	}

	private static int[][] getFromTo(int[][] arr, int from, int to) {
		int startindex = arr.length;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i][0] >= from) {
				startindex = i;
				break;
			}
		}
		
		int endindex = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i][0] <= to) {
				endindex = i;
			}
			else {
				break;
			}
		}
		
		if (endindex < startindex) {
			return null;
		}
		else {
			int cnt = 0;
			int[][] returnArr = new int[endindex - startindex + 1][2];
			for (int i = startindex; i <= endindex; i++) {
				returnArr[cnt] = arr[i];
				cnt++;
			}
			return returnArr;
		}
	}
	
	private static int[][] loadProfile(String fileName) {
		return CSVImporter.readInteger2DimArrayFromFile(fileName, delimeter, "\\\"");
	}
	
}
