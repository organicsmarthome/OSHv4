package osh.utils.physics;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class TemperatureUtil {
	
	private static double kelvinShift = 273.15;
	
	public static double convertCelsiusToKelvin(double celsius) {
		return celsius + kelvinShift;
	}
	
	public static double convertKelvinToCelsius(double kelvin) {
		return Math.max(0, kelvin - kelvinShift);
	}
}
