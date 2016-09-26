package osh.comdriver;

import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.comdriver.weather.CurrentWeatherRequestThread;
import osh.comdriver.weather.WeatherPredictionRequestThread;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class WeatherPredictionProviderComDriver extends CALComDriver implements IEventTypeReceiver, IHasState {

	// Current weather
	// http://api.openweathermap.org/data/2.5/weather?id=ID&APPID=APPID

	// Forecast
	// http://api.openweathermap.org/data/2.5/forecast/city?id=ID&APPID=APPID

	private String urlToCurrentWeather = "http://api.openweathermap.org/data/2.5/weather?id=2892794&APPID=";
	private String urlToWeatherPrediction = "http://api.openweathermap.org/data/2.5/forecast/city?id=2892794&APPID=";
	private String apiKey = "API_KEY";

//	private WeatherPredictionDetails weatherdetails = null;

	private WeatherPredictionRequestThread reqPredictionRunnable;
	private Thread reqPredictionThread;

	private CurrentWeatherRequestThread reqCurrentRunnable;
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
	public WeatherPredictionProviderComDriver(IOSH controllerbox, UUID deviceID, OSHParameterCollection driverConfig) throws HALException {
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

		// init weather prediction request thread
		reqPredictionRunnable = new WeatherPredictionRequestThread(getGlobalLogger(), this, this.urlToWeatherPrediction,
				this.apiKey);
		reqPredictionThread = new Thread(reqPredictionRunnable, "DachsInformationRequestThread");
		reqPredictionThread.start();

		// init current weather request thread
		reqCurrentRunnable = new CurrentWeatherRequestThread(getGlobalLogger(), this, this.urlToCurrentWeather,
				this.apiKey);
		reqCurrentThread = new Thread(reqCurrentRunnable, "DachsInformationRequestThread");
		reqCurrentThread.start();

	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();

		// still alive message
		if (getTimer().getUnixTime() % 60 == 0) {
			getGlobalLogger().logDebug("onNextTimePeriod() (getTimer().getUnixTime() % 60 == 0) - I'm still alive");
		}

		// re-init request thread if dead
		if (reqPredictionThread == null || !reqPredictionThread.isAlive()) {
			getGlobalLogger().logError("Restart WeatherPredictionRequestThread");
			
			reqPredictionRunnable = new WeatherPredictionRequestThread(getGlobalLogger(), this,
					this.urlToWeatherPrediction, this.apiKey);
			reqPredictionThread = new Thread(reqPredictionRunnable, "PredictionThread");
			reqPredictionThread.start();
		}
		if (reqCurrentThread == null || !reqCurrentThread.isAlive()) {
			getGlobalLogger().logError("Restart CurrentWeatherRequestThread");
			
			reqCurrentRunnable = new CurrentWeatherRequestThread(getGlobalLogger(), this, this.urlToCurrentWeather,
					this.apiKey);
			reqCurrentThread = new Thread(reqCurrentRunnable, "CurrentThread");
			reqCurrentThread.start();
		}

	}

	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();

		reqPredictionRunnable.shutdown();
		reqCurrentRunnable.shutdown();
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
			//TODO: this was changed from driverRegistry to ComRegistry please fix all other classes depending on recieveing this on the driverRegistry (via DataBroker)
			// set raw details
			this.getComRegistry().setStateOfSender(CurrentWeatherDetails.class, currentWeatherDetails);
			getGlobalLogger().logDebug("set new state" + currentWeatherDetails);
		}
	}

	public void receivePredictionDetails(WeatherPredictionDetails weatherDetails) {
		synchronized (weatherDetails) {
			//TODO: see above
			// set raw details
			this.getComRegistry().setStateOfSender(WeatherPredictionDetails.class, weatherDetails);
			getGlobalLogger().logDebug("set new state" + weatherDetails);
		}
	}

}
