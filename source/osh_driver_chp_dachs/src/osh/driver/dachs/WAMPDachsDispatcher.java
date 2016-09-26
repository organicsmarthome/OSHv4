package osh.driver.dachs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.WAMPDachsChpDriver;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;


/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class WAMPDachsDispatcher{

	private IGlobalLogger globalLogger;
	private WAMPDachsChpDriver dachsDriver;
	
	private String url = "ws://wamp-router:8080/ws";
	private String realm = "eshl";
	private String wampTopic = "eshl.dachs.v2.readout.information";	
	
	private WampClient client;

	// Scheduler
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Scheduler rxScheduler = Schedulers.from(executor);
	
	private DachsDriverDetails dachsDetails;
	private Subscription onDataSubscription;
	
	private ObjectMapper mapper;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public WAMPDachsDispatcher(
			IGlobalLogger globalLogger, 
			WAMPDachsChpDriver dachsDriver) {
		
		this.mapper = new ObjectMapper();
		this.globalLogger = globalLogger;
		this.dachsDriver = dachsDriver;
		
		// get information from DACHS via WAMP and save into dachsDetails
		subscribeForWampUpdates();
		
		this.dachsDetails = new DachsDriverDetails(
				dachsDriver.getDeviceID(), 
				dachsDriver.getTimer().getUnixTime());
	}

	
	public void sendPowerRequest(boolean setOn) {
		if (globalLogger != null) {
			globalLogger.logDebug("doPowerRequest()");
		}
//		List <NameValuePair> datalist = new ArrayList <NameValuePair>(); 
		String jsonInString = ""; 
		if ( setOn ) {
			jsonInString = "{\"Stromf_Ew.Anforderung_GLT.bAktiv\" : \"1\", \"Stromf_Ew.Anforderung_GLT.bAnzahlModule\" : \"1\"}";
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "1"));
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAnzahlModule", "1"));
		}
		else {
			jsonInString = "{\"Stromf_Ew.Anforderung_GLT.bAktiv\" : \"0\", \"Stromf_Ew.Anforderung_GLT.bAnzahlModule\" : \"1\"}";
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "0"));
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAnzahlModule", "1"));
		}
		
//		try {
//			jsonInString = mapper.writeValueAsString(datalist);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
		
		client.call("eshl.dachs.v1.request.powerrequest", jsonInString)
		      .observeOn(rxScheduler)
		      .subscribe( response -> {
		    	  globalLogger.logInfo(response);
		      }, err -> {
		    	  globalLogger.logError("sending command failed", err);
		      });
	}


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
												HashMap<String, String> map =
														mapper.convertValue(eventNode, new TypeReference<Map<String, String>>(){});

												synchronized (this) {
													dachsDetails.setValues(map);
													this.dachsDriver.processDachsDetails(dachsDetails);
													this.notifyAll();
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


	public DachsDriverDetails getDachsDetails(){
		return this.dachsDetails;
	}
	
}
