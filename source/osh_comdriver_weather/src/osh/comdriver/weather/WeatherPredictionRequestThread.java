package osh.comdriver.weather;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import osh.comdriver.WeatherPredictionProviderComDriver;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.prediction.PredictedWeatherMap;

/**
 * 
 * @author Jan Mueller, Ingo Mauser
 *
 */
public class WeatherPredictionRequestThread implements Runnable {

	private IGlobalLogger globalLogger;
	private WeatherPredictionProviderComDriver comDriver;
	private String urlWeatherPrediction;
	private String apiKey;
	
	private boolean shutdown;
	private Date lastException = new Date();
	private int reconnectWait;
	
	private final int logEverySeconds = 1 * 60;
	private long lastLog = 0;
	
	
	/**
	 * CONSTRUCTOR
	 * @param globalLogger
	 * @param WeatherPredictionProviderComDriver
	 * @param WeatherPredictionProviderComDriver
	 */
	public WeatherPredictionRequestThread(
			IGlobalLogger globalLogger, 
			WeatherPredictionProviderComDriver weatherPredictionProviderComDriver, 
			String urlWeatherPrediction,
			String  apiKey) {
		this.globalLogger = globalLogger;
		this.comDriver = weatherPredictionProviderComDriver;
		this.urlWeatherPrediction = urlWeatherPrediction;
		this.apiKey = apiKey;
	}
	
	
	@Override
	public void run() {
		while (!shutdown) {
			
			if (comDriver.getTimer().getUnixTime() - lastLog >= logEverySeconds) {
				try {
					// get WeatherPredictionn
					WeatherPredictionDetails weatherDetails = new WeatherPredictionDetails(
							comDriver.getDeviceID(), 
							comDriver.getTimer().getUnixTime(),
							getWeatherPrediction( this.urlWeatherPrediction, this.apiKey));
					
					// send WeatherPrediction to ComDriver
					comDriver.receivePredictionDetails(weatherDetails);
					
					this.lastLog = comDriver.getTimer().getUnixTime();
				} 
				catch (Exception e) {
					this.globalLogger.logError("Reading weather prediction info failed", e);
					
					long diff = new Date().getTime() - lastException.getTime();
					if (diff < 0 || diff > 300000) {
						reconnectWait = 0;
					}
					else {
						if (reconnectWait <= 0) {
							reconnectWait = 1;
						}
						reconnectWait *= 2;
						if (reconnectWait > 180) {
							reconnectWait = 180;
						}
					}
					lastException = new Date();
					
					try {
						Thread.sleep(reconnectWait * 1000);
					} catch (InterruptedException e1) {}
				}
			}
			
			try {
				Thread.sleep(10);
			} 
			catch (InterruptedException e) {}
		}
	
	}

	public void shutdown() {
		this.shutdown = true;
	}
	
	
	public PredictedWeatherMap getWeatherPrediction( String urlToWeatherAPI, String apiKey) throws OSHException {
			
		ObjectMapper mapper = new ObjectMapper();
		PredictedWeatherMap obj = null;
		
		try {
			 obj = mapper.readValue(new URL(urlToWeatherAPI + apiKey), PredictedWeatherMap.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;		
	}
	
}
