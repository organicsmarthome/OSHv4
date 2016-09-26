package osh.old.busdriver;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails.ConnectionStatus;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.old.busdriver.wago.LowLevelWagoByteDetails;
import osh.old.busdriver.wago.SmartPlugException;
import osh.old.busdriver.wago.Wago750860ModuleType;
import osh.old.busdriver.wago.WagoTCPUDPDispatcher;
import osh.old.busdriver.wago.WagoTCPUDPDispatcher.UpdateListener;
import osh.old.busdriver.wago.data.WagoDiData;
import osh.old.busdriver.wago.data.WagoDiGroup;
import osh.old.busdriver.wago.data.WagoDoData;
import osh.old.busdriver.wago.data.WagoDoGroup;
import osh.old.busdriver.wago.data.WagoPowerMeter;
import osh.old.busdriver.wago.data.WagoRelayData;
import osh.old.busdriver.wago.data.WagoVirtualGroup;
import osh.old.busdriver.wago.data.WagoVirtualSwitch;
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
@Deprecated
public class WagoTCPUDPBusDriver extends HALBusDriver implements UpdateListener, IEventTypeReceiver {

	private WagoTCPUDPDispatcher wagoControllerDispatcher;
	
	private long wagoControllerIdPart;
	
	private String controllerHostname;
	
