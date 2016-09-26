package osh.driver;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.XsdApplianceProgramConfigurations;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.driver.appliance.generic.XsdLoadProfilesHelperTool;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.en50523.EN50523DeviceState;
import osh.en50523.EN50523DeviceStateRemoteControl;
import osh.hal.exchange.FutureApplianceControllerExchange;
import osh.hal.exchange.FutureApplianceObserverExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class GenericApplianceDriver 
							extends ApplianceDriver {
	
	// ### Variables for LoadProfile ###
	
	/** Compression type of the load profile */
	protected LoadProfileCompressionTypes loadProfileType = LoadProfileCompressionTypes.DISCONTINUITIES;
	protected int powerEps = 100;
	protected int timeSlotDurationEps = 1;
	
	// ### Variables for DeviceState ###
	
	/** DIN EN 50523 DeviceState (OFF, RUNNING, ...) */
	private EN50523DeviceState currentEn50523State = EN50523DeviceState.UNKNOWN;
	
	/** DIN EN 50523 Remote Control State (DISABLED, TEMPORARILY_DISABLED, ENABLED) */
	private EN50523DeviceStateRemoteControl currentEn50523RemoteControlState = EN50523DeviceStateRemoteControl.DISABLED;
	
	// ### Variables for Configurations (= program + extras + loadProfiles) ###
	
	/** All Configurations (= program + extras + loadProfiles) */
	protected XsdApplianceProgramConfigurations applianceConfigurations;
	
	
	/** Expected ending time received from appliance */
	protected Long expectedEndingTimeReceivedFromAppliance = null;
	
	/** Expected finish time received from appliance */
	protected Long expectedFinishTimeReceivedFromAppliance = null;
		
	// ### Variables for current Configuration ###
	
	/** Device Configuration which is now active: Selected by user on device */
	protected Integer selectedConfigurationID = null;
	
	/** StartingTime of Active Configuration Profile (ACP) */
	protected Long configurationStartedAt = null;
	
	/** StartingTime of Active Phase in Active Configuration Profile */
	protected Long phaseStartedAt = null;
	
	// ### PRIVATE variables for exclusive usage in this class, NOT in subclasses ###
	// result of optimization
	
	/** in case of eDoF: result of optimization */
	private Integer selectedProfileID = null;
	
	/** in case of tDoF: result of optimization  */
	private long[] selectedStartingTimes;
	
	/**
	 * Active Configuration Profile (ACP)<br>
	 * contains:<br> 
	 * dynamicLoadProfiles: remaining dynamic load profiles with tDoF or eDoF (using relative times to 0)<br>
	 */
	private ApplianceProgramConfigurationStatus applianceConfigurationProfile = null;
	
	/** indicates whether ACP has changed */
	private boolean acpChanged = false;
	
	// ###
	
	/** indicator whether something important changed and the OX has to be resent */
	private boolean updateOx = false;

	
	/**
	 * CONSTRUCTOR
	 */
	public GenericApplianceDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, JAXBException, SAXException,
			HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// load some variables (e.g. initialize variables)
		
		// IMPORTANT:
		// if (getDeviceType() == DeviceTypes.WASHINGMACHINE) <-- does NOT work!
		
		// get Configurations file
		{
			String configurationsFile = driverConfig.getParameter("profilesource");
			if (configurationsFile != null) {
				JAXBContext jaxbWMParameters = JAXBContext.newInstance("osh.configuration.appliance");
				Unmarshaller unmarshallerConfigurations = jaxbWMParameters.createUnmarshaller();
				Object unmarshalledConfigurations = unmarshallerConfigurations.unmarshal(new File(configurationsFile));
				if (unmarshalledConfigurations instanceof XsdApplianceProgramConfigurations) {
					this.applianceConfigurations = (XsdApplianceProgramConfigurations) unmarshalledConfigurations;
				}
				else {
					throw new HALException("No valid configurations file found!");
				}
			}
			else {
				throw new HALException("Appliance configurations are missing!");
			}
		}
		
		// default: UNKNOWN
		this.currentEn50523State = EN50523DeviceState.UNKNOWN;
		
		// default: remote disabled (well...it's reality...)
		this.currentEn50523RemoteControlState = EN50523DeviceStateRemoteControl.DISABLED;
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		// update status etc every second
		getTimer().registerComponent(this, 1);
		
		long now = getTimer().getUnixTime();
		
		// set initial device state
		updateGenericApplianceDriverDetails(now);
		updateGenericApplianceProgramDriverDetails(now);
		
	}
	
	
	@Override
	synchronized public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		// get current time
		long now = getTimer().getUnixTime();
		
		// if not OFF -> device logic for running etc
		if (currentEn50523State == EN50523DeviceState.OFF) {
			deviceStateIsOFF();
		}
		else  if (currentEn50523State == EN50523DeviceState.STANDBY) {
			deviceStateIsSTANDBY();
		}
		else if (currentEn50523State == EN50523DeviceState.PROGRAMMED) {
			this.deviceStateIsPROGRAMMED();
		}
		else if (currentEn50523State == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART) {
			this.deviceStateIsPROGRAMMEDWAITINGTOSTART();
		}
		else if (currentEn50523State == EN50523DeviceState.RUNNING) {
			this.deviceStateIsRUNNING();
		}
		else if (currentEn50523State == EN50523DeviceState.PAUSE) {
			this.deviceStateIsPAUSE();
		}
		else if (currentEn50523State == EN50523DeviceState.ENDPROGRAMMED) {
			this.deviceStateIsENDPROGRAMMED();
		}
		else if (currentEn50523State == EN50523DeviceState.FAILURE) {
			this.deviceStateIsFAILURE();
		}
		else if (currentEn50523State == EN50523DeviceState.PROGRAMMEINTERRUPTED) {
			this.deviceStateIsPROGRAMMEINTERRUPTED();
		}
		else if (currentEn50523State == EN50523DeviceState.RINSEHOLD) {
			this.deviceStateIsRINSEHOLD();
		}
		else if (currentEn50523State == EN50523DeviceState.SERVICE) {
			this.deviceStateIsSERVICE();
		}
		else if (currentEn50523State == EN50523DeviceState.SUPERFREEZING) {
			this.deviceStateIsSUPERFREEZING();
		}
		else if (currentEn50523State == EN50523DeviceState.SUPERCOOLING) {
			this.deviceStateIsSUPERCOOLING();
		}
		else if (currentEn50523State == EN50523DeviceState.SUPERHEATING) {
			this.deviceStateIsSUPERHEATING();
		}
		else if (currentEn50523State == EN50523DeviceState.PHASEHOLD) {
			this.deviceStateIsPHASEHOLD();
		}
		
		// notify observer about current power states
		FutureApplianceObserverExchange observerObj
			= new FutureApplianceObserverExchange(
					this.getDeviceID(), 
					now,
					(this.getPower(Commodity.ACTIVEPOWER) != null 			? this.getPower(Commodity.ACTIVEPOWER) : 0), 			// IHALElectricPowerDetails
					(this.getPower(Commodity.REACTIVEPOWER) != null 		? this.getPower(Commodity.REACTIVEPOWER) : 0), 			// IHALElectricPowerDetails
					(this.getPower(Commodity.HEATINGHOTWATERPOWER) != null 	? this.getPower(Commodity.HEATINGHOTWATERPOWER) : 0),	// IHALThermalPowerDetails
					(this.getPower(Commodity.DOMESTICHOTWATERPOWER) != null ? this.getPower(Commodity.DOMESTICHOTWATERPOWER) : 0), 	// IHALThermalPowerDetails
					(this.getPower(Commodity.NATURALGASPOWER) != null 		? this.getPower(Commodity.NATURALGASPOWER) : 0) 		// IHALGasPowerDetails
					);
		
		// IHALGenericApplianceDetails
		observerObj.setEn50523DeviceState(currentEn50523State);
		
		// IHALGenericApplianceProgramDetails
		if ( this.acpChanged ) {
			// profile changed
			// ACP: clone and compress
			observerObj.setApplianceConfigurationProfile(
					applianceConfigurationProfile, 
					LoadProfileCompressionTypes.DISCONTINUITIES,
					1,
					-1);
			observerObj.setAcpReferenceTime(now);
			this.acpChanged = false;
		}
		else {
			// ACP in OX = null
		}

		if (this.applianceConfigurationProfile != null) {
			observerObj.setAcpID(this.applianceConfigurationProfile.getAcpID());
//			getGlobalLogger().logError("applianceConfigurationProfile.getAcpID() " + applianceConfigurationProfile.getAcpID());
		}
		
		
		if(this.expectedFinishTimeReceivedFromAppliance!=null){
			observerObj.setDOF(calculateDOFfromfinishTimeandRemainingTime());
		}
		
			//send OX
			this.notifyObserver(observerObj);
		
	}
	
	private long calculateDOFfromfinishTimeandRemainingTime() {
		//if programmtime is bigger than the time of the settings from the appliance 
		if(expectedEndingTimeReceivedFromAppliance >= expectedFinishTimeReceivedFromAppliance){
			//RETURN DOF = 0
			//means the appliance will be starting right now
			return 0;
		}else {
			//calculate the dof from the programmtime and the setting from the appliance.
			//RETURN DOF
			Long dof =  (this.expectedFinishTimeReceivedFromAppliance - this.expectedEndingTimeReceivedFromAppliance);
			double dofround = dof;
			
			//ROUND DOF in the last 2 numbers, that will give a max change of 50 sec.
			dofround = dofround/100;
			dofround = Math.round(dofround);
			dofround = dofround*100;
			dof= (long) dofround;
			
			return dof;
		}
		
	}


	/**
	 * Is called when there is a new CX object
	 * @param controllerRequest
	 */
	@Override
	synchronized protected void onControllerRequest(HALControllerExchange controllerRequest) {
		FutureApplianceControllerExchange cx = (FutureApplianceControllerExchange) controllerRequest;
		
		// command without ACP available (may be shut off...)
		if (applianceConfigurationProfile == null) {
//			getGlobalLogger().logError(getDeviceType() + " ERROR: received bad command (applianceConfigurationProfile == null)");
			// throw it away...
			return;
		}
		
		//TODO CHeck if necessary
		// check whether UUID of ApplianceConfigurationProfile is still valid
//		getGlobalLogger().logError("applianceConfigurationProfile.getAcpID().equals(cx.getApplianceConfigurationProfileID()" + applianceConfigurationProfile.getAcpID()+" != "+cx.getApplianceConfigurationProfileID());
//		if (!applianceConfigurationProfile.getAcpID().equals(cx.getApplianceConfigurationProfileID())) {
//			getGlobalLogger().logError(getDeviceType() + " ERROR: received bad command (invalid UUID of ACP)");
//			getGlobalLogger().logError(getDeviceType() + " ERROR: mismatch: applianceConfigurationProfileID to OC != applianceConfigurationProfileID from OC");
//			// throw it away...
//			return;
//		}
		
		
		
		if (selectedStartingTimes != null
				&& selectedProfileID == cx.getSelectedProfileId()
				&& selectedStartingTimes.length < cx.getSelectedStartTimes().length) {
			getGlobalLogger().logError(getDeviceType() + " ERROR: wrong length");
			// throw it away...
			return;
		}
		//
		if (this.acpChanged ) {
			getGlobalLogger().logError(getDeviceType() + " ERROR: ACP changed, throw it away...");
			// throw it away...
			return;
		}
		
		// normal
		if (currentEn50523State == EN50523DeviceState.PROGRAMMED
				|| currentEn50523State == EN50523DeviceState.RUNNING) {
			// reprogram appliance
			
			selectedProfileID = cx.getSelectedProfileId();
			selectedStartingTimes = cx.getSelectedStartTimes();
			
			String selectTimes = Arrays.toString(selectedStartingTimes);
			getGlobalLogger().logDebug(getDeviceType() + " RECEIVED Selected starting times: " + selectTimes);
		}
		else {
			getGlobalLogger().logError(getDeviceType() + " ERROR: received CX although not in state PROGRAMMED or RUNNING");
		}
		
	}
	
	
	
	// ### DeviceState - Changed ###

	public EN50523DeviceState getEN50523State() {
		return currentEn50523State;
	}

	protected void setEN50523DeviceState(EN50523DeviceState currentEn50523DeviceState) {
		
		EN50523DeviceState oldState = currentEn50523DeviceState;
		
		if (this.currentEn50523State != currentEn50523DeviceState) {
			this.updateOx = true;
		}
		
		this.currentEn50523State = currentEn50523DeviceState;
		
		if (currentEn50523DeviceState == EN50523DeviceState.UNKNOWN) {
			this.deviceStateChangedToUNKNOWN(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.OFF) {
			this.deviceStateChangedToOFF(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.STANDBY) {
			this.deviceStateChangedToSTANDBY(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.PROGRAMMED) {
			this.deviceStateChangedToPROGRAMMED(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART) {
			this.deviceStateChangedToPROGRAMMEDWAITINGTOSTART(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.RUNNING) {
			this.deviceStateChangedToRUNNING(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.PAUSE) {
			this.deviceStateChangedToPAUSE(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.ENDPROGRAMMED) {
			this.deviceStateChangedToENDPROGRAMMED(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.FAILURE) {
			this.deviceStateChangedToFAILURE(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.PROGRAMMEINTERRUPTED) {
			this.deviceStateChangedToPROGRAMMEINTERRUPTED(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.RINSEHOLD) {
			this.deviceStateChangedToRINSEHOLD(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.SERVICE) {
			this.deviceStateChangedToSERVICE(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.SUPERFREEZING) {
			this.deviceStateChangedToSUPERFREEZING(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.SUPERCOOLING) {
			this.deviceStateChangedToSUPERCOOLING(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.SUPERHEATING) {
			this.deviceStateChangedToSUPERHEATING(oldState);
		}
		else if (currentEn50523DeviceState == EN50523DeviceState.PHASEHOLD) {
			this.deviceStateChangedToPHASEHOLD(oldState);
		}
	}
	
	
	protected void deviceStateChanged(EN50523DeviceState oldState) {
		//NOTHING
		
		if(oldState.name() != currentEn50523State.name()){
			getGlobalLogger().logDebug(getDeviceType() 
					+ " : changed from : " + oldState.name() + " to " + currentEn50523State.name() 
					+ " @" + getTimer().getUnixTime() + ", waiting for optimization...");
		}
	}
	
	
	protected void deviceStateChangedToPHASEHOLD(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToSUPERHEATING(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToSUPERCOOLING(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToSUPERFREEZING(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToSERVICE(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToRINSEHOLD(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToPROGRAMMEINTERRUPTED(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToFAILURE(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToENDPROGRAMMED(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToPAUSE(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToRUNNING(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToPROGRAMMEDWAITINGTOSTART(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToPROGRAMMED(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}

	protected void deviceStateChangedToSTANDBY(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}
	
	protected void deviceStateChangedToOFF(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}
	
	protected void deviceStateChangedToUNKNOWN(EN50523DeviceState oldState) {
		deviceStateChanged(oldState);
		//NOTHING
	}
	
	

	
	// ### Device State is active ###
	
	protected void deviceStateIsUNKNOWN() {
		//NOTHING
	}
	
	protected void deviceStateIsOFF() {
		//NOTHING
	}
	
	protected void deviceStateIsSTANDBY() {
		//NOTHING
	}
	
	protected void deviceStateIsPROGRAMMED() {
		//NOTHING
		
		if (selectedStartingTimes == null
				&& 
				(getCurrentEn50523RemoteControlState() == EN50523DeviceStateRemoteControl.ENABLED_REMOTE_AND_ENERGY_CONTROL
				|| getCurrentEn50523RemoteControlState() == EN50523DeviceStateRemoteControl.ENABLED_REMOTE_CONTROL)) {
			// PROGRAMMED and not optimized, yet
			getGlobalLogger().logDebug("Waiting for optimization: selectedStartTimes == null (not yet optimized)");
		}
	}
	
	protected void deviceStateIsPROGRAMMEDWAITINGTOSTART() {
		//NOTHING
		
		if (selectedStartingTimes == null
				&& 
				(getCurrentEn50523RemoteControlState() == EN50523DeviceStateRemoteControl.ENABLED_REMOTE_AND_ENERGY_CONTROL
				|| getCurrentEn50523RemoteControlState() == EN50523DeviceStateRemoteControl.ENABLED_REMOTE_CONTROL)) {
			// PROGRAMMED and not optimized, yet
			getGlobalLogger().logDebug("Waiting for optimization: selectedStartTimes == null (not yet optimized)");
		}
	}
	
	protected void deviceStateIsRUNNING() {
		//NOTHING
	}
	
	protected void deviceStateIsPAUSE() {
		//NOTHING
	}
	
	protected void deviceStateIsENDPROGRAMMED() {
		//NOTHING
	}
	
	protected void deviceStateIsFAILURE() {
		//NOTHING
	}
	
	protected void deviceStateIsPROGRAMMEINTERRUPTED() {
		//NOTHING
	}
	
	protected void deviceStateIsRINSEHOLD() {
		//NOTHING
	}
	
	protected void deviceStateIsSERVICE() {
		//NOTHING
	}
	
	protected void deviceStateIsSUPERFREEZING() {
		//NOTHING
	}
	
	protected void deviceStateIsSUPERCOOLING() {
		//NOTHING
	}

	protected void deviceStateIsSUPERHEATING() {
		//NOTHING
	}
	
	protected void deviceStateIsPHASEHOLD() {
		//NOTHING
	}

	
	// ### RemoteControl ###
	
	protected EN50523DeviceStateRemoteControl getCurrentEn50523RemoteControlState() {
		return currentEn50523RemoteControlState;
	}
	
	protected void setCurrentEn50523RemoteControlState(EN50523DeviceStateRemoteControl currentEn50523RemoteControlState) {
		this.currentEn50523RemoteControlState = currentEn50523RemoteControlState;
		
		// update ACP
		this.acpChanged = true;
		this.updateOx = true;
	}
	
	
	
	
	public Integer getSelectedConfigurationID() {
		return selectedConfigurationID;
	}
	
	public void setSelectedConfigurationID(Integer selectedConfigurationID) {
		this.selectedConfigurationID = selectedConfigurationID;
		
		// ### build ApplianceConfigurationProfile ###
		SparseLoadProfile[][] dynamicLoadProfiles = XsdLoadProfilesHelperTool.getSparseLoadProfilesArray(
					applianceConfigurations.getApplianceProgramConfiguration().get(selectedConfigurationID).getLoadProfiles());
		// get min and max times
		int[][][] minMaxTimes = new int[dynamicLoadProfiles.length][][];
		for (int i = 0; i < dynamicLoadProfiles.length; i++) {
			minMaxTimes[i] = new int[dynamicLoadProfiles[i].length][2];
			for (int j = 0; j < dynamicLoadProfiles[i].length; j++) {
				minMaxTimes[i][j][0] = applianceConfigurations.getApplianceProgramConfiguration().get(selectedConfigurationID)
						.getLoadProfiles().getLoadProfile().get(0).getPhases().getPhase().get(j).getMinLength();
				minMaxTimes[i][j][1] = applianceConfigurations.getApplianceProgramConfiguration().get(selectedConfigurationID)
						.getLoadProfiles().getLoadProfile().get(0).getPhases().getPhase().get(j).getMaxLength();		
			}
		}
		
		ApplianceProgramConfigurationStatus newACP = new ApplianceProgramConfigurationStatus(
				UUID.randomUUID(), 
				dynamicLoadProfiles,
				minMaxTimes,
				getTimer().getUnixTime());
		this.applianceConfigurationProfile = newACP;
		this.acpChanged = true;
	}
	
	protected String getProgramName(int selectedConfigurationID) {
		String ret = null;
		try {
			ret = applianceConfigurations
					.getApplianceProgramConfiguration()
					.get(selectedConfigurationID)
					.getProgram().getDescriptions()
					.getDescription().get(0).getValue();
		}
		catch (Exception e) {
			//...doesn't exist...so what...return null!
		}
		return ret;
	}
	
	public void setExpectedEndingTimeReceivedFromAppliance(
			Long expectedEndingTimeReceivedFromAppliance) {
		this.expectedEndingTimeReceivedFromAppliance = expectedEndingTimeReceivedFromAppliance;
	}
	
	public void setExpectedFinishTimeReceivedFromAppliance(
			Long expectedFinishTimeReceivedFromAppliance) {
		this.expectedFinishTimeReceivedFromAppliance = expectedFinishTimeReceivedFromAppliance;
	}
	
	protected long[] getSelectedStartingTimes() {
		if(selectedStartingTimes!=null){
			return selectedStartingTimes.clone();
		}
		else{
			return null;
		}
	}
	
	protected Integer getSelectedProfileID() {
		return selectedProfileID;
	}
	
	protected void setAcpChanged(boolean acpChanged) {
		this.acpChanged = acpChanged;
	}
	
	private long getExpectedEndingTime() {
		if (expectedEndingTimeReceivedFromAppliance != null) {
			// received ending time from appliance
			return expectedEndingTimeReceivedFromAppliance;
		}
		else {
			if (applianceConfigurationProfile != null
					&& selectedStartingTimes != null
					&& selectedProfileID != null) {
				long now = getTimer().getUnixTime();
				
				//TODO calculate ending time
				return now;
			}
			else {
				return -1;
			}
		}
	}
	
	
	private long getExpectedFinishTime() {
		if (expectedFinishTimeReceivedFromAppliance != null) {
			// received ending time from appliance
			return expectedFinishTimeReceivedFromAppliance;
		}
		return -1;
	}
	
	// ### reset method for variables
	protected void resetVariables() {
		// ### Expected ending time received from appliance
		expectedEndingTimeReceivedFromAppliance = null;
		
		// ### Expected Finish time received from appliance DOF
		expectedFinishTimeReceivedFromAppliance = null;
		
		
		// Device Configuration which is now active: Selected by user on device */
		selectedConfigurationID = null;
		
		// StartingTime of Active Configuration Profile (ACP) */
		configurationStartedAt = null;
		
		// StartingTime of Active Phase in Active Configuration Profile */
		phaseStartedAt = null;
		
		
		// result of optimization
		
		// in case of eDoF: result of optimization */
		selectedProfileID = null;
		
		// in case of tDoF: result of optimization  */
		selectedStartingTimes = null;
		
		// Active Configuration Profile (ACP)<br>
		applianceConfigurationProfile = null;
		
		// indicates whether ACP has changed */
		acpChanged = false; //TODO check whether this makes sense
	}
	
	
	
	// ### Details Creators ###
	
	protected void updateGenericApplianceDriverDetails(long now) {
		GenericApplianceDriverDetails driverDetails = createApplianceDetails(now);
		getDriverRegistry().setStateOfSender(GenericApplianceDriverDetails.class, driverDetails);
	}
	
	
	private GenericApplianceDriverDetails createApplianceDetails(long now) {
		GenericApplianceDriverDetails details = new GenericApplianceDriverDetails(getDeviceID(), now);
		details.setState(currentEn50523State);
		details.setStateTextDE(currentEn50523State.getDescriptionDE());
		return details;
	}
	
	
	protected void updateGenericApplianceProgramDriverDetails(long now) {
		GenericApplianceProgramDriverDetails pdd = this.createGenericApplianceProgramDriverDetails(now);
		getDriverRegistry().setStateOfSender(GenericApplianceProgramDriverDetails.class, pdd);
	}
	
	
	private GenericApplianceProgramDriverDetails createGenericApplianceProgramDriverDetails(long now) {
		GenericApplianceProgramDriverDetails details = new GenericApplianceProgramDriverDetails(getDeviceID(), now);
		
		if ( selectedConfigurationID == null
				|| selectedConfigurationID == -1) {
			details.setProgramName("no program available");
		}
		else {
			String programName = getProgramName(selectedConfigurationID);
			details.setProgramName(programName);
		}
		
		if ( selectedStartingTimes == null ) {
			details.setStartTime(-1);
		}
		else {
			details.setStartTime(selectedStartingTimes[0]);
		}
		
		long expectedEndingTime = getExpectedEndingTime();
		
		if ( expectedEndingTime == -1 ) {
			details.setEndTime(-1);
		}
		else {
			details.setEndTime(expectedEndingTime);
		}
		
		if ( expectedEndingTime == -1 ) {
			details.setRemainingTime(-1);
		}
		else {
			details.setRemainingTime((int) (expectedEndingTime - now));
		}
		
		long expectedFinishTime = getExpectedFinishTime();
		
		if(expectedFinishTime == -1){
			details.setFinishTime(-1);
		}
		else{
			details.setFinishTime(expectedFinishTime);
		}
		
		
		return details;
	}
}
