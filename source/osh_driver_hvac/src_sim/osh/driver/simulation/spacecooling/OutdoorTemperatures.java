package osh.driver.simulation.spacecooling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import osh.core.logging.IGlobalLogger;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class OutdoorTemperatures {
	
	private double temperatureCorrection = 2.0;
	private Map<Long, Double> valuesFromFile = null;
	private IGlobalLogger logger;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public OutdoorTemperatures(IGlobalLogger logger, String fileAndPath) {
		this.logger = logger;
		this.valuesFromFile = new HashMap<Long, Double>();
		
		BufferedReader file = null;
		
		try {
			file = new BufferedReader(new FileReader(fileAndPath ));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.logDebug("Error CSV file");
		}
		
		String line;
		
		try {
			while((line = file.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				@SuppressWarnings("unused")
				String id = st.nextToken();
				int day = Integer.parseInt(st.nextToken());
				int month = Integer.parseInt(st.nextToken()) - 1;
				int minute = Integer.parseInt(st.nextToken());
				int hour = Integer.parseInt(st.nextToken());
				double temperature = Double.parseDouble(st.nextToken());
									
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.set(1970, month, day, hour, minute, 0);
//				java.util.Date date = cal.getTime();
				long timeInSec = cal.getTimeInMillis() / 1000;
			    valuesFromFile.put(timeInSec, temperature + temperatureCorrection);
//			    logger.logDebug(month +" "+  day+" " +  hour+" " +  minute + " = " + timeInSec);
			}
			System.out.println();
		} 
		catch (IOException e) {
			e.printStackTrace();
			logger.logDebug("Error parsing file", e);
		}			
	}
	
	
	public double getTemperature(long timestamp) {
		try {
			return valuesFromFile.get( (timestamp / 300) * 300);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.logDebug(timestamp);
			logger.logDebug((timestamp / 300) * 300); 
			System.exit(0);
			return 22.0;
		}
	}
	
	/**
	 * Handle with care. Do NOT alter/modify/whatever to map!
	 */
	public Map<Long, Double> getMap() {
		return valuesFromFile;
	}

}
