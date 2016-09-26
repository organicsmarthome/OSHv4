package osh.driver.simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.xml.sax.SAXException;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.XsdApplianceProgramConfigurations;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.appliance.generic.XsdLoadProfilesHelperTool;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.en50523.EN50523DeviceState;
import osh.en50523.EN50523DeviceStateRemoteControl;
import osh.hal.exchange.FutureApplianceControllerExchange;
import osh.hal.exchange.FutureApplianceObserverExchange;
import osh.registry.interfaces.IHasState;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.ActionParameter;
import osh.simulation.screenplay.ActionParameters;
import osh.simulation.screenplay.ActionParametersHelper;
import osh.simulation.screenplay.ActionType;
import osh.simulation.screenplay.PerformAction;
import osh.simulation.screenplay.ScreenplayType;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.time.TimeConversion;

/**
 * Interruptible hybrid appliance
 * 
 * @author Ingo Mauser
 *
 */
public class GenericFutureApplianceSimulationDriver 
					extends ApplianceSimulationDriver 
					implements IHasState {

	// ### Variables for ESC ###

//	/** Currently unused, but may be used to react, e.g., on in-flow water temperature... */
//	@SuppressWarnings("unused")
//	private EnumMap<Commodity, RealCommodityState> commodityInputStates;

	// ### Variables for DeviceState ###

	/** DIN EN 50523 DeviceState (OFF, RUNNING, ...) */
	protected EN50523DeviceState currentEn50523State;

	/** DIN EN 50523 Remote Control State (DISABLED, TEMPORARILY_DISABLED, ENABLED) */
	protected EN50523DeviceStateRemoteControl currentEn50523RemoteControlState;


	// ### quasi-static Variables for Configurations (= program + extras + loadProfiles) ###

	/** All Configurations (= program + extras + loadProfiles) */
	protected XsdApplianceProgramConfigurations applianceConfigurations;


	// ### Variables for current Configuration ###

	/** Device Configuration which is now active: Selected by user on device */
	protected Integer selectedConfigurationID = null;

	/** StartingTime of Active Configuration Profile (ACP) */
	protected Long configurationStartedAt = null;
	
	protected long lastSet1sttDof = 0;

	/** StartingTime of Active Phase in Active Configuration Profile */
	protected Long phaseStartedAt = null;


	// ### PRIVATE variables for exclusive usage in this class, NOT in subclasses ###

	/** in case of eDoF: result of optimization */
	private Integer selectedProfileID;

	/** in case of tDoF: result of optimization  */
	private long[] selectedStartingTimes;

	/**
	 * Active Configuration Profile (ACP)<br>
	 * contains:<br> 
	 * dynamicLoadProfiles: remaining dynamic load profiles with tDoF or eDoF (using relative times to 0)<br>
	 */
	private ApplianceProgramConfigurationStatus applianceConfigurationProfile;

	/** indicates whether ACP has changed */
	private boolean acpChanged;


	/** if there was no way to schedule the planned runs for a day schedule the runs the day after */
	private int runCorrection = 0;
	private int[] correctionSelectedIDs = new int[0];

	// ### Debugging and Logging variables ###
	//TODO make nice
	/** number of total runs since loading of driver */
	private double activePowerConsumption = 0.0;
	private int totalRealizedNumberOfRuns = 0;
	private int totalPlannedNumberOfRuns = 0;
	private int[] profileNumberOfRuns;
	private double avgTotalRuns = 0.0;
	private int totalNumberOfRunsProfile0 = 0;
	private int totalNumberOfRunsProfile1 = 0;
	private int[] startTimes = new int[1440];
	private int[] dofs = new int[1440];
	private int[] profilesSelected;


	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws JAXBException 
	 * @throws SAXException 
	 * @throws HALException 
	 */
	public GenericFutureApplianceSimulationDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
					throws SimulationSubjectException, JAXBException, SAXException, HALException {
		super(controllerbox, deviceID, driverConfig);

		// load some variables (e.g. initialize variables)

		// IMPORTANT:
		// if (getDeviceType() == DeviceTypes.WASHINGMACHINE) <-- does NOT work!

		// get WashingParametersConfigurations profile file
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

//		SEKR: Already done by superclass(DeviceSimulationDriver)
//		// get Commodities used by this device
//		{
//			String commoditiesArray = driverConfig.getParameter("usedcommodities");
//			if (commoditiesArray != null) {
//				usedCommodities = Commodity.parseCommodityArray(commoditiesArray);
//			}
//			else {
//				throw new HALException("Used Commodities are missing!");
//			}
//		}

		// default: OFF
		turnOff();

		// default: remote enabled (well...it's a simulation...)
		this.currentEn50523RemoteControlState = EN50523DeviceStateRemoteControl.ENABLED_REMOTE_CONTROL;

		this.profilesSelected = new int[applianceConfigurations.getApplianceProgramConfiguration().size()];
		this.profileNumberOfRuns = new int[getConfigurationShares().length];
		Arrays.fill(profilesSelected, 0);
		Arrays.fill(profileNumberOfRuns, 0);
		Arrays.fill(startTimes, 0);
		Arrays.fill(dofs, 0);
	}


//	Nothing to do for now
//	@Override
//	public void onSystemIsUp() {
//		super.onSystemIsUp();
//
//		// do after loading...do e.g.
//		// register for call of onNextTimePeriod() for every 10 ticks...
//		// getTimer().registerComponent(this, 10);
//
//		// register for StateExchanges from DriverRegistry
//		// this.getDriverRegistry().register(StateExchange.class, this);
//	}


	@Override
	public void onNextTimeTick() {
		// get current time
		long now = getTimer().getUnixTime();

		// if not OFF -> device logic for running etc
		if (currentEn50523State == EN50523DeviceState.OFF) {
			// Device is OFF - there is nothing to do
			// set power to 0 (to be safe)
			for (Commodity c : usedCommodities) {
				this.setPower(c, 0);
			}
		}
		else if (currentEn50523State == EN50523DeviceState.PROGRAMMED) {
			doLogicProgrammed(now);
		}
		else if (currentEn50523State == EN50523DeviceState.RUNNING) {
			doLogicRunning(now);
		}
		else if (currentEn50523State == EN50523DeviceState.ENDPROGRAMMED) {
			// if ENDPROGAMMED then the appliance is immediately turned off by user
			turnOff();
		}


		// notify observer about current power states
		FutureApplianceObserverExchange observerObj
		= new FutureApplianceObserverExchange(
				this.getDeviceID(), 
				now,
				this.getPower(Commodity.ACTIVEPOWER), // IHALElectricPowerDetails
				this.getPower(Commodity.REACTIVEPOWER), // IHALElectricPowerDetails
				this.getPower(Commodity.HEATINGHOTWATERPOWER), // IHALThermalPowerDetails
				this.getPower(Commodity.DOMESTICHOTWATERPOWER), // IHALThermalPowerDetails
				this.getPower(Commodity.NATURALGASPOWER) // IHALGasPowerDetails
				);
		observerObj.setDOF(lastSet1sttDof);

		this.activePowerConsumption += (double) this.getPower(Commodity.ACTIVEPOWER);

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
		}

		// send OX
		this.notifyObserver(observerObj);
	}


	/**
	 * Logic when in state PROGRAMMED
	 */
	private void doLogicProgrammed(long now) {
		// start device if time is reached / if it has been optimized...
		if (this.selectedStartingTimes == null) {
			// PROGRAMMED and not optimized, yet
			// wait for optimization
			getGlobalLogger().logDebug(getDeviceType() + " : PROGRAMMED @" + now + ", waiting for optimization...");
		}
		else {
			// received selected starting times, go RUNNING (maybe running in pause...)
			setEN50523State(EN50523DeviceState.RUNNING);
			this.configurationStartedAt = now;
			this.phaseStartedAt = now;
			this.acpChanged = true;
			getGlobalLogger().logDebug(
					getDeviceType() + " : started RUNNING @" + now 
					+ " with selectedStartingTimes: " + Arrays.toString(selectedStartingTimes)
					+ " and selectedProfile: " + selectedProfileID);
		}
	}


	/**
	 * Logic when in state RUNNING
	 */
	private void doLogicRunning(long now) {
		// validity check
		{
			int currentDurationSinceStart = (int) (now - configurationStartedAt);
			if (currentDurationSinceStart < 0) {
				getGlobalLogger().logError(getDeviceType() + " ERROR: timewarp, currentDurationSinceStart is negative!", new Exception());
			}
		}

		// ### calculate current state in ApplianceConfigurationProfile ###
		// get LoadProfile
		SparseLoadProfile[][] dlp = applianceConfigurationProfile.getDynamicLoadProfiles();
		// get min max times
		int[][][] minMaxTimes = applianceConfigurationProfile.getMinMaxDurations();

		if (this.selectedStartingTimes == null) {
			getGlobalLogger().logDebug(getDeviceType() + " ERROR: should not happen");
		}

		if (selectedStartingTimes.length != minMaxTimes[selectedProfileID].length
				|| selectedStartingTimes.length != dlp[selectedProfileID].length ) {
			getGlobalLogger().logDebug(getDeviceType() + " ERROR: wrong length");
		}

		// check if next phase has to be started
		// [0] is currently running phase / next phase
		if (selectedStartingTimes.length > 1 && now >= selectedStartingTimes[1]) {
			// NEW PHASE
			// current phase is finished...
			// next phase is due...
			getGlobalLogger().logDebug(getDeviceType() + " : switched to next phase");
//			if (selectedNextProfileID != null) {
//				if (selectedProfileID != selectedNextProfileID) {
//					getGlobalLogger().logDebug(getDeviceType() + " : switching to new profile: " + selectedNextProfileID);
//				}					
//				selectedProfileID = selectedNextProfileID;
//				selectedNextProfileID = null;
//			}

			// ### determine new ACP ###
			// shorten old DLP and MinMaxTimes
			SparseLoadProfile[][] newDlp = new SparseLoadProfile[dlp.length][];
			int[][][] newMinMaxTimes = new int[dlp.length][][];
			for (int i = 0; i < dlp.length; i++) {
				newDlp[i] = new SparseLoadProfile[dlp[i].length - 1];
				newMinMaxTimes[i] = new int[dlp[i].length - 1][];
				for (int j = 1 /* sic! */; j < dlp[i].length; j++) {
					newDlp[i][j-1] = dlp[i][j];
					newMinMaxTimes[i][j-1] = minMaxTimes[i][j];
				}
			}
			
			// build new ACP
			ApplianceProgramConfigurationStatus newACP = new ApplianceProgramConfigurationStatus(
					UUID.randomUUID(), 
					newDlp,
					newMinMaxTimes,
					now);
			this.applianceConfigurationProfile = newACP;
			
			//next phase would be last phase --> do not send an updated acp as this would cause a rescheduling, controller will reschedule when device --> off
			if (selectedStartingTimes.length == 2 && selectedStartingTimes[1] + minMaxTimes[selectedProfileID][1][0] <= now + 1) {
				getGlobalLogger().logDebug("Switched to last phase, set notReschedule-Flag");
				newACP.setDoNotReschedule(true);
			} 
			
			this.acpChanged = true;
			this.phaseStartedAt = now;

			// shorten selectedStartingTimes (until upcoming optimization is finished)
			long[] newSelectedStartingTimes = new long[selectedStartingTimes.length - 1];
			for (int i = 1; i < selectedStartingTimes.length; i++) {
				newSelectedStartingTimes[i-1] = selectedStartingTimes[i];
			}
			this.selectedStartingTimes = newSelectedStartingTimes;

			// set new power values...
			for (Commodity c : usedCommodities) {
				try {
					setPower(c, newDlp[selectedProfileID][0].getLoadAt(c, (int) (now - phaseStartedAt))); // (now - configurationStartedAt) = 0!
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else if (selectedStartingTimes.length == 1 && selectedStartingTimes[0] + minMaxTimes[selectedProfileID][0][0] <= now) {
			// END LAST PHASE
			// it has been the last phase:
			// END of program reached (in simulation: always exactly as expected)

			if (selectedProfileID == 0) {
				totalNumberOfRunsProfile0++;
			}
			else if (selectedProfileID == 1) {
				totalNumberOfRunsProfile1++;
			}
			
			this.profilesSelected[selectedProfileID]++;

			totalRealizedNumberOfRuns++;

			// switch to ENDPROGRAMMED
			setEN50523State(EN50523DeviceState.ENDPROGRAMMED);

			// set all powers to 0
			for (Commodity c : usedCommodities) {
				setPower(c, 0);
			}
		}
		else {
			// stay in CURRENT PHASE...
			// phase is running (not yet finished)...
			// is RUNNING...get current power values / load

			int currentDurationSinceStart = (int) (now - phaseStartedAt);
			int corrected = (int) (currentDurationSinceStart % dlp[selectedProfileID][0].getEndingTimeOfProfile());

			// set new power values...
			for (Commodity c : usedCommodities) {
				setPower(c, dlp[selectedProfileID][0].getLoadAt(c, corrected));
			}
		}
	}

	/**
	 * Turn it off...
	 */
	private void turnOff() {
		long now = getTimer().getUnixTime();

		for (Commodity c : usedCommodities) {
			this.setPower(c, 0);
		}

		setEN50523State(EN50523DeviceState.OFF);
		getGlobalLogger().logDebug(getDeviceType() + " : switched OFF @" + now);

		// reset variables
		this.acpChanged = true;
		this.applianceConfigurationProfile = null;

		this.configurationStartedAt = null;
		this.phaseStartedAt = null;

		this.selectedConfigurationID = null;
		this.selectedProfileID = null;
//		this.selectedNextProfileID = null;
		this.selectedStartingTimes = null;
	}


	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();

		if (getOSH().getOSHstatus().isSimulation()) {
			//DEBUG
			// output the number of runs of this device

			if (DatabaseLoggerThread.isLogDevices()) {
				DatabaseLoggerThread.enqueueDevices(totalPlannedNumberOfRuns, totalRealizedNumberOfRuns, activePowerConsumption / 3600000.0 , profileNumberOfRuns, 
						dofs, startTimes, profilesSelected, getDeviceType());
			}

			try {
				String fileName = getOSH().getOSHstatus().getLogDir()  + "/"
						//						+ "_" + getRandomGenerator().getNextLong() 
						+ "_" + getDeviceType() 
						+ "_" + (System.currentTimeMillis() / 1000)
						+ ".txt";
				PrintWriter pwr = new PrintWriter(new File(fileName));
				pwr.println("TOTAL;"+totalPlannedNumberOfRuns);
				pwr.println("AVGTOTAL;"+avgTotalRuns);
				pwr.println("REALTOTAL;"+totalRealizedNumberOfRuns);
				pwr.println("TOTAL0;"+totalNumberOfRunsProfile0);
				pwr.println("TOTAL1;"+totalNumberOfRunsProfile1);
				pwr.close();
				System.out.println("Output: " + fileName);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//DEBUG END
		}
	}


	/**
	 * temporary until state machine is included<br>
	 * re-programming: oldState == newState but state could have changed (e.g. other configurationId)
	 * @param newState
	 */
	private void setEN50523State(EN50523DeviceState newState) {
		getGlobalLogger().logDebug(
				getDeviceType() 
				+ " : performAction: change from " + currentEn50523State
				+ " to " + newState + " (" + getDeviceID() + ")");

		this.currentEn50523State = newState;
	}


	/**
	 * Is called when there is a new CX object
	 * @param controllerRequest
	 * @throws HALException 
	 */
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
		super.onControllerRequest(controllerRequest);

		FutureApplianceControllerExchange cx = (FutureApplianceControllerExchange) controllerRequest;

		// command without ACP available (may be shut off...)
		if (applianceConfigurationProfile == null) {
			getGlobalLogger().logError(getDeviceType() + " ERROR: received bad command (applianceConfigurationProfile == null)");
			// throw it away...
			return;
		}
		// check whether UUID of ApplianceConfigurationProfile is still valid
		if (!applianceConfigurationProfile.getAcpID().equals(cx.getApplianceConfigurationProfileID())) {
			getGlobalLogger().logError(getDeviceType() + " received bad command (invalid UUID of ACP)");
			getGlobalLogger().logError(getDeviceType() + " mismatch: applianceConfigurationProfileID to OC != applianceConfigurationProfileID from OC");
			// throw it away...
			return;
		}
		//
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
						
			if (selectedProfileID != null) {
				if (selectedProfileID != cx.getSelectedProfileId()) {
					getGlobalLogger().logDebug(getDeviceType() + " : switching to new profile: " + cx.getSelectedProfileId());
				}				
			}
			
			selectedProfileID = cx.getSelectedProfileId();
			selectedStartingTimes = cx.getSelectedStartTimes();

			String selectTimes = Arrays.toString(selectedStartingTimes);
			getGlobalLogger().logDebug(getDeviceType() + " RECEIVED Selected starting times: " + selectTimes + " with selected profile: " + selectedProfileID);// (selectedNextProfileID == null ? selectedProfileID : selectedNextProfileID));
		}
		else {
			getGlobalLogger().logError(getDeviceType() + " received CX although not in state PROGRAMMED or RUNNING");
		}

	}


	// <### SCREENPLAY STUFF ###>

	// for generation of appliance executions...
	@Override
	public void performNextAction(SubjectAction nextAction) {

		// IMPORTANT: DO DoF-Action FIRST!

		// DoF-Action
		if (nextAction.getActionType() == ActionType.USER_ACTION) {
			int newDof = 0;

			String dofString = ActionParametersHelper.getValueForParameterOfParameters(
					nextAction.getPerformAction().get(0), 
					"dof", 
					"tdof");

			if (dofString != null) {
				newDof = Integer.valueOf(dofString);
			}
			else {
				newDof = 0;
			}
			
			lastSet1sttDof = newDof;
		}

		//if the next state is true the i-appliance is filled up and is now able to run
		else if (nextAction.isNextState() && nextAction.getActionType() == ActionType.I_DEVICE_ACTION) {

			// SAFETY FIRST!
			if (currentEn50523State == EN50523DeviceState.PROGRAMMED
					|| currentEn50523State == EN50523DeviceState.RUNNING) {
				getGlobalLogger().logDebug(getDeviceType() + " device already running, no new action possible");
				return;
			}

			// get selected configurationID
			String selectedConfigurationIDString = ActionParametersHelper.getValueForParameterOfParameters(
					nextAction.getPerformAction().get(0), 
					"appliance", 
					"configuration");
			if (selectedConfigurationIDString != null) {
				selectedConfigurationID = Integer.valueOf(selectedConfigurationIDString);
			}
			else {
				selectedConfigurationID = 0;
			}

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

			if ( this.isControllable() ) {
				// if controllable then PROGRAMMED
				this.configurationStartedAt = getTimer().getUnixTime();
				setEN50523State(EN50523DeviceState.PROGRAMMED); // wait until optimization in PROGRAMMED state
				selectedProfileID = null; // safety first...
//				selectedNextProfileID = null; //safety first
				selectedStartingTimes = null; // safety first...
			}
			else {
				// if NOT controllable then RUNNING
				this.configurationStartedAt = getTimer().getUnixTime();
				this.phaseStartedAt = configurationStartedAt;
				setEN50523State(EN50523DeviceState.RUNNING);
				this.selectedProfileID = 0; // start with the first profile

				// shorten available profiles in DLP
				SparseLoadProfile[][] shortenedDlp = new SparseLoadProfile[1][];
				shortenedDlp[0] = dynamicLoadProfiles[0];
				dynamicLoadProfiles = shortenedDlp;

				// shorten minMaxTimes
				int[][][] shortenedMinMaxTimes = new int[1][][];
				shortenedMinMaxTimes[0] = minMaxTimes[0];
				minMaxTimes = shortenedMinMaxTimes;

				// set starting times (start immediately with minimum times)
				selectedStartingTimes = new long[shortenedDlp[0].length];
				long time = configurationStartedAt;
				for (int i = 0; i < dynamicLoadProfiles[0].length; i++) {
					selectedStartingTimes[i] = time;
					time = time + minMaxTimes[0][i][0];
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
		else if (!nextAction.isNextState() && nextAction.getActionType() == ActionType.I_DEVICE_ACTION) {
			// should currently not be in use...
			getGlobalLogger().logDebug(
					getDeviceType() 
					+ " performAction: change from " + currentEn50523State.toString() 
					+ " to OFF (" + getDeviceID() + ")");
			turnOff();
		}
	}

	@Override
	protected void generateDynamicDailyScreenplay() throws OSHException {
		// get current Random-Number and construct new Random-generator
		// (reason: there might be a varying number of random values necessary for one day,
		//          resulting in deviations of the optimization using different parameters for
		//          the optimization)
		// Author: IMA

		long initialNumber = getRandomGenerator().getNextLong();
		OSHRandomGenerator newRandomGen = new OSHRandomGenerator(new Random(initialNumber));

		// get number of configurations
		int noOfPrograms = applianceConfigurations.getApplianceProgramConfiguration().size();
		if (noOfPrograms != getConfigurationShares().length) {
			throw new OSHException("ERROR: noOfPrograms != configurationShares.length");
		}

		boolean lastDay = (getTimer().getUnixTime() - getTimer().getUnixTimeAtStart()) / 86400 
				== (getSimulationEngine().getSimulationDuration() / 86400 - 1);

		//calculate runs per day, correct for impossible runs from earlier days
		int runsToday = 0;
		// days with higher consumption explained by run of devices have more runs
		long now = getTimer().getUnixTime();
		int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(now);
		double avgRunsToday = getAvgDailyRuns() * getCorrectionFactorDay()[dayOfYear];
		
		int dailyRunsMin = (int) Math.floor(avgRunsToday);
		int dailyRunsMax = (int) Math.ceil(avgRunsToday);
		double probMax = avgRunsToday - dailyRunsMin;
		double r = newRandomGen.getNextDouble();
		if (r < probMax) {
			runsToday += dailyRunsMax;
		}
		else {
			runsToday += dailyRunsMin;
		}		

		this.avgTotalRuns += avgRunsToday;
		this.totalPlannedNumberOfRuns += runsToday;
		

		// if there is at least one run today...generate action!
		if (runsToday + runCorrection > 0) {

			// select programs randomly (e.g. washing parameters...)
			// based on configurationShares -> prepare distribution
			double[] configurationDistribution = new double[getConfigurationShares().length];
			for (int i = 0; i < getConfigurationShares().length; i++) {
				if (i == 0) {
					configurationDistribution[0] = getConfigurationShares()[0];
				}
				else if (i == getConfigurationShares().length - 1) {
					configurationDistribution[i] = 1;
				}
				else {
					configurationDistribution[i] = configurationDistribution[i-1] + getConfigurationShares()[i];
				}
			}

			// calculate max tDoF based on number of runs today and their maximum possible duration
			int maxProgramDuration = XsdLoadProfilesHelperTool.getMaximumDurationOfAllConfigurations(applianceConfigurations);
			//check if parts of this day are blocked by actions of the previous day
			long blockedTime = checkForBlockedSeconds();
			long blockedSeconds = blockedTime == 0 ? 0 : blockedTime - now;

			int currentMax1stDof = calcMax1stTDof(runsToday + runCorrection, (int) (86400 - blockedSeconds), maxProgramDuration);

			//if there is more then 1 run to be scheduled, save the runtimes to check if planned runs do overlap
			int[] generatedConfigurationIDs = new int[runsToday + runCorrection];
			int[] selectedProfileLengths = new int[runsToday + runCorrection];
			
			//restoring configured runs from earlier days that could notr be scheduled
			for (int i = 0; i < runCorrection; i++) {
				generatedConfigurationIDs[i] = correctionSelectedIDs[i];
				selectedProfileLengths[i] = XsdLoadProfilesHelperTool.getMaximumLengthOfOneConfiguration(
						applianceConfigurations.getApplianceProgramConfiguration().get(correctionSelectedIDs[i]));				
			}

			//generate configurationIDs
			for (int i = 0; i < runsToday; i++) {
				// select one program randomly
				double randomForProgramChoice = newRandomGen.getNextDouble();
				int configurationForThisRun = 0;
				for (int j = 0; j < configurationDistribution.length; j++) {
					if (randomForProgramChoice > configurationDistribution[j]) {
						configurationForThisRun++;
					}
				}
				generatedConfigurationIDs[runCorrection + i] = configurationForThisRun;
				selectedProfileLengths[runCorrection + i] = XsdLoadProfilesHelperTool.getMaximumLengthOfOneConfiguration(
						applianceConfigurations.getApplianceProgramConfiguration().get(configurationForThisRun));
				
				this.profileNumberOfRuns[configurationForThisRun]++;
			}
			
			runsToday += runCorrection;
			runCorrection = 0;
			correctionSelectedIDs = new int[0];

			int errorCount = -1;
			Long[][] selectedStartTimeAndDofs = null;

			while(selectedStartTimeAndDofs == null) {
				errorCount++;
				
				if (runsToday == 0)
					continue;

				if (errorCount == 40) {
					//okay, we can't shift it to the next day, so we'll cheat
					if (lastDay) {
						long lastEndTime = blockedTime == 0 ? now : blockedTime;
						long endOfSim = getSimulationEngine().getSimulationDuration() + getTimer().getUnixTimeAtStart();
						
						selectedStartTimeAndDofs = new Long[runsToday][2];
						for (int i = 0; i < runsToday; i++) {
							//not even cheating works, so good bye sweet sweet world
							if (lastEndTime >= endOfSim) {
								runsToday = i;
								selectedStartTimeAndDofs = Arrays.copyOf(selectedStartTimeAndDofs, runsToday);

								getGlobalLogger().logError("UUID=" +  getDeviceID() + " DeviceType=" + getDeviceType() + " Cannot possibly schedule for last day");
								break;
							}

							selectedStartTimeAndDofs[i][0] = lastEndTime + 100;							
							selectedStartTimeAndDofs[i][1] = 0L;

							lastEndTime = lastEndTime + 100 + selectedProfileLengths[i];
						}

						break;

					} else {

						getGlobalLogger().logError("UUID=" +  getDeviceID() + " DeviceType=" + getDeviceType() + " Unable to schedule a run, correcting now");
						errorCount = 0;
						//move last selected run to tomorrow
						runCorrection++;
						this.correctionSelectedIDs = Arrays.copyOf(this.correctionSelectedIDs, runCorrection);
						this.correctionSelectedIDs[runCorrection - 1] = generatedConfigurationIDs[runsToday - 1];						

						runsToday--;						
						generatedConfigurationIDs = Arrays.copyOf(generatedConfigurationIDs, runsToday);
						selectedProfileLengths = Arrays.copyOf(selectedProfileLengths, runsToday);
						//no way to schedule even 1 run
						if (runsToday == 0) {
							getGlobalLogger().logError("UUID=" +  getDeviceID() + " DeviceType=" + getDeviceType() + " Cannot even schedule a single run for this day");
							break;
						}							
					}						
				}				

				selectedStartTimeAndDofs = generateStartTimesAndTDofs(runsToday, selectedProfileLengths, newRandomGen, now, blockedSeconds, currentMax1stDof, lastDay);				
			}
			
			for (int i = 0; i < runsToday; i++) {
				
				long startTime = selectedStartTimeAndDofs[i][0];
				long tDof = selectedStartTimeAndDofs[i][1];
				
				// Log it to console for debugging...
				getGlobalLogger().logDebug("UUID=" +  getDeviceID() + " DeviceType=" + getDeviceType() + " Start=" + startTime + " DoF=" + tDof);
				generateAndSetActions(startTime, generatedConfigurationIDs[i], tDof);				
			}
		}
	}

	private Long[][] generateStartTimesAndTDofs(int numberToGenerate, int[] selectedProfileLengths, OSHRandomGenerator randomGen, 
			long now, long blockedSeconds, int currentMax1stTDof, boolean lastDay) throws OSHException {

		Long[][] values = new Long[numberToGenerate][2];

		for (int i = 0; i < numberToGenerate; i++) {
			long startTime = 0;
			int maxTicks = selectedProfileLengths[i];
			int middleOfPowerConsumption = maxTicks / 2;	

			do {
				// select random start time for run
				double randomValue = randomGen.getNextDouble();		
				startTime = getRandomTimestampForRunToday(
						now, 
						middleOfPowerConsumption, 
						randomValue, 
						randomGen);
			} while (startTime <= now + blockedSeconds);

			// better do not generate action for the beginning of the simulation (no signal...)
			if (startTime < getTimer().getUnixTimeAtStart() + 100) {
				startTime = 100;
			}
			//ensure the start time is in the future (action will otherwise be deleted)
			startTime = Math.max(now + 100, startTime);

			long tDof = generateNewDof(true, numberToGenerate, startTime, randomGen, getDeviceMax1stDof(), currentMax1stTDof);

			//check that startTime does not violate previous selected startTimes/tDofs
			long currentEnd = startTime + tDof + maxTicks + 100;
			
			if (lastDay && currentEnd >= getTimer().getUnixTimeAtStart() + getSimulationEngine().getSimulationDuration())
				return null;
			
			for (int j = 0; j < i; j++) {

				long otherEnd = (values[j][0] + values[j][1] + selectedProfileLengths[j] + 100);

				if ((startTime >= values[j][0] && startTime <= otherEnd)
						|| (currentEnd >= values[j][0] && currentEnd <= otherEnd)
						|| (startTime <= values[j][0] && currentEnd >= otherEnd)) {
					return null;
				}				
			}
			values[i][0] = startTime;
			values[i][1] = tDof;
		}

		return values;
	}

	private void generateAndSetActions(long startTime, int configurationID, long tDof) {
		
		dofs[(int) Math.round((double) tDof / 60.0)]++;
		startTimes[TimeConversion.convertUnixTime2MinuteOfDay(startTime)]++;

		SubjectAction action = new SubjectAction();
		action.setTick(startTime);		      
		action.setDeviceID(getDeviceID().toString());
		action.setNextState(true);
		action.setActionType(ActionType.I_DEVICE_ACTION);

		// add selected configuration to program action
		PerformAction configurationPerformAction = new PerformAction();
		ActionParameters configurationActionParameters = new ActionParameters();
		configurationActionParameters.setParametersName("appliance");
		ActionParameter configurationActionParameter = new ActionParameter();
		configurationActionParameter.setName("configuration");
		configurationActionParameter.setValue("" + configurationID);
		configurationActionParameters.getParameter().add(configurationActionParameter);
		configurationPerformAction.getActionParameterCollection().add(configurationActionParameters);
		action.getPerformAction().add(configurationPerformAction);
		this.setAction(action);


		//create now the new action
		SubjectAction dofAction = new SubjectAction();
		dofAction.setTick(startTime - 1); // Do 1 sec in advance!
		dofAction.setDeviceID(this.getDeviceID().toString());
		dofAction.setActionType(ActionType.USER_ACTION);
		dofAction.setNextState(false);		
		PerformAction dofAction2Perform = new PerformAction();
		ActionParameters dofActionParameters = new ActionParameters();
		dofActionParameters.setParametersName("dof");
		ActionParameter dofActionParameter = new ActionParameter();
		dofActionParameter.setName("tdof");
		dofActionParameter.setValue("" + tDof);
		dofActionParameters.getParameter().add(dofActionParameter);
		dofAction2Perform.getActionParameterCollection().add(dofActionParameters);
		dofAction.getPerformAction().add(dofAction2Perform);
		this.setAction(dofAction);
	}


	// Overrides the tDoF in the LocalController
	@Override
	protected int generateNewDof(
			boolean useRandomDof, 
			int actionCountPerDay, 
			long applianceActionTimeTick, 
			OSHRandomGenerator randomGen,
			int maxDof,
			int maxPossibleDof) {
		// generate DOFs with binary distribution
		if (getSimulationEngine().getScreenplayType() == ScreenplayType.DYNAMIC) {
			int maxProgramDuration = XsdLoadProfilesHelperTool.getMaximumDurationOfAllConfigurations(applianceConfigurations);
			if (86400 / ((maxProgramDuration + 1) * actionCountPerDay) < 1 && actionCountPerDay > 1) {
				return 0;
				//we now have a run correction for this
//				throw new RuntimeException("Program duration to long for multiple runs per day");
			}

			// in 15 minutes steps only
			int stepSize = 900;
			maxDof = maxDof / stepSize;
			//deviate
			//E(X)=0.5*max=28800s=8h or E(X)=0.5*max=14400s=4h or similar
			BinomialDistribution binDistribution = new BinomialDistribution(maxDof, 0.5);
			double rand = randomGen.getNextDouble();
			int newValue = 0;
			for (int i = 0; i < maxDof; i++) {
				if (binDistribution.cumulativeProbability(i) > rand) {
					newValue = i;
					break;
				}
			}
			return Math.min(newValue * stepSize, maxPossibleDof);
		}

		return 0;
	}

	private long checkForBlockedSeconds() {
		long maxBlock = 0;

		// overlapping with PLANNED (only in simulation) action from this day 
		// (actions from the other day should be already scheduled and in actions...)
		Collection<SubjectAction> existingActions = getActions();
		Iterator<SubjectAction> it = existingActions.iterator();

		int tdof = 0;

		while(it.hasNext()) {
			SubjectAction a = it.next();

			if (a.getActionType() == ActionType.USER_ACTION) {

				tdof = Integer.parseInt(ActionParametersHelper.getValueForParameterOfParameters(
						a.getPerformAction().get(0), 
						"dof", 
						"tdof"));				
			} else if (a.getActionType() == ActionType.I_DEVICE_ACTION) {

				int selectedConfigurationID = Integer.parseInt(ActionParametersHelper.getValueForParameterOfParameters(
						a.getPerformAction().get(0), 
						"appliance", 
						"configuration"));

				int maxDur = XsdLoadProfilesHelperTool.getMaximumLengthOfOneConfiguration(
						applianceConfigurations.getApplianceProgramConfiguration().get(selectedConfigurationID));

				maxBlock = Math.max(maxBlock, a.getTick() + maxDur + tdof + 100);				
			}
			getGlobalLogger().logError("UUID=" +  getDeviceID() + " DeviceType=" + getDeviceType() + " SubjectActions still remaining at the start of the day");
		}

		// check if overlapping with scheduled/running action from the other day (day before)
		if (selectedStartingTimes != null) {
			int lastIndex = selectedStartingTimes.length - 1;
			long otherLast = 
					selectedStartingTimes[lastIndex] 
							+ applianceConfigurationProfile.getDynamicLoadProfiles()[selectedProfileID][lastIndex].getEndingTimeOfProfile() + 100;
			
			//running action could be rescheduled, so look for maximum length this could run
			if (this.isControllable()) {
				long lastPossibleEnd = this.configurationStartedAt + this.lastSet1sttDof 
						+ XsdLoadProfilesHelperTool.getMaximumLengthOfOneConfiguration(
								applianceConfigurations.getApplianceProgramConfiguration().get(selectedConfigurationID)) + 100;				
				otherLast = Math.max(lastPossibleEnd, otherLast);
			}			
			
			maxBlock = Math.max(maxBlock, otherLast);

		}

		return maxBlock;

	}


	// ### ESC STUFF ###

//	SEKR: Already done by superclass (DeviceSimulationDriver)
//	@Override
//	public EnumMap<Commodity, RealCommodityState> getCommodityOutputStates() {
//		EnumMap<Commodity, RealCommodityState> map = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
//		for (Commodity c : usedCommodities) {
//			if (c == Commodity.ACTIVEPOWER || c == Commodity.REACTIVEPOWER) {
//				RealElectricalCommodityState state = new RealElectricalCommodityState(
//						c, 
//						0.0 + getPower(c));
//				map.put(c, state);
//			}
//			else if (c == Commodity.NATURALGASPOWER || c == Commodity.HEATINGHOTWATERPOWER) {
//				RealThermalCommodityState state = new RealThermalCommodityState(
//						c, 
//						0.0 + getPower(c), 
//						0.0,
//						null);
//				map.put(c, state);
//			}
//			else {
//				getGlobalLogger().logError("Commodity " + c + " NOT IMPLEMENTED!");
//			}
//		}
//		return map;
//	}
//
//	@Override
//	public void setCommodityInputStates(
//			EnumMap<Commodity, RealCommodityState> inputStates,
////			EnumMap<AncillaryCommodity,AncillaryCommodityState> ancillaryInputStates) {
//			AncillaryMeterState ancillaryMeterState) {
//		this.commodityInputStates = inputStates;
//	}


	// ### GETTER ###

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
