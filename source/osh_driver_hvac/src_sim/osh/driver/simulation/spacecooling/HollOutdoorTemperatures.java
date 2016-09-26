package osh.driver.simulation.spacecooling;

import osh.core.logging.IGlobalLogger;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class HollOutdoorTemperatures extends OutdoorTemperatures {
	
	private static String fileAndPath = "configfiles/temperature/outdoorTemperatures_jufe.csv";
	
	/**
	 * CONSTRUCTOR
	 */
	public HollOutdoorTemperatures(IGlobalLogger logger) {
		super(logger, fileAndPath);
	}
	
}
