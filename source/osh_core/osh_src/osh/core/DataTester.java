package osh.core;

import java.util.TimeZone;
import java.util.UUID;

import osh.OSHComponent;
import osh.OSH;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.logging.IGlobalLogger;
import osh.core.logging.OSHGlobalLogger;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.eal.hal.HALRealTimeDriver;
import osh.registry.ComRegistry;
import osh.registry.DriverRegistry;
import osh.registry.OCRegistry;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class DataTester {
	
	private DataTester() {
		
	}

	public static void main(String[] args) throws OSHException {
		OSH osh = new OSH();
		
		SystemLoggerConfiguration systemLoggingConfiguration = new SystemLoggerConfiguration(
				"DEBUG", 
				true, //systemLoggingToConsoleActive
				true, //systemLoggingToFileActive
				false, 
				true, 
				true,
				"test");
		
		IGlobalLogger globalLogger = new OSHGlobalLogger(osh, systemLoggingConfiguration);
		osh.setLogger(globalLogger);
		
		osh.getOSHstatusObj().setIsSimulation(true);
		
		HALRealTimeDriver realTimeDriver = new HALRealTimeDriver(
				globalLogger,
				TimeZone.getTimeZone("UTC"),
				true,
				true,
				1,
				0);
		osh.setTimer(realTimeDriver);
		
		ComRegistry comRegistry = new ComRegistry(osh);
		OCRegistry ocRegistry = new OCRegistry(osh);
		DriverRegistry driverRegistry = new DriverRegistry(osh);
		
		osh.setDriverRegistry(driverRegistry);
		osh.setExternalRegistry(comRegistry);
		osh.setOCRegistry(ocRegistry);

		DataBroker dataCustodian = new DataBroker(UUID.randomUUID(), osh);
		dataCustodian.onSystemIsUp();
		
		DataTester dt = new DataTester();
		
		DataTester.driverCom dc = dt.new driverCom(osh, dataCustodian);
		DataTester.ocCom oc = dt.new ocCom(osh, dataCustodian);
		
//		dc.sendEvent();
//		oc.sendEvent();
		
		dc.sendState();
		oc.sendState();
		
		ocRegistry.flushAllQueues();
		driverRegistry.flushAllQueues();
		
		ocRegistry.flushAllQueues();
		driverRegistry.flushAllQueues();
		
	}

	
	
	
	public class driverCom extends OSHComponent implements IEventTypeReceiver, IHasState {
		
		public driverCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
			super(theOrganicSmartHome);
			((OSH) getOSH()).getDriverRegistry().register(StartDeviceRequest.class, this);
			dc.registerDataReachThroughEvent(uuid, StartDeviceRequest.class, RegistryType.OC, RegistryType.DRIVER);
			
			((OSH) getOSH()).getDriverRegistry().registerStateChangeListener(PlsStateExchange.class, this);
			dc.registerDataReachThroughState(uuid, PlsStateExchange.class, RegistryType.OC, RegistryType.DRIVER);
		}

		public void sendEvent() {
			StopDeviceRequest sdr = new StopDeviceRequest(uuid, UUID.randomUUID(), 0);
			((OSH) getOSH()).getDriverRegistry().sendEvent(StopDeviceRequest.class, sdr);
		}
		
		public void sendState() {
			EpsStateExchange pls = new EpsStateExchange(uuid, 0);
			((OSH) getOSH()).getDriverRegistry().setState(EpsStateExchange.class, this, pls);
			EpsPlsStateExchange epspls = new EpsPlsStateExchange(uuid, 0, null, null, 0, 0, 0, 0, 0, false);
			((OSH) getOSH()).getDriverRegistry().setState(EpsPlsStateExchange.class, this, epspls);
		}

		UUID uuid = UUID.randomUUID();
		
		@Override
		public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
			System.out.println("driverCom recieved Event, type: " + type);
			System.out.println("sender is: " + event.getSender() + ", this is: " + uuid);
		}

		@Override
		public Object getSyncObject() {
			return this;
		}

		@Override
		public UUID getUUID() {
			return uuid;
		}
		
	}
	
	public class ocCom extends OSHComponent implements IEventTypeReceiver, IHasState {
		
		public ocCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
			super(theOrganicSmartHome);
			((OSH) getOSH()).getOCRegistry().register(StopDeviceRequest.class, this);
			dc.registerDataReachThroughEvent(uuid, StopDeviceRequest.class, RegistryType.DRIVER, RegistryType.OC);
			
			((OSH) getOSH()).getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
			dc.registerDataReachThroughState(uuid, EpsStateExchange.class, RegistryType.DRIVER, RegistryType.OC);
		}

		public void sendEvent() {
			StartDeviceRequest sdr = new StartDeviceRequest(uuid, UUID.randomUUID(), 0);
			((OSH) getOSH()).getOCRegistry().sendEvent(StartDeviceRequest.class, sdr);
		}
		
		public void sendState() {
			PlsStateExchange pls = new PlsStateExchange(uuid, 0);
			((OSH) getOSH()).getOCRegistry().setState(PlsStateExchange.class, this, pls);
		}
		
		UUID uuid = UUID.randomUUID();

		@Override
		public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
			if (event instanceof StateChangedExchange) {
				System.out.println("ocCom recieved StateChangedExchange, type: " + ((StateChangedExchange) event).getType());
				
				if (((StateChangedExchange) event).getType().equals(EpsStateExchange.class)) {
					EpsStateExchange ex = ((OSH) getOSH()).getOCRegistry().getState(EpsStateExchange.class, ((StateChangedExchange) event).getStatefulentity());
					System.out.println("sender is: " + ex.getSender() + ", this is: " + uuid);
				}
				
				
			} else {
				System.out.println("ocCom recieved Event, type: " + type);
				System.out.println("sender is: " + event.getSender() + ", this is: " + uuid);
			}
		}

		@Override
		public Object getSyncObject() {
			return this;
		}

		@Override
		public UUID getUUID() {
			return uuid;
		}
		
	}
}
