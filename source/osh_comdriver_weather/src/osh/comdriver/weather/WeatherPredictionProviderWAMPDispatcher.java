package osh.comdriver.weather;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import osh.comdriver.WeatherProviderWAMPDriver;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.current.CurrentWeatherMap;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

/**
 * Handling the connection to openweathermap via wamp
 * 
 * @author Jan Mueller
 *
 */
public class WeatherPredictionProviderWAMPDispatcher{

	private IGlobalLogger globalLogger;
	private WeatherProviderWAMPDriver comDriver;
	
	private boolean shutdown;
	
	private String url = "ws://wamp-router:8080/ws";
	private String realm = "eshl";
//	private String realm = "realm1";
	private String wampTopic = "eshl.openweathermap.v1.readout.currentweather";	
	
	private ObjectMapper mapper;
	private WampClient client;

	CurrentWeatherDetails weatherDetails;
	
	Subscription onDataSubscription;

	// Scheduler
	ExecutorService executor = Executors.newSingleThreadExecutor();
	Scheduler rxScheduler = Schedulers.from(executor);

	static final int TIMER_INTERVAL = 1000; // 1s

	private long lastLog = 0;
	/**
	 * CONSTRUCTOR
	 * 
	 * @param logger
	 * @param address
	 * @throws MalformedURLException
	 */
	public WeatherPredictionProviderWAMPDispatcher(IGlobalLogger logger,WeatherProviderWAMPDriver comDriver) {
		super();
		this.mapper=new ObjectMapper();
		this.globalLogger = logger;
		this.comDriver = comDriver;

		subscribeForWampUpdates();
		
		
	
	}

	/**
	 * Collect information about current weather and provide it WeatherProviderWAMPDriver.java
	 * 
	 * 
	 * @param id
	 *            Miele UID (sic!)
	 * @return
	 */

	public void subscribeForWampUpdates() {
		WampClientBuilder builder = new WampClientBuilder();
		try {
			IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
			builder.withConnectorProvider(connectorProvider)
			.withUri(url)
			.withRealm(realm)
			.withInfiniteReconnects()
			.withReconnectInterval(3, TimeUnit.SECONDS);

			client = builder.build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Subscribe on the clients status updates
		client.statusChanged()
		.observeOn(rxScheduler)
		.subscribe(
				state -> {
					System.out.println("Session status changed to " + state);
					if (state instanceof WampClient.ConnectedState) {
						// SUBSCRIBE to a topic and receive events
						onDataSubscription = client.makeSubscription(wampTopic)
								.observeOn(rxScheduler)
								.subscribe(
										ev -> {
											if (ev.arguments() == null || ev.arguments().size() < 1)
												return; // error

											JsonNode eventNode = ev.arguments().get(0);
											if (eventNode.isNull()) return;

											try {
												CurrentWeatherMap currentWeatherData = mapper.convertValue(eventNode, CurrentWeatherMap.class );
												
												synchronized (this) {
													this.weatherDetails = new CurrentWeatherDetails(
				             								comDriver.getDeviceID(),
				             								comDriver.getTimer().getUnixTime(),
				             								currentWeatherData);
				             						comDriver.receiveCurrentDetails(weatherDetails);
				             						
				             						lastLog = comDriver.getTimer().getUnixTime();
													
												}
											} catch (IllegalArgumentException e) {
												return; // error
											}
										},
										e -> {
											globalLogger.logError("failed to subscribe to topic", e);
										}, 
										() -> {
											globalLogger.logInfo("subscription ended");
										});				
					}
					else if (state instanceof WampClient.DisconnectedState) {
						if (onDataSubscription != null)
							onDataSubscription.unsubscribe();
						onDataSubscription = null;
					}
				},
				t -> {
					globalLogger.logError("Session ended with error ", t);
				},
				() -> {
					globalLogger.logInfo("Session ended normally");
				});

		client.open();
	}

	
	public void shutdown() {
		this.shutdown = true;
	}
}
