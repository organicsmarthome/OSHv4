package osh.comdriver.details;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;
import osh.openweathermap.current.CurrentWeatherMap;
import osh.utils.DeepCopy;

/**
 * http://openweathermap.org/current
 * @author Jan Mueller
 *
 */

public class CurrentWeatherDetails extends StateExchange {

	/** SERIAL */
	private static final long serialVersionUID = -2129466555073434421L;

	private CurrentWeatherMap currentWeatherMap;

	/**
	 * CONSTRUCTOR
	 */
	public CurrentWeatherDetails(UUID sender, long timestamp, CurrentWeatherMap currentWeatherMap) {
		super(sender, timestamp);
		
		this.currentWeatherMap = (CurrentWeatherMap) DeepCopy.copy(currentWeatherMap);
	}

	public CurrentWeatherMap getTemperatureForecastList() {
		return currentWeatherMap;
	}

}
