package osh.driver;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.miele.DeviceProfile;
import osh.configuration.appliance.miele.ProfileTick;
import osh.configuration.system.DeviceTypes;
import osh.core.RegistryType;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.PowerProfileTick;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.hal.exchange.GenericApplianceDofObserverExchange;
import osh.hal.exchange.GenericApplianceStarttimesControllerExchange;
import osh.hal.exchange.MieleApplianceControllerExchange;
import osh.hal.exchange.MieleApplianceObserverExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.xml.XMLSerialization;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class MieleApplianceDriver 
					extends HALDeviceDriver 
					implements IEventTypeReceiver, IHasState {
	
	// uuid of this appliance as posted by the bus driver
	private UUID applianceBusDriverUUID;

	// driverData
	private DeviceProfile deviceProfile;
	private EnumMap<Commodity, ArrayList<PowerProfileTick>> currentLoadProfiles;
	private long programStartedTime = -1;
	
	//temporal degree of freedom
	private int firstDof = 0;
	private int secondDof = 0;
	
	// pending command
	private EN50523OIDExecutionOfACommandCommands pendingCommand = null;
	
	//successive incomplete errors count
	private int incompletedata = 0;
	
	// stored data from bus drivers
	private GenericApplianceDriverDetails currentAppDetails;
	private GenericApplianceProgramDriverDetails appProgramDetails;
	private MieleApplianceDriverDetails mieleApplianceDriverDetails;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	
	/**
	 * CONSTRUCTOR
	 */
	public MieleApplianceDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws HALException {
		super(controllerbox, deviceID, driverConfig);
		
		String cfgApplianceUUID = driverConfig.getParameter("applianceuuid");
		if( cfgApplianceUUID == null ) {
			throw new HALException("Need config parameter applianceuuid");
		}
		this.applianceBusDriverUUID = UUID.fromString(cfgApplianceUUID);
		
		try {
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}
		
		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		setDataSourcesUsed( this.getMeterUuids() );
		setDataSourcesConfigured(Collections.singleton(applianceBusDriverUUID));

		// at the moment we have only one Power Profile
		this.loadDeviceProfiles();
		this.currentLoadProfiles = this.generatePowerProfiles();

		// register for changes of different details...
		getDriverRegistry().registerStateChangeListener(GenericApplianceDriverDetails.class, this);
		getDriverRegistry().registerStateChangeListener(GenericApplianceProgramDriverDetails.class, this);
		getDriverRegistry().registerStateChangeListener(DofStateExchange.class, this);
		
		//DofStateExchange will only be published to the com registry, we need a data reach through for this
		getOSH().getDataBroker().registerDataReachThroughState(
				getUUID(), 
				DofStateExchange.class, 
				RegistryType.COM, 
				RegistryType.DRIVER);
		
		getTimer().registerComponent(
				new IRealTimeSubscriber() {
					@Override
					public Object getSyncObject() {
						return MieleApplianceDriver.this;
					}
					
					@Override
					public void onNextTimePeriod() throws OSHException {
						synchronized (this) {
							if( pendingCommand == EN50523OIDExecutionOfACommandCommands.START ) {
								StartDeviceRequest req = new StartDeviceRequest(
										getDeviceID(), 
										applianceBusDriverUUID, 
										getTimer().getUnixTime());
								getDriverRegistry().sendCommand(StartDeviceRequest.class, req);
							}
						}
					}
				}, 
				1
			);
		
		StaticCompressionExchange stat = new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime());
		stat.setCompressionType(compressionType);
		stat.setCompressionValue(compressionValue);
		this.notifyObserver(stat);
	}


	private ArrayList<PowerProfileTick> shrinkPowerProfile(
			Commodity commodity,
			List<PowerProfileTick> powerProfile, 
			int programDuration){
		ArrayList<PowerProfileTick> _tmpList = new ArrayList<PowerProfileTick>();

		//if it's greater => shrink it!
		if (powerProfile.size() >= programDuration) {
			for( int i = 0; i < programDuration; i++){
				_tmpList.add(powerProfile.get(i));
			}
		}
		else {
			_tmpList.addAll(powerProfile);
			//expand it
			for (int i = 0; i < (programDuration-powerProfile.size()); i++){
				_tmpList.add(powerProfile.get(powerProfile.size()-1));
			}
		}
		
		return _tmpList;
	}
	
	
	private EnumMap<Commodity, ArrayList<PowerProfileTick>> generatePowerProfiles() {
		
		EnumMap<Commodity, ArrayList<PowerProfileTick>> profiles = new EnumMap<>(Commodity.class);
		
		int count = 0;
		
		// iterate time ticks
		for ( ProfileTick profileTick : deviceProfile.getProfileTicks().getProfileTick() ) {
			
			// iterate commodities
			for (int i = 0; i < profileTick.getLoad().size(); i++) {
				
				Commodity currentCommodity = Commodity.fromString(profileTick.getLoad().get(i).getCommodity());
				
				ArrayList<PowerProfileTick> _pwrProfileList = profiles.get(currentCommodity);
				
				if ( _pwrProfileList == null ) {
					_pwrProfileList = new ArrayList<PowerProfileTick>();
					profiles.put(currentCommodity, _pwrProfileList);
				}
				
				PowerProfileTick _pwrPro = new PowerProfileTick();
				_pwrPro.commodity = currentCommodity;
				_pwrPro.timeTick = count;
				_pwrPro.load = profileTick.getLoad().get(i).getValue();
				_pwrProfileList.add(_pwrPro);
			}
			
			++count;
		}
		
		return profiles;
	}
	
	private void loadDeviceProfiles() throws OSHException{
		String profileSourceName = getDriverConfig().getParameter("profilesource");
		//load profiles
		try {
			this.deviceProfile = (DeviceProfile)XMLSerialization.file2Unmarshal(profileSourceName, DeviceProfile.class);
		} 
		catch (FileNotFoundException e) {
			throw new OSHException(e);
		} 
		catch (JAXBException e) {
			throw new OSHException(e);
		}
	}
	
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {

		try {
			if (controllerRequest instanceof MieleApplianceControllerExchange) {			
				MieleApplianceControllerExchange controllerExchange = (MieleApplianceControllerExchange) controllerRequest;
	
				if ( controllerExchange.getApplianceCommand() == EN50523OIDExecutionOfACommandCommands.START ) {
					StartDeviceRequest req = new StartDeviceRequest(getDeviceID(), applianceBusDriverUUID, controllerRequest.getTimestamp());
					getDriverRegistry().sendCommand(StartDeviceRequest.class, req);
					this.pendingCommand = EN50523OIDExecutionOfACommandCommands.START;
				}
				if ( controllerExchange.getApplianceCommand() == EN50523OIDExecutionOfACommandCommands.STOP ) {
					StopDeviceRequest req = new StopDeviceRequest(getDeviceID(), applianceBusDriverUUID, controllerRequest.getTimestamp());
					getDriverRegistry().sendCommand(StopDeviceRequest.class, req);
				}
			} else if (controllerRequest instanceof GenericApplianceStarttimesControllerExchange) {
				GenericApplianceStarttimesControllerExchange gasce = (GenericApplianceStarttimesControllerExchange) controllerRequest;
				ExpectedStartTimeExchange este = new ExpectedStartTimeExchange(applianceBusDriverUUID, gasce.getStartTime());
				este.setExpectedStartTime(gasce.getStartTime());
				getDriverRegistry().setStateOfSender(ExpectedStartTimeExchange.class, este);
			}

		} 
		catch (Exception reqEx) {
			getGlobalLogger().logError("Request to Miele Gateway failed!", reqEx);
		}
	}


	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		
		// our device? then: build observer exchange
		if ( event instanceof StateChangedExchange  && ((StateChangedExchange) event).getStatefulentity().equals(getDeviceID())) {
			boolean updateOx = false;
			
			if (((StateChangedExchange) event).getType().equals(DofStateExchange.class)) {
				//making sure dof is only set for 'really' controllable devices
				if (this.getDeviceType() == DeviceTypes.WASHINGMACHINE
						|| this.getDeviceType() == DeviceTypes.DISHWASHER 
						|| this.getDeviceType() == DeviceTypes.DRYER ) {
					DofStateExchange dse = getDriverRegistry().getState(DofStateExchange.class, getUUID());
					this.firstDof = dse.getDevice1stDegreeOfFreedom();
					this.secondDof = dse.getDevice2ndDegreeOfFreedom();
					//sanity
					if (firstDof < 0 || secondDof < 0) {
						this.getGlobalLogger().logError("Recieved illegal dof, not sending to o/c");
					} else {
						GenericApplianceDofObserverExchange gadoe = new GenericApplianceDofObserverExchange(getDeviceID(),
								getTimer().getUnixTime());
						gadoe.setDevice1stDegreeOfFreedom(this.firstDof);
						gadoe.setDevice1stDegreeOfFreedom(this.secondDof);
						this.notifyObserver(gadoe);
					}

				}
				
			} else {
				// consider only if the own state changed
				// (changed by meter device or BusDriver)
				UUID entity = ((StateChangedExchange) event).getStatefulentity();
				
				if ( applianceBusDriverUUID.equals(entity) ) {
					// get appliance details from registry
					currentAppDetails = getDriverRegistry().getState(
							GenericApplianceDriverDetails.class, applianceBusDriverUUID);
					
					// get appliance program details from registry
					appProgramDetails  = getDriverRegistry().getState(
							GenericApplianceProgramDriverDetails.class, applianceBusDriverUUID);

					// get miele program details from registry
					mieleApplianceDriverDetails = getDriverRegistry().getState(
							MieleApplianceDriverDetails.class, applianceBusDriverUUID);

					updateOx = true;
				}
				
				// EN50523 state
				
				// update meter data
				if ( this.getMeterUuids().contains(entity) ) {
					updateOx = true;
				}
			}
			
			

			// generate ox object
			if( updateOx ) {
				MieleApplianceObserverExchange _ox = new MieleApplianceObserverExchange(
						getDeviceID(), getTimer().getUnixTime());
	
				// check for incomplete data
				if (currentAppDetails == null) {
					if( incompletedata == 0 )
						getGlobalLogger().logWarning("appDetails not available. Wait for data... UUID: " + getUUID());
					incompletedata++;
					return;
				}
				if (appProgramDetails == null) {
					if( incompletedata == 0 )
						getGlobalLogger().logWarning("appProgramDetails not available. Wait for data... UUID: " + getUUID());
					incompletedata++;
					return;
				}
				
				if (mieleApplianceDriverDetails == null) {
					if( incompletedata == 0 )
						getGlobalLogger().logWarning("mieleApplianceDriverDetails not available. Wait for data... UUID: " + getUUID());
					incompletedata++;
					return;
				}

				
				_ox.setEn50523DeviceState(currentAppDetails.getState());			
				_ox.setProgramName(appProgramDetails.getProgramName());
				_ox.setPhaseName(appProgramDetails.getPhaseName());
				//don't get Power Profile from Program Details, use stuff from file.
				
				// calculate profile
				if( currentAppDetails.getState() != null && mieleApplianceDriverDetails != null) {
					switch (currentAppDetails.getState()) {
					case PROGRAMMEDWAITINGTOSTART:
					case PROGRAMMED: {
						long maxProgramDuration = mieleApplianceDriverDetails.getExpectedProgramDuration();
						
						// Miele Gateway needs some time before it delivers the correct information about program duration
						if( maxProgramDuration <= 0 )
							return;
						
 						EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles = new EnumMap<>(Commodity.class);
						
						for ( Entry<Commodity, ArrayList<PowerProfileTick>> e : currentLoadProfiles.entrySet() ) {
							ArrayList<PowerProfileTick> expectedPowerProfile = shrinkPowerProfile(e.getKey(), e.getValue(), (int)maxProgramDuration);
							expectedLoadProfiles.put(e.getKey(), expectedPowerProfile);
						}
						
						_ox.setExpectedLoadProfiles(expectedLoadProfiles);
						_ox.setDeviceStartTime(mieleApplianceDriverDetails.getStartTime());
						
						programStartedTime = -1;
						
						} break;
					case RUNNING: {
						synchronized (this) { // reset pending command
							if ( pendingCommand == EN50523OIDExecutionOfACommandCommands.START ) {
								pendingCommand = null;
							}
						}
						if( programStartedTime == -1 )
							programStartedTime = getTimer().getUnixTime();
						
						long remainingProgramDuration;
						if (isControllable()) {
							remainingProgramDuration = mieleApplianceDriverDetails.getProgramRemainingTime();
							long now = getTimer().getUnixTime();
							if (remainingProgramDuration == -1 && programStartedTime <= now) { // IMA @2016-05-20: FIX for hob/oven are "Controllable"
								remainingProgramDuration = currentLoadProfiles.get(Commodity.ACTIVEPOWER).size() - (now - programStartedTime);
							}
						}
						else {
							remainingProgramDuration = currentLoadProfiles.get(Commodity.ACTIVEPOWER).size() - (getTimer().getUnixTime() - programStartedTime);
						}
							
						long finishedProgramDuration = getTimer().getUnixTime() - programStartedTime;
						
						EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles = new EnumMap<>(Commodity.class);
						
						if( remainingProgramDuration > 0 ) { // only makes sense if gateway doesn't provide this information
							for ( Entry<Commodity, ArrayList<PowerProfileTick>> e : currentLoadProfiles.entrySet() ) {
								ArrayList<PowerProfileTick> expectedPowerProfile = shrinkPowerProfile(e.getKey(), e.getValue(), (int) (remainingProgramDuration+finishedProgramDuration));
								expectedLoadProfiles.put(e.getKey(), expectedPowerProfile);
							}
						}
						
//						_ox.setProgramRemainingTime(remainingTime);
						_ox.setExpectedLoadProfiles(expectedLoadProfiles);
						_ox.setDeviceStartTime(mieleApplianceDriverDetails.getStartTime());
						
						} break;
					default: {
						programStartedTime = -1;
						} break;
					}
				}
				
				// meta details
				_ox.setName(getName());
				_ox.setLocation(getLocation());
				_ox.setDeviceType(getDeviceType());
				_ox.setDeviceClass(getDeviceClassification());
				_ox.setConfigured(true);
				
				//all data available -> reset error counter
				if ( incompletedata > 0 ) {
					getGlobalLogger().logWarning("data source(s) for device: " + getDeviceID() + " are available again after " + incompletedata + " missing");
				}
				incompletedata = 0;
				
				this.notifyObserver(_ox);
			} /* if updateOx */
			
		} /* if( event instanceof StateChangedExchange ) */
		
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