	private Set<UUID> knownUUIDs;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException 
	 */
	public WagoTCPUDPBusDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) throws OSHException {
		super(controllerbox, deviceID, driverConfig);

		controllerHostname = driverConfig.getParameter("hostname");
		
		if( controllerHostname == null )
			throw new OSHException("bus driver config parameter hostname not set!");
		
		knownUUIDs = new HashSet<>();
		
		//TODO devices from variable... ?!?
	}
	
	private void connectToWagoController() throws OSHException {
		try {
			InetAddress addr = InetAddress.getByName(controllerHostname);
			
			// Port of Wago 750-860 Protocol is 9155
			wagoControllerIdPart = UUIDGenerationHelperWago.getUUIDLowerPart(addr, UUIDGenerationHelperWago.WAGO_750_860_DEFAULT_PORT);
			
			wagoControllerDispatcher = new WagoTCPUDPDispatcher(getGlobalLogger(), addr);
			wagoControllerDispatcher.registerUpdateListener(this);
		} catch (Exception e) {
			throw new OSHException(e);
		}
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		connectToWagoController();

		getDriverRegistry().register(SwitchRequest.class, this);
	}

	@Override
	public void updateDataFromBusManager(IHALExchange exchangeObject) {
		// currently NOTHING
	}

	public void checkUUID(final UUID uuid, Wago750860ModuleType type)
			throws OSHException {
		if (!knownUUIDs.contains(uuid)) {
			knownUUIDs.add(uuid);

			BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid,
					getTimer().getUnixTime(),
					wagoControllerDispatcher.isConnected() ? ConnectionStatus.ATTACHED
							: ConnectionStatus.ERROR);
			getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class,
					bs);

			getDriverRegistry().register(SwitchRequest.class,
					new IEventTypeReceiver() {
						@Override
						public Object getSyncObject() {
							return WagoTCPUDPBusDriver.this.getSyncObject();
						}

						@Override
						public <T extends EventExchange> void onQueueEventTypeReceived(
								Class<T> type, T event) throws OSHException {
							WagoTCPUDPBusDriver.this
									.onQueueEventTypeReceived(type, event);
						}

						@Override
						public UUID getUUID() {
							return uuid;
						}
					});
		}
	}
	
	@Override
	public void wagoUpdateEvent() {

		long now = getTimer().getUnixTime();
		boolean connected = wagoControllerDispatcher.isConnected(); 
		
		for ( UUID uuid : knownUUIDs ) {
			BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid, now, connected?ConnectionStatus.ATTACHED:ConnectionStatus.ERROR);
			getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
		}
		
		// set metering data
		for (WagoPowerMeter meterData : wagoControllerDispatcher.getPowerData()) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart(
									Wago750860ModuleType.METER,
									(short) meterData.getGroupId(), 
									(short) meterData.getMeterId() ),
					wagoControllerIdPart );
		
			try {
				checkUUID(uuid, Wago750860ModuleType.METER);
			} catch (OSHException e) {
				e.printStackTrace();
			}
		}
		
		// set switch data
		for( WagoRelayData relay : wagoControllerDispatcher.getSwitchData() ) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.SWITCH,
					(short)relay.getId(), (short) 0 ),
					wagoControllerIdPart );
			
			try {
				checkUUID(uuid, Wago750860ModuleType.SWITCH);
			} catch (OSHException e) {
				e.printStackTrace();
			}			
		}

		// set virtual switch data
		for( WagoVirtualSwitch vswitch : wagoControllerDispatcher.getVirtualSwitchData() ) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.VIRTUALSWITCH,
					(short)vswitch.getGroupId(), (short)vswitch.getId() ),
					wagoControllerIdPart );
			
			try {
				checkUUID(uuid, Wago750860ModuleType.VIRTUALSWITCH);
			} catch (OSHException e) {
				e.printStackTrace();
			}
		}
		
		// set vs data group
		for( WagoVirtualGroup vsg : wagoControllerDispatcher.getVirtualSwitchGroupData()) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.VIRTUALSWITCH,
					(short)vsg.getGroupId(), (short) UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID ),
					wagoControllerIdPart );

			try {
				checkUUID(uuid, Wago750860ModuleType.VIRTUALSWITCH);
			} catch (OSHException e) {
				e.printStackTrace();
			}

			LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, getTimer().getUnixTime());

			llDetail.setData(vsg.getByte());

			getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
		}

		// set digital in data
		for( WagoDiData di : wagoControllerDispatcher.getDigitalInData()) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.DIGITALINPUT,
					(short)di.getGroupId(), (short) di.getId() ),
					wagoControllerIdPart );

			try {
				checkUUID(uuid, Wago750860ModuleType.DIGITALINPUT);
			} catch (OSHException e) {
				e.printStackTrace();
			}
		}

		// set digital in data group
		for( WagoDiGroup dig : wagoControllerDispatcher.getDigitalInGroup()) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.DIGITALINPUT,
					(short)dig.getGroupId(), (short) UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID ),
					wagoControllerIdPart );

			try {
				checkUUID(uuid, Wago750860ModuleType.DIGITALINPUT);
			} catch (OSHException e) {
				e.printStackTrace();
			}

			LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, getTimer().getUnixTime());

			llDetail.setData(dig.getByte());

			getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
		}
		
		
		// set digital out data
		for( WagoDoData do8 : wagoControllerDispatcher.getDigitalOutData() ) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.DIGITALOUTPUT,
					(short)do8.getGroupId(), (short)do8.getId() ),
					wagoControllerIdPart );
			
			try {
				checkUUID(uuid, Wago750860ModuleType.DIGITALOUTPUT);
			} catch (OSHException e) {
				e.printStackTrace();
			}
		}
		
		// set do8 data group
		for( WagoDoGroup vsg : wagoControllerDispatcher.getDigitalOutGroup()) {
			UUID uuid = new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart( Wago750860ModuleType.DIGITALOUTPUT,
					(short)vsg.getGroupId(), (short) UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID ),
					wagoControllerIdPart );

			try {
				checkUUID(uuid, Wago750860ModuleType.DIGITALOUTPUT);
			} catch (OSHException e) {
				e.printStackTrace();
			}

			LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, getTimer().getUnixTime());

			llDetail.setData(vsg.getByte());

			getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
		}

		// TODO: handle analog input, digital output, etc...
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		
		if (event instanceof SwitchRequest) {
			SwitchRequest switchreq = (SwitchRequest) event;
			
			// sanity checks...
			UUID targetId = switchreq.getReceiver();
			if( targetId.getLeastSignificantBits() != wagoControllerIdPart )
				throw new OSHException( "received command with wrong controller id" );
			
			// extract wago target ids
			long higherPart = targetId.getMostSignificantBits();
			short moduleId = (short) ((higherPart >> 16) & 0xffff);
			short portId = (short) ((higherPart >> 0) & 0xffff);
			byte moduleType = (byte) ((higherPart >> 32) & 0xff);
			int uuidPrefix = (int) ((higherPart >> 32) & 0xffffff00);

			// further sanity checks
			if( portId > 7 || (portId != 0 && moduleType == Wago750860ModuleType.SWITCH.value()))
				throw new OSHException( "received command with wrong port id" );
			if( moduleType != Wago750860ModuleType.SWITCH.value() &&
				moduleType != Wago750860ModuleType.VIRTUALSWITCH.value() &&
				moduleType != Wago750860ModuleType.DIGITALOUTPUT.value())
				throw new OSHException( "received command with wrong module type" );
			if( uuidPrefix != UUIDGenerationHelperWago.WAGO_750_860_UUID_PREFIX )
				throw new OSHException( "received command with invalid uuid" );
			
			try {
				if( moduleType == Wago750860ModuleType.SWITCH.value() )
					wagoControllerDispatcher.setSwitch(moduleId, switchreq.getTurnOn());
				else if ( moduleType == Wago750860ModuleType.VIRTUALSWITCH.value() )
					wagoControllerDispatcher.setVirtualSwitch(moduleId, portId, switchreq.getTurnOn());
				else if ( moduleType == Wago750860ModuleType.DIGITALOUTPUT.value() )
					wagoControllerDispatcher.setDigitalOutput(moduleId, portId, switchreq.getTurnOn());
			} catch (SmartPlugException e) {
				// pass on exception
				throw new OSHException(e);
			}
		}
	}

	@Override
	public UUID getUUID() {
		// construct own UUID
		return new UUID( UUIDGenerationHelperWago.getWago750860UUIDHigherPart(
				Wago750860ModuleType.CONTROLLER, (short) 0, (short) 0), wagoControllerIdPart);
	}

}
