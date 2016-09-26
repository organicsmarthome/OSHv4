package osh.comdriver;

import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.comdriver.weather.CurrentWeatherProviderWAMPDispatcher;
import osh.comdriver.weather.WeatherPredictionProviderWAMPDispatcher;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Jan Mueller
 *
 */
public class WeatherProviderWAMPDriver extends CALComDriver implements IEventTypeReceiver, IHasState, Runnable {

	private WeatherPredictionProviderWAMPDispatcher weatherPredictionProviderWAMPDispatcher;


	private CurrentWeatherProviderWAMPDispatcher currentWeatherProviderWAMPDispatcher;
	private Thread reqCurrentThread;
	private UUID loggerUUID;
	

	/**
	 * CONSTRUCTOR
	 * 
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws HALException 
	 */
	public WeatherProviderWAMPDriver(IOSH controllerbox, UUID deviceID, OSHParameterCollection driverConfig) throws HALException {
		super(controllerbox, deviceID, driverConfig);
		
		String loggerUUID = driverConfig.getParameter("loggeruuid");
		if( loggerUUID == null ) {
			throw new HALException("Need config parameter loggeruuid");
		}
		this.loggerUUID = UUID.fromString(loggerUUID);

	}

	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		this.getTimer().registerComponent(this, 3600);

		this.currentWeatherProviderWAMPDispatcher = new CurrentWeatherProviderWAMPDispatcher(getGlobalLogger(), this);
		this.weatherPredictionProviderWAMPDispatcher = new WeatherPredictionProviderWAMPDispatcher(getGlobalLogger(), this);
		
		new Thread(this, "pull proxy of WeatherProviderDriver to WAMP").start();

	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();

		// still alive message
		if (getTimer().getUnixTime() % 60 == 0) {
			getGlobalLogger().logDebug("onNextTimePeriod() (getTimer().getUnixTime() % 60 == 0) - I'm still alive");
		}

	}

	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();

	}

	@Override
	public void run() {
		while (true) {
			synchronized (this.currentWeatherProviderWAMPDispatcher) {
				try { // wait for new data
					this.currentWeatherProviderWAMPDispatcher.wait();
				} catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}

				// long timestamp = getTimer().getUnixTime();

				// if ( currentWeatherProviderWAMPDispatcher. ().isEmpty() ) {
				// // an error has occurred
				// }
			}
			synchronized (this.weatherPredictionProviderWAMPDispatcher) {
				try { // wait for new data
					this.weatherPredictionProviderWAMPDispatcher.wait();
				} catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}

//				long timestamp = getTimer().getUnixTime();
				// if ( currentWeatherProviderWAMPDispatcher. ().isEmpty() ) {
				// // an error has occurred
				// }
			}
		}
	}

	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		//NOTHING
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

	public void receiveCurrentDetails(CurrentWeatherDetails currentWeatherDetails) {
		synchronized(currentWeatherDetails) {
			//TODO: this was changed from driverRegistry to ComRegistry please fix all other classes depending on receiving this on the driverRegistry (via DataBroker)
			// set raw details
			this.getComRegistry().setState(CurrentWeatherDetails.class,this, currentWeatherDetails);
			getGlobalLogger().logDebug("set new state" + currentWeatherDetails);
		}
	}

	public void receivePredictionDetails(WeatherPredictionDetails weatherDetails) {
		synchronized (weatherDetails) {
			//TODO: see above
			// set raw details
			this.getComRegistry().setState(WeatherPredictionDetails.class,this, weatherDetails);
			getGlobalLogger().logDebug("set new state" + weatherDetails);
		}
	}

}
