package osh.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.datatypes.registry.details.common.SwitchDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.energy.ElectricPowerDriverDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.SmartPlugObserverExchange;
import osh.hal.interfaces.ISwitchRequest;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;


/**
 * Generic Meter / Switch Device Driver<br>
 * <br>
 * Configure via Parameter "MeterDataUUIDs" and "SwitchDataUUIDs", which are
 * comma separated lists of UUIDs of real metering devices.<br>
 * Data from more than one device are aggregated (e.g. power of all 3-phases summed into one value)
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class SmartPlugDriver extends HALDeviceDriver implements IHasState, IEventTypeReceiver {
	
	private DeviceMetaDriverDetails deviceMetaDetails;
	private List<UUID> meterDataSources;
	private List<UUID> switchDataSources;
	private boolean generateApplianceData = false;
	
	private int incompletecounter = 0;
	
	private boolean updateElectricPowerDriverState = false;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException
	 * @throws HALException 
	 */
	public SmartPlugDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
	}

	
	/* ********************
	 * methods
	 */
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		initSmartPlug(getDriverConfig());
		
		getDriverRegistry().registerStateChangeListener(ElectricPowerDriverDetails.class, this);
		getDriverRegistry().registerStateChangeListener(SwitchDriverDetails.class, this);
	}
	
	private void initSmartPlug( OSHParameterCollection config ) throws OSHException {
		// prepare device details
		deviceMetaDetails = new DeviceMetaDriverDetails(getDeviceID(), getTimer().getUnixTime());
		deviceMetaDetails.setName(config.getParameter("name"));
		deviceMetaDetails.setLocation(config.getParameter("location"));
		if ( getDeviceType() != null ) {
			deviceMetaDetails.setDeviceType(getDeviceType());
		}
			
		if( getDeviceClassification() != null ) {
			deviceMetaDetails.setDeviceClassification(getDeviceClassification());
		}
		
		// generate appliance data from power data
		if( config.getParameter("generateappliancedata") != null &&
			config.getParameter("generateappliancedata").equals("true") ) {
			generateApplianceData = true;
		}
		
		// set data sources
		String cfgMeterSources = config.getParameter("metersources");
		if( cfgMeterSources != null )
			meterDataSources = parseUUIDArray( cfgMeterSources );
		else
			meterDataSources = Collections.emptyList();
		
		if( meterDataSources.contains( getDeviceID() ) ) {
			getGlobalLogger().logWarning("metersources can not contain own UUID! smart plug uuid: " + getDeviceID());
			updateElectricPowerDriverState = false;
		}
		
		// optional
		String cfgSwitch = config.getParameter("switch");
		if( cfgSwitch != null )
			switchDataSources = parseUUIDArray( cfgSwitch );
		else
			switchDataSources = Collections.emptyList();
		
		// set device meta details in driver registry
		getDriverRegistry().setStateOfSender(DeviceMetaDriverDetails.class, deviceMetaDetails);
		
		// set configuration details for meter and switch sources
		setDataSourcesUsed( meterDataSources );
		setDataSourcesUsed( switchDataSources );
		setDataSourcesConfigured(Collections.singleton(getDeviceID()));
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {
		if( controllerRequest instanceof ISwitchRequest ) {
			for( UUID switchUUID : switchDataSources ) {
				SwitchRequest switchReq = new SwitchRequest(controllerRequest.getDeviceID(), switchUUID, controllerRequest.getTimestamp());
				getDriverRegistry().sendCommand(SwitchRequest.class, switchReq);
			}
		}
	}
	
	public SmartPlugObserverExchange updateHALExchange() throws OSHException {
		SmartPlugObserverExchange _ox
			= new SmartPlugObserverExchange(getDeviceID(), getTimer().getUnixTime());

		// Set DeviceMetaDetails
		_ox.setDeviceMetaDetails(deviceMetaDetails);
		
		// aggregate power data
		{
			ArrayList<ElectricPowerDriverDetails> pdList = new ArrayList<ElectricPowerDriverDetails>();
			UUID meterUUID = null;
			
			for( UUID sourceUUID : meterDataSources ) {
				ElectricPowerDriverDetails p = getDriverRegistry().getState(
						ElectricPowerDriverDetails.class, sourceUUID);
				
				if( p == null ) {
					// unable to fetch state
					if (incompletecounter == 0) {
						getGlobalLogger().logWarning("incomplete data source(s) (device: " + getDeviceID() +  " meterDataSource: " + sourceUUID + ")");
					}
					incompletecounter++;
					return null;
				}
				
				pdList.add(p);
				
				if( meterUUID == null )
					meterUUID = p.getMeterUuid();
			}

			ElectricPowerDriverDetails aggregated = ElectricPowerDriverDetails.aggregatePowerDetails(getUUID(), pdList);
			_ox.setActivePower((int) Math.round(aggregated.getActivePower()));
			_ox.setReactivePower((int) Math.round(aggregated.getReactivePower()));
			
			if( updateElectricPowerDriverState ) {
				getDriverRegistry().setStateOfSender(ElectricPowerDriverDetails.class, aggregated);
			}
		}

		// aggregate switch data
		{
			int _sdCount = 0;
			boolean ambigiousState = false; // if one switch is on and another is off
			SwitchDriverDetails _sd = new SwitchDriverDetails(_ox.getDeviceID(), _ox.getTimestamp());
			
			for( UUID sourceUUID : switchDataSources ) {
				SwitchDriverDetails s = getDriverRegistry().getState(SwitchDriverDetails.class, sourceUUID);
				if( s == null ) {
					// unable to fetch state
					if (incompletecounter == 0) {
						getGlobalLogger().logWarning("incomplete data source(s) (device: " + getDeviceID() +  " switchDataSources: " + sourceUUID + ")");
					}
					incompletecounter++;
					return null;
				}
				
				if( _sdCount == 0 ) {
					_sd.setOn(s.isOn());
				} else {
					if( _sd.isOn() != s.isOn() )
						ambigiousState = true;
				}
				
				_sdCount++;
			}
			
			if( ambigiousState == false ) {
				if (_sdCount > 0) {
					_ox.setOn(_sd.isOn());
				} else {
					//no switch available (for example: house connection)
					_ox.setOn(true);
				}
			} else {
				throw new OSHException("ERROR: setting undefined state (switchDataSources: " + Arrays.toString(switchDataSources.toArray()) + ", meterDataSources: " + Arrays.toString(meterDataSources.toArray()) + ")");
			}
		}
		
		//all data is available, reset incomplete counter
		if( incompletecounter > 0 ) {
			getGlobalLogger().logWarning("data source(s) for device: " + getDeviceID() + " are available again after " + incompletecounter);
		}
		incompletecounter = 0;
		
		this.notifyObserver(_ox);
		
		// generate appliance data
		if( generateApplianceData ) {
			GenericApplianceDriverDetails appDetails = new GenericApplianceDriverDetails(getDeviceID(), _ox.getTimestamp());
			if( _ox.isOn() ) {
				if( _ox.getActivePower() > 5 )
					appDetails.setState(EN50523DeviceState.RUNNING);
				else
					appDetails.setState(EN50523DeviceState.STANDBY);
			} else {
				appDetails.setState(EN50523DeviceState.OFF);
			}
			appDetails.setStateTextDE(appDetails.getState().getDescriptionDE());
			getDriverRegistry().setState(GenericApplianceDriverDetails.class, this, appDetails);
		}
		
		return _ox;
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		if( event instanceof StateChangedExchange  && ((StateChangedExchange) event).getStatefulentity().equals(getDeviceID())) {
			if( meterDataSources.contains(((StateChangedExchange) event).getStatefulentity())
			 || switchDataSources.contains(((StateChangedExchange) event).getStatefulentity()) ) {
				try {
					updateHALExchange();
				} catch (OSHException e) {
					getGlobalLogger().logWarning(e);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
