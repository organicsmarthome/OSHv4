package osh.utils.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DuplicateLines {
	public static void main(String[] args) {
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File("data/line_file.txt")));
			String _tmpString;
			while ((_tmpString = csvReader.readLine()) != null) {
				System.out.println(_tmpString);
				System.out.println(_tmpString);
			}
			csvReader.close();
		}
		catch (Exception ex)
		{
			System.out.println("Error reading csv-file: " +ex.getMessage());
		}
	}
	
}
