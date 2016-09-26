package osh.mgmt.commanager;

import java.util.UUID;

import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.registry.interfaces.IEventTypeReceiver;



/**
 * 
 * @author Jan Mueller
 *
 */
public class WeatherPredictionComManager extends ComManager implements IEventTypeReceiver {

	CurrentWeatherDetails currentWeatherDetails;
	WeatherPredictionDetails weatherPredictionDetails;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public WeatherPredictionComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();	
		getTimer().registerComponent(this, 1);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();

	}
	
	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		// NOTHING
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		// NOTHING
	}	
	
}
