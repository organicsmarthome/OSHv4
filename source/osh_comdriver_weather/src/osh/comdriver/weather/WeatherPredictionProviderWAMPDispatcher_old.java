package osh.comdriver.weather;


import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import osh.comdriver.WeatherProviderWAMPDriver;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.prediction.PredictedWeatherMap;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

/**
 * Handling the connection to openweathermap via wamp
 * 
 * @author Jan Mueller
 *
 */
public class WeatherPredictionProviderWAMPDispatcher_old implements Runnable {

	private IGlobalLogger logger;
	private WeatherProviderWAMPDriver comDriver;
	
	private boolean shutdown;
	
	private String url = "ws://wamp-router:8080/ws";
	private String realm = "eshl";
//	private String realm = "realm1";
	private String wampTopic = "eshl.openweathermap.v1.readout.weatherprediction";	
	
	private WampClient client;

	Subscription addProcSubscription;
	Subscription counterPublication;
	Subscription onHelloSubscription;

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
	public WeatherPredictionProviderWAMPDispatcher_old(IGlobalLogger logger,WeatherProviderWAMPDriver comDriver) {
		super();

		this.logger = logger;
		this.comDriver = comDriver;

		new Thread(this, "WeatherPredictionProviderWAMPDispatcher for WAMP").start();
	}

	/**
	 * Collect information about current weather and provide it WeatherProviderWAMPDriver.java
	 * 
	 * 
	 * @param id
	 *            Miele UID (sic!)
	 * @return
	 */



	@Override
	public void run() {

			try {
				
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
		        catch (WampError e) {
		            e.printStackTrace();
		            return;
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		            return;
		        }

		        // Subscribe on the clients status updates
		        client.statusChanged()
		              .observeOn(rxScheduler)
		              .subscribe(new Action1<WampClient.State>() {
		            @Override
		            public void call(WampClient.State t1) {
		                logger.logInfo("Session status changed to " + t1);

		                if (t1 instanceof WampClient.ConnectedState) {
		                    
		                    // SUBSCRIBE to a topic and receive events
		                    onHelloSubscription = client.makeSubscription(wampTopic, String.class)
		                                                .observeOn(rxScheduler)
		                                                .subscribe(new Action1<String>() {
		                                                                	
		                                        
		                         @Override
		                         public void call(String msg) {
		                        	 logger.logDebug("event for 'onhello' received: " + msg);
		                             
//		                             try {
		                             if (msg != null) {
		             				    
		             					// Process the JSON
		             					try {	             						
		             						ObjectMapper mapper = new ObjectMapper();
		             						PredictedWeatherMap predictedWeatherMap = mapper.readValue(msg, PredictedWeatherMap.class );
		             						WeatherPredictionDetails weatherPredictionDetails = new WeatherPredictionDetails(
		             								comDriver.getDeviceID(),
		             								comDriver.getTimer().getUnixTime(),
		             								predictedWeatherMap);
		             						comDriver.receivePredictionDetails(weatherPredictionDetails);
		             						
		             						lastLog = comDriver.getTimer().getUnixTime();
		             						
		             						
		             					} catch (IOException e) {
		             						if (logger != null) {
		             							logger.logError("failed to unmarshall current weather json", e);
		             						}
		             						else {
		             							e.printStackTrace();
		             						}          				        
		             				    }
		             				}
		             			}
		                     }
		                         , new Action1<Throwable>() {
		                         @Override
		                         public void call(Throwable e) {
		                        	 logger.logError("failed to subscribe 'onhello': " + e);
		                             e.printStackTrace();
		                         }
		                     }, new Action0() {
		                         @Override
		                         public void call() {
		                        	 logger.logInfo("'onhello' subscription ended");
		                         }
		                     });
				
		                }
		                else if (t1 instanceof WampClient.DisconnectedState) {
		                    closeSubscriptions();
		                }
		            }
		        }, new Action1<Throwable>() {
		            @Override
		            public void call(Throwable t) {
		            	logger.logError("Session ended with error " + t);
		            }
		        }, new Action0() {
		            @Override
		            public void call() {
		            	logger.logInfo("Session ended normally");
		            }
		        });

		        client.open();
				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close all subscriptions (registered events + procedures) and shut down
	 * all timers (doing event publication and calls)
	 */
	void closeSubscriptions() {
		if (onHelloSubscription != null)
			onHelloSubscription.unsubscribe();
		onHelloSubscription = null;
		if (counterPublication != null)
			counterPublication.unsubscribe();
		counterPublication = null;
		if (addProcSubscription != null)
			addProcSubscription.unsubscribe();
		addProcSubscription = null;
	}
	
	public void shutdown() {
		this.shutdown = true;
	}
}
