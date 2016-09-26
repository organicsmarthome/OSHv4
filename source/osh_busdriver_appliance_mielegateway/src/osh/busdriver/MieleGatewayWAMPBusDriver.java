package osh.busdriver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import osh.busdriver.mielegateway.MieleGatewayWAMPDispatcher;
import osh.busdriver.mielegateway.data.MieleApplianceRawDataJSON;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataJSON;
import osh.busdriver.mielegateway.data.MieleDuration;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails.ConnectionStatus;
import osh.datatypes.registry.details.common.StartTimeDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.en50523.EN50523DeviceState;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.utils.uuid.UUIDGenerationHelperMiele;

/**
 * BusDriver for Miele Homebus Gateway at KIT
 * 
 * @author Kaibin Bao, Ingo Mauser
 */
public class MieleGatewayWAMPBusDriver extends HALBusDriver implements Runnable {
	
	private String mieleGatewayHost;
	private MieleGatewayWAMPDispatcher mieleGatewayDispatcher = null;
	
	private InetAddress mieleGatewayAddr;

	private Map<UUID, Integer> deviceIds;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws UnknownHostException 
	 */
	public MieleGatewayWAMPBusDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) throws UnknownHostException {
		super(controllerbox, deviceID, driverConfig);
		
		this.mieleGatewayHost = driverConfig.getParameter("mielegatewayhost");
		this.mieleGatewayAddr = InetAddress.getByName(mieleGatewayHost);
		
		this.deviceIds = new HashMap<>();
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		// create dispatcher for
		// connection to Miele gateway
		this.mieleGatewayDispatcher = new MieleGatewayWAMPDispatcher(getGlobalLogger());
		
		new Thread(this, "push proxy of Miele bus driver to WAMP").start();
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (this.mieleGatewayDispatcher) {
				try { // wait for new data
					this.mieleGatewayDispatcher.wait();
				} 
				catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}
				
				long timestamp = getTimer().getUnixTime();
				
				if ( mieleGatewayDispatcher.getDeviceData().isEmpty() ) { // an error has occurred
					for( UUID uuid : deviceIds.keySet() ) {
						BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid, timestamp, ConnectionStatus.ERROR);
						getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
					}
				}
				
				for (MieleDeviceHomeBusDataJSON dev : mieleGatewayDispatcher.getDeviceData()) {
					// build UUID
					long uuidHigh = UUIDGenerationHelperMiele.getMieleUUIDHigherPart( dev.getUid() );
					long uuidLow;
					try {
						uuidLow = UUIDGenerationHelperMiele.getHomeApplianceUUIDLowerPart((short)dev.getDeviceClass(), mieleGatewayAddr);
					} 
					catch (Exception e) {
						getGlobalLogger().logError("should not happen: UUID generation failed", e);
						continue;
					}
					final UUID devUUID = new UUID(uuidHigh, uuidLow);
					
					MieleGatewayWAMPBusDriver driver = this;
					
					// register UUID as command receiver to the registry
					if( !deviceIds.containsKey(devUUID) ) { // device already known?
						IEventTypeReceiver eventReceiver = new IEventTypeReceiver() {
							@Override
							public Object getSyncObject() {
								return MieleGatewayWAMPBusDriver.this;
							}
							
							@Override
							public UUID getUUID() {
								return devUUID;
							}

							@Override
							public <T extends EventExchange> void onQueueEventTypeReceived(
									Class<T> type, T event) throws OSHException {
//								this.onQueueEventTypeReceived(type, event);
								driver.onQueueEventReceived(event, devUUID);
							}
						};
						
						// register device
						try {
							getDriverRegistry().register(StartDeviceRequest.class, eventReceiver);
							getDriverRegistry().register(StopDeviceRequest.class, eventReceiver);
							getDriverRegistry().register(SwitchRequest.class, eventReceiver);
							getDriverRegistry().registerStateChangeListener(ExpectedStartTimeExchange.class, eventReceiver);
							deviceIds.put(devUUID, dev.getUid());
						} catch (OSHException e) {
							// nop. happens.
							getGlobalLogger().logError("should not happen", e);
						}
					}
				}

				for ( Entry<UUID, Integer> ent : deviceIds.entrySet() ) {
					final UUID devUUID = ent.getKey();
					final MieleDeviceHomeBusDataJSON dev = mieleGatewayDispatcher.getDeviceData(ent.getValue());

					// check if device is published by gateway at the moment
					if( dev == null ) {
						BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
						getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
						continue;
					}
				
					// check if all data is available
					if( dev.getDeviceDetails() == null ) { 
						BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
						getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
						continue;
					} 
					else {
						BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ATTACHED);
						getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
					}
					
					// create program details
					GenericApplianceProgramDriverDetails programdetails = new GenericApplianceProgramDriverDetails(devUUID, timestamp);
					programdetails.setLoadProfiles(null);
					programdetails.setProgramName(dev.getDeviceDetails().getProgramName());
					programdetails.setPhaseName(dev.getDeviceDetails().getPhaseName());
					
					// create Miele specific details
					  // duration
					MieleApplianceDriverDetails mieledetails = new MieleApplianceDriverDetails(devUUID, timestamp);
					if( dev.getDuration() != null )
						mieledetails.setExpectedProgramDuration(dev.getDuration().duration()*60);
					else
						mieledetails.setExpectedProgramDuration(-1);

					  // remaining time
					if( dev.getRemainingTime() != null )
						mieledetails.setProgramRemainingTime(dev.getRemainingTime().duration()*60);
					else
						mieledetails.setProgramRemainingTime(-1);
					
					  // start time
					if( dev.getStartTime() != null ) {
						Calendar cal = Calendar.getInstance();
						long nowInMillies = getTimer().getUnixTime() * 1000L;
						
						cal.setTimeInMillis(nowInMillies);
						
						cal.setTimeZone(getTimer().getHostTimeZone());
						
						cal.set(Calendar.HOUR_OF_DAY, dev.getStartTime().hour());
						cal.set(Calendar.MINUTE, dev.getStartTime().minute());
						cal.set(Calendar.SECOND, 0);

						if( cal.getTimeInMillis() <= nowInMillies )
							cal.add(Calendar.DAY_OF_YEAR, 1);
					
						mieledetails.setStartTime( cal.getTimeInMillis() / 1000L );
					} else
						mieledetails.setStartTime(-1);

					
					// set state of the UUID
					try {
						getDriverRegistry().setStateOfSender(
								GenericApplianceDriverDetails.class,
								createApplianceDetails(devUUID, timestamp, dev));
						getDriverRegistry().setStateOfSender(
								StartTimeDetails.class,
								createStartTimeDetails(devUUID, timestamp, dev));
						getDriverRegistry().setStateOfSender(
								GenericApplianceProgramDriverDetails.class,
								programdetails);
						getDriverRegistry().setStateOfSender(
								MieleApplianceDriverDetails.class,
								mieledetails);
						
					} 
					catch (OSHException e1) {
						BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
						getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
						getGlobalLogger().logError(e1);
					}
				}
			}
		}		
	}


	@Override
	public void updateDataFromBusManager(IHALExchange exchangeObject) {
		// NOTHING
	}

	static private StartTimeDetails createStartTimeDetails(
			final UUID devUUID, 
			long timestamp,
			MieleDeviceHomeBusDataJSON dev) {
		
		StartTimeDetails startDetails = new StartTimeDetails(devUUID, timestamp);
		startDetails.setStartTime(-1);

		if ( dev.getDeviceDetails() != null ) {
			MieleDuration mieleStartTime = dev.getDeviceDetails().getStartTime();
			
			if ( mieleStartTime != null ) {
				int starttime = mieleStartTime.duration();
				
				if ( starttime >= 0 ) {
					Calendar calNow = Calendar.getInstance();
					Calendar calStartTime = (Calendar) calNow.clone();
					
					calStartTime.set(Calendar.MINUTE, starttime % 60);
					calStartTime.set(Calendar.HOUR_OF_DAY, starttime / 60);
					
					if ( calStartTime.before(calNow) ) {
						calStartTime.add(Calendar.DAY_OF_YEAR, 1);
					}
					
					startDetails.setStartTime(calStartTime.getTimeInMillis()/1000L);
				}
			}
		}
		
		return startDetails;
	}
	
	static private GenericApplianceDriverDetails createApplianceDetails( 
			UUID uuid, 
			long timestamp, 
			MieleDeviceHomeBusDataJSON dev ) throws OSHException {
		
		GenericApplianceDriverDetails details = new GenericApplianceDriverDetails(uuid, timestamp); 
		MieleApplianceRawDataJSON devDetails = dev.getDeviceDetails();
		
		if ( devDetails == null ) {
			throw new OSHException("can't get device details");
		}
		
		if ((dev.getState() == EN50523DeviceState.PROGRAMMED || dev.getState() == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART)
				&& (dev.getActions() == null || !dev.getActions().contains("start"))) {
			details.setState(EN50523DeviceState.STANDBY);
		}
		else {
			details.setState(dev.getState());
		}
		
		return details;
	}

	public void onQueueEventReceived(EventExchange event, UUID deviceUUID) throws OSHException {
		if ( event instanceof CommandExchange ) {
			UUID devUUID = ((CommandExchange) event).getReceiver();
			Integer uid = deviceIds.get(devUUID);
			
			if ( uid != null ) { // known device?
				if ( event instanceof StartDeviceRequest ) {
					mieleGatewayDispatcher.sendCommand("start", uid);
				} else if ( event instanceof StopDeviceRequest ) {
					mieleGatewayDispatcher.sendCommand("stop", uid);
				} else if ( event instanceof SwitchRequest ) {
					if( ((SwitchRequest) event).isTurnOn() ) {
						mieleGatewayDispatcher.sendCommand("lighton", uid);
					} else {
						mieleGatewayDispatcher.sendCommand("lightoff", uid);
					}
				}
			}
		} else if (event instanceof StateChangedExchange) {
			if (deviceUUID.equals(((StateChangedExchange) event).getStatefulentity())) {
				if (((StateChangedExchange) event).getType().equals(ExpectedStartTimeExchange.class)) {
					
					ExpectedStartTimeExchange este = getDriverRegistry().getState(ExpectedStartTimeExchange.class, ((StateChangedExchange) event).getStatefulentity());
					UUID devUUID = este.getSender();
					Integer uid = deviceIds.get(devUUID);			
					mieleGatewayDispatcher.sendStarttimes(este.getExpectedStartTime(), uid);				
				}
			}
		}
	}

	/* CURRENTLY NOT IN USE (BUT KEEP IT!)
	private final int MIELE_GW_UID_HOMEBUS = 0x48425553; // "HBUS" for HOMEBUS
	private final short MIELE_GW_APPLIANCE_TYPE = 0x4757; // "GW"
	public UUID getUUID() {
		long uuidHigh = getUUIDHigherPart(MIELE_GW_UID_HOMEBUS, MIELE_BRAND_AND_MANUFACTURER_ID, MIELE_BRAND_AND_MANUFACTURER_ID);
		long uuidLow;
		try {
			uuidLow = getUUIDLowerPart(MIELE_GW_APPLIANCE_TYPE, mieleGatewayAddr);
		} catch (ControllerBoxException e) {
			getGlobalLogger().logError("should not happen", e);
			return null;
		}
		
		return new UUID( uuidHigh, uuidLow );
	}
	*/
	
}
