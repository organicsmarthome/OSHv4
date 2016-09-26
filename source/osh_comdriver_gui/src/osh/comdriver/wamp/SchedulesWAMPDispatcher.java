package osh.comdriver.wamp;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;

import osh.core.logging.IGlobalLogger;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SchedulesWAMPDispatcher {

	private IGlobalLogger logger;

//	private String url = "ws://wamp-router:8080/ws";
//	private String realm = "eshl";
	private String url = "ws://localhost:8080/ws";
	private String realm = "realm1";
	
	private WampClient client;


	// Scheduler
	ExecutorService executor = Executors.newSingleThreadExecutor();
	Scheduler rxScheduler = Schedulers.from(executor);


	/**
	 * CONSTRUCTOR
	 * 
	 * @param logger
	 * @param address
	 * @throws MalformedURLException
	 */
	public SchedulesWAMPDispatcher(IGlobalLogger logger) {
		super();

		this.logger = logger;
		
        WampClientBuilder builder = new WampClientBuilder();
        try {
        	IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
            builder.withConnectorProvider(connectorProvider)
            .withUri(url)
            .withRealm(realm)
            .withInfiniteReconnects()
            .withReconnectInterval(3, TimeUnit.SECONDS);
            
            client = builder.build();
        } catch (Exception e) {
        	e.printStackTrace();
            return;
        }
        
        client.open();
	}	
	
	public void sendSchedules(SchedulesWAMPExchangeObject sweo) {
		client.publish("eshl.optimisation.results", sweo, new TypeReference<SchedulesWAMPExchangeObject>(){})
		      .observeOn(rxScheduler)
		      .subscribe( response -> {
		    	  logger.logInfo(response);
		      }, err -> {
		    	  logger.logError("publishing schedules failed", err);
		      });
	}
}
