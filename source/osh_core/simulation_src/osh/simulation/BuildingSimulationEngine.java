package osh.simulation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.ConfigurationParameter;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.esc.exception.EnergySimulationException;
import osh.simulation.energy.IDeviceEnergySubject;
import osh.simulation.energy.SimEnergySimulationCore;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.Screenplay;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.CostCalculator;
import osh.utils.string.StringConversions;
import osh.utils.time.TimeConversion;
import osh.utils.xml.XMLSerialization;

/**
 * Simulation engine for the smart-home-lab
 * 
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class BuildingSimulationEngine extends SimulationEngine {

	private ScreenplayType screenplayType = null;
	private UUID entityUUID;

	// Simulation Subjects
	private ArrayList<ISimulationSubject> simSubjectsList;
	private HashMap<UUID, ISimulationSubject> simSubjectsMap;

	private OSHParameterCollection engineParameters;

	//logging intervals
	List<Long[]> loggingIntervals = new ArrayList<Long[]>();
	private Long[] timeStampForIntervall;
	private long[] relativeIntervallStart;
	private boolean databaseLogging;
	private boolean logDetailedPower;
	private boolean logEpsPls;
	private boolean logH0;
	private boolean logIntervalls;
	private boolean logDevices;
	private boolean logHotWater;
	private boolean logWaterTank;
	private boolean logGA;
	private boolean logSmartHeater;
	private OSHSimulationResults[] intervallResults;

	//saved EPS and PLS
	private EnumMap<AncillaryCommodity, PriceSignal> priceSignals = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);
	private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimits = new EnumMap<AncillaryCommodity, PowerLimitSignal>(AncillaryCommodity.class);

	//saved power
	private AncillaryCommodityLoadProfile loadProfile = new AncillaryCommodityLoadProfile();

	//saved array for H0
	private double[][][] aggrH0ResultsWeekdays = new double[7][1440][2];
	private int[][] h0ResultsCounter = new int[7][1440];
	private double[][] aggrH0ResultsDays = new double[365][2];
	private int[] h0ResultsCounterDays = new int[365];
	private double aggrActiveConsumption = 0.0;
	private double aggrReactiveConsumption = 0.0;

	// Energy Simulation Core
	private SimEnergySimulationCore energySimulationCore;
	// ESC Subjects
	private ArrayList<IDeviceEnergySubject> energySimSubjectsList;
	private HashMap<UUID, IDeviceEnergySubject> energySimSubjectsMap;

	private PrintWriter powerWriter;

	public double currentActivePower = 0;
	public double currentReactivePower = 0;


	/**
	 * CONSTRUCTOR<br>
	 * constructor with a given array of devices to simulate...yes everything is a device!
	 * @param deviceList
	 * @param simlogger 
	 */
	public BuildingSimulationEngine(
			ArrayList<? extends OSHComponent> deviceList,
			List<ConfigurationParameter> engineParameters,
			SimEnergySimulationCore esc,
			ScreenplayType screenplayType,
			ISimulationActionLogger simlogger,
			PrintWriter powerWriter,
			UUID entityUUID) throws SimulationEngineException {

		//LOGGING
		this.oshSimulationResults = new OSHSimulationResults();

		this.energySimulationCore = esc;

		this.screenplayType = screenplayType;

		this.simSubjectsList = new ArrayList<ISimulationSubject>();
		this.simSubjectsMap = new HashMap<UUID, ISimulationSubject>();

		this.energySimSubjectsList = new ArrayList<IDeviceEnergySubject>();
		this.energySimSubjectsMap = new HashMap<UUID, IDeviceEnergySubject>();

		// get simulation subjects
		try {
			for ( OSHComponent _driver : deviceList ) {
				if ( _driver instanceof ISimulationSubject ) {
					ISimulationSubject _simSubj = (ISimulationSubject) _driver;

					//assign the simulation engine
					_simSubj.setSimulationEngine(this);

					//assign logger
					_simSubj.setSimulationActionLogger(simlogger);

					//add subject
					this.simSubjectsList.add(_simSubj);

					//do the same for the HashMap (better direct Access)
					this.simSubjectsMap.put(_simSubj.getDeviceID(),_simSubj);			
				}
			}	
		}
		catch (Exception ex){
			throw new SimulationEngineException(ex);
		}


		// get ESC simulation subjects
		try {
			for ( OSHComponent _driver : deviceList ) {
				if ( _driver instanceof IDeviceEnergySubject) {
					IDeviceEnergySubject _simSubj = (IDeviceEnergySubject) _driver;

					//add subject
					this.energySimSubjectsList.add(_simSubj);

					//do the same for the HashMap (better direct Access)
					this.energySimSubjectsMap.put(_simSubj.getDeviceID(),_simSubj);	
				}
			}	
		}
		catch (Exception ex){
			throw new SimulationEngineException(ex);
		}

		this.engineParameters = new OSHParameterCollection();
		this.engineParameters.loadCollection(engineParameters);

		this.entityUUID = entityUUID; 

		try {
			logH0 = Boolean.parseBoolean(this.engineParameters.getParameter("logH0"));
		} 
		catch (Exception e) {
			logH0 = false;
		}

		try {
			logIntervalls = Boolean.parseBoolean(this.engineParameters.getParameter("logIntervalls"));
		} 
		catch (Exception e) {
			logIntervalls = false;
		}

		try {
			logDevices = Boolean.parseBoolean(this.engineParameters.getParameter("logDevices"));
		} 
		catch (Exception e) {
			logDevices = false;
		}

		try {
			logHotWater = Boolean.parseBoolean(this.engineParameters.getParameter("logHotWater"));
		} 
		catch (Exception e) {
			logHotWater = false;
		}

		try {
			logEpsPls = Boolean.parseBoolean(this.engineParameters.getParameter("logEpsPls"));
		} 
		catch (Exception e) {
			logEpsPls = false;
		}

		try {
			logDetailedPower = Boolean.parseBoolean(this.engineParameters.getParameter("logDetailedPower"));
		} 
		catch (Exception e) {
			logDetailedPower = false;
		}

		try {
			logWaterTank = Boolean.parseBoolean(this.engineParameters.getParameter("logWaterTank"));
		} 
		catch (Exception e) {
			logWaterTank = false;
		}

		try {
			logGA = Boolean.parseBoolean(this.engineParameters.getParameter("logGA"));
		} 
		catch (Exception e) {
			logGA = false;
		}

		try {
			logSmartHeater = Boolean.parseBoolean(this.engineParameters.getParameter("logSmartHeater"));
		} 
		catch (Exception e) {
			logSmartHeater = false;
		}



		String loggingIntervallsAsArray = null;

		try {
			loggingIntervallsAsArray = this.engineParameters.getParameter("loggingIntervalls");
		} 
		catch (Exception e) {
		}

		if (loggingIntervallsAsArray != null && loggingIntervallsAsArray.length() > 2) {
			Long[][] tmp = StringConversions.fromStringTo2DimLongArray(loggingIntervallsAsArray);
			for (Long[] intervall : tmp)
				loggingIntervals.add(intervall);
		}



		this.powerWriter = powerWriter;

		if (logH0) {
			for (int i = 0; i < aggrH0ResultsWeekdays.length; i++) {
				for (int j = 0; j < aggrH0ResultsWeekdays[i].length; j++) {
					h0ResultsCounter[i][j] = 0;
					for (int k = 0; k < aggrH0ResultsWeekdays[i][j].length; k++)
						aggrH0ResultsWeekdays[i][j][k] = 0.0;				
				}
			}
			for (int i = 0; i < aggrH0ResultsDays.length; i++) {
				for (int j = 0; j < aggrH0ResultsDays[i].length; j++) {
					aggrH0ResultsDays[i][j] = 0.0;
				}			
				h0ResultsCounterDays[i] = 0;
			}
		}


	}


	// ### SCREENPLAY LEGACY CODE ###
	// INFO: currently not used

	/**
	 * load the actions for the devices from a screenplay-object for a timespan
	 * @param currentScreenplay
	 */
	public void loadSingleScreenplay(Screenplay currentScreenplay){
		for ( ISimulationSubject _simSubj : simSubjectsList ){
			for(int i = 0; i < currentScreenplay.getSIMActions().size(); i++){
				//Search for an action for a specific device
				if (currentScreenplay.getSIMActions().get(i).getDeviceID().compareTo(_simSubj.getDeviceID().toString()) == 0) {
					_simSubj.setAction(currentScreenplay.getSIMActions().get(i));
				}
			}
		}
	}

	/**
	 * @param load the actions for the devices for a timespan or a cycle from a file
	 */
	public void loadSingleScreenplayFromFile(String screenPlaySource) throws SimulationEngineException{
		Screenplay currentScreenplaySet;
		try {
			currentScreenplaySet = (Screenplay) XMLSerialization.file2Unmarshal(screenPlaySource, Screenplay.class);
		}
		catch (Exception ex){
			currentScreenplaySet = null;
			throw new SimulationEngineException(ex);
		}
		this.loadSingleScreenplay(currentScreenplaySet);
	}


	/**
	 * simulate the next timeTick, increment the real-time driver 
	 * @param currentTick
	 * @throws SimulationEngineException
	 */
	@Override
	public void simulateNextTimeTick(long currentTick) throws SimulationEngineException {

		//		Map<AncillaryCommodity,AncillaryCommodityState> ancillaryMeterState;
		AncillaryMeterState ancillaryMeterState;

		// #1 EnergySimulation
		try {
			ancillaryMeterState = energySimulationCore.doNextEnergySimulation(energySimSubjectsList);
		}
		catch (EnergySimulationException ex) {
			throw new SimulationEngineException(ex);
		}

		// #2 Notify the Subject that the next Simulation Tick begins
		//    Simulation Pre-tick Hook
		try {
			for (ISimulationSubject _simSubject : simSubjectsList) {
				_simSubject.onSimulationPreTickHook();
			}
		} 
		catch (SimulationSubjectException ex){
			throw new SimulationEngineException(ex);
		}

		// #3 DeviceSimulation (the Tick)
		try {
			for (ISimulationSubject _simSubject : simSubjectsList) {
				_simSubject.triggerSubject();
			}
		} 
		catch (SimulationSubjectException ex){
			throw new SimulationEngineException(ex);
		}

		// #4 Notify the Subject that the current Simulation Tick ended
		//	  Simulation Post-tick Hook
		try {
			for (ISimulationSubject _simSubject : simSubjectsList) {
				_simSubject.onSimulationPostTickHook();
			}
		} 
		catch (SimulationSubjectException ex){
			throw new SimulationEngineException(ex);
		}

		// TEMP LOGGING
		logTick(ancillaryMeterState, currentTick);
	}

	@Override
	protected void notifyLocalEngineOnSimulationIsUp() throws SimulationEngineException {
		try {
			for (ISimulationSubject simulationSubject: this.simSubjectsList) {
				simulationSubject.onSimulationIsUp();
			}
		} 
		catch (SimulationSubjectException ex ) {
			throw new SimulationEngineException(ex);
		}

	}


	// ### GETTERS ###

	/**
	 * get a simulationSubject by his (device) ID.
	 * This can be called from another subject to get an appending subject
	 * @param subjectID
	 * @return
	 */
	protected ISimulationSubject getSimulationSubjectByID(UUID subjectID){
		ISimulationSubject _simSubj = null;
		_simSubj = simSubjectsMap.get(subjectID);
		return _simSubj;
	}

	public ScreenplayType getScreenplayType() {
		return screenplayType;
	}

	public void setDatabaseLogging() {
		this.databaseLogging = true;

		this.intervallResults = new OSHSimulationResults[this.loggingIntervals.size()];
		this.timeStampForIntervall = new Long[this.loggingIntervals.size()];
		this.relativeIntervallStart = new long[this.loggingIntervals.size()];
		for (int i = 0; i < this.loggingIntervals.size(); i++) {
			this.intervallResults[i] = new OSHSimulationResults();
			this.timeStampForIntervall[i] = null;
			this.relativeIntervallStart[i] = 0;
		}

		DatabaseLoggerThread.setLogDevices(logDevices);
		DatabaseLoggerThread.setLogHotWater(logHotWater);
		DatabaseLoggerThread.setLogWaterTank(logWaterTank);
		DatabaseLoggerThread.setLogGA(logGA);
		DatabaseLoggerThread.setLogSmartHeater(logSmartHeater);
	}


	// ### GETTERS ###
	//NONE

	// LOGGING

	private void logTick(
			AncillaryMeterState ancillaryMeterState,
			long currentTick) {

		// GET EPS and PLS FROM REGISTRY
		EpsPlsStateExchange epse = this.ocRegistry.getState(
				EpsPlsStateExchange.class, 
				UUID.fromString("e5ad4b36-d417-4be6-a1c8-c3ad68e52977"));

		if (ancillaryMeterState != null
				&& epse != null) {

			double currentActivePowerConsumption = 0;

			double currentActivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
			if (currentActivePowerExternal > 0) {
				currentActivePowerConsumption = currentActivePowerExternal;
			}

			double currentActivePowerPvAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
			double currentActivePowerPvFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN);
			
			double currentActivePowerPv = currentActivePowerPvFeedIn + currentActivePowerPvAutoConsumption;
			currentActivePowerConsumption = currentActivePowerConsumption + Math.abs(currentActivePowerPvAutoConsumption);

			
			double currentActivePowerChpAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
			double currentActivePowerChpFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);

			double currentActivePowerChp = currentActivePowerChpFeedIn + currentActivePowerChpAutoConsumption;
			currentActivePowerConsumption = currentActivePowerConsumption + Math.abs(currentActivePowerChpAutoConsumption);

			
			double currentActivePowerBatteryCharging = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION);
			double currentActivePowerBatteryAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION);
			double currentActivePowerBatteryFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN);
			
			double currentActivePowerBatteryDischarging = currentActivePowerBatteryFeedIn + currentActivePowerBatteryAutoConsumption;
			
			//if battery is charging it's power is already contained in the activePowerExternal
			if (currentActivePowerBatteryAutoConsumption < 0) 
				currentActivePowerConsumption = currentActivePowerConsumption + Math.abs(currentActivePowerBatteryAutoConsumption);

			double currentReactivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

//			if (currentReactivePowerExternal != 0) {
//				@SuppressWarnings("unused")
//				int xxx = 0;
//			}

			double currentGasPowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL);

			long currentTime = timerdriver.getUnixTime();

			/* array
			 * [0] = epsCosts
			 * [1] = plsCosts
			 * [2] = gasCosts
			 * [3] = feedInCompensationPV
			 * [4] = feedInCompensationCHP
			 * [5] = autoConsumptionCosts
			 */
			double[] costs = CostCalculator.calcSingularCosts(
					epse.getEpsOptimizationObjective(), 
					epse.getVarOptimizationObjective(), 
					epse.getPlsOptimizationObjective(), 
					currentTime, 
					1, 
					epse.getPlsUpperOverlimitFactor(), 
					epse.getPlsLowerOverlimitFactor(), 
					ancillaryMeterState, 
					epse.getPs(), 
					epse.getPwrLimit());

			if (oshSimulationResults != null
					&& oshSimulationResults instanceof OSHSimulationResults) {
				((OSHSimulationResults) oshSimulationResults).addActivePowerConsumption(
						currentActivePowerConsumption);

				((OSHSimulationResults) oshSimulationResults).addActivePowerPV(
						currentActivePowerPv);
				((OSHSimulationResults) oshSimulationResults).addActivePowerPVAutoConsumption(
						currentActivePowerPvAutoConsumption);
				((OSHSimulationResults) oshSimulationResults).addActivePowerPVFeedIn(
						currentActivePowerPvFeedIn);

				((OSHSimulationResults) oshSimulationResults).addActivePowerCHP(
						currentActivePowerChp);
				((OSHSimulationResults) oshSimulationResults).addActivePowerCHPAutoConsumption(
						currentActivePowerChpAutoConsumption);
				((OSHSimulationResults) oshSimulationResults).addActivePowerCHPFeedIn(
						currentActivePowerChpFeedIn);

				((OSHSimulationResults) oshSimulationResults).addActivePowerBatteryCharging(
						currentActivePowerBatteryCharging);
				((OSHSimulationResults) oshSimulationResults).addActivePowerBatteryDischarging(
						currentActivePowerBatteryDischarging);
				((OSHSimulationResults) oshSimulationResults).addActivePowerBatteryAutoConsumption(
						currentActivePowerBatteryAutoConsumption);
				((OSHSimulationResults) oshSimulationResults).addActivePowerBatteryFeedIn(
						currentActivePowerBatteryFeedIn);

				((OSHSimulationResults) oshSimulationResults).addActivePowerExternal(
						currentActivePowerExternal);

				((OSHSimulationResults) oshSimulationResults).addReactivePowerExternal(
						currentReactivePowerExternal);

				((OSHSimulationResults) oshSimulationResults).addGasPowerExternal(
						currentGasPowerExternal);

				((OSHSimulationResults) oshSimulationResults).addEpsCostsToEpsCosts(costs[0]);
				((OSHSimulationResults) oshSimulationResults).addPlsCostsToPlsCosts(costs[1]);
				((OSHSimulationResults) oshSimulationResults).addCostsToTotalCosts(costs[0] + costs[1] + costs[2]);
				((OSHSimulationResults) oshSimulationResults).addGasCostsToGasCosts(costs[2]);
				((OSHSimulationResults) oshSimulationResults).addFeedInCostsToFeedInCostsPV(costs[3]);
				((OSHSimulationResults) oshSimulationResults).addFeedInCostsToFeedInCostsCHP(costs[4]);
				((OSHSimulationResults) oshSimulationResults).addAutoConsumptionCostsToAutoConsumptionCosts(costs[5]);
				// GAS COSTS
			}

			this.currentActivePower = currentActivePowerExternal;
			this.currentReactivePower = currentReactivePowerExternal;

			if (databaseLogging && currentTick > 1 && logIntervalls) {
				//interval logging of values
				if (!loggingIntervals.isEmpty()) {
					for (int i = 0; i < loggingIntervals.size(); i++) {
						//set up the next timestamps at start of the simulation
						if (timeStampForIntervall[i] == null) {
							Long[] intervall = loggingIntervals.get(i);
							if (intervall[0] > 0) {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthMonth(currentTime, intervall[0]);
							} else if (intervall[1] > 0) {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthWeek(currentTime, intervall[1]);
							} else {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthDayAfterToday(currentTime, intervall[2]);
							}
						} else if (timeStampForIntervall[i] <= currentTime) {
							Long[] intervall = loggingIntervals.get(i);
							OSHSimulationResults newBase = ((OSHSimulationResults) oshSimulationResults).clone();
							OSHSimulationResults toLog = intervallResults[i];
							intervallResults[i] = newBase;
							toLog.generateDiffToOtherResult((OSHSimulationResults) oshSimulationResults);
							DatabaseLoggerThread.enqueueSimResults(toLog, relativeIntervallStart[i], currentTick);

							relativeIntervallStart[i] = currentTick + 1;
							if (intervall[0] > 0) {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthMonth(currentTime, intervall[0]);
							} else if (intervall[1] > 0) {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthWeek(currentTime, intervall[1]);
							} else {
								timeStampForIntervall[i] = TimeConversion.getStartOfXthDayAfterToday(currentTime, intervall[2]);
							}							
						}
					}
				}
			}

			if (databaseLogging && logEpsPls) {
				//new eps/pls, so save the past and update the saved one
				if (epse.isEpsPlsChanged()) {
					//handle eps
					Map<AncillaryCommodity, PriceSignal> toLogEps = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);
					for (Entry<AncillaryCommodity, PriceSignal> en : epse.getPs().entrySet()) {
						PriceSignal oldPs = priceSignals.get(en.getKey());

						if (oldPs == null) {
							priceSignals.put(en.getKey(), en.getValue().clone());
						} else {
							oldPs.extendAndOverride(en.getValue().clone());							
							//log past
							toLogEps.put(en.getKey(), oldPs.cloneBefore(currentTime));
							//only keep the future
							PriceSignal nowAndFuture = oldPs.cloneAfter(currentTime);
							priceSignals.put(en.getKey(), nowAndFuture);
						}																	
					}
					DatabaseLoggerThread.enqueueEps(toLogEps);	

					//handle pls
					Map<AncillaryCommodity, PowerLimitSignal> toLogPls = new EnumMap<AncillaryCommodity, PowerLimitSignal>(AncillaryCommodity.class);
					for (Entry<AncillaryCommodity, PowerLimitSignal> en : epse.getPwrLimit().entrySet()) {
						PowerLimitSignal oldPls = powerLimits.get(en.getKey());

						if (oldPls == null) {
							powerLimits.put(en.getKey(), en.getValue().clone());	
						} else {
							oldPls.extendAndOverride(en.getValue().clone());							
							//log past
							toLogPls.put(en.getKey(), oldPls.cloneBefore(currentTime));
							//only keep the future
							PowerLimitSignal nowAndFuture = oldPls.cloneAfter(currentTime);
							powerLimits.put(en.getKey(), nowAndFuture);		
						}														
					}
					DatabaseLoggerThread.enqueuePls(toLogPls);		
				}
			}

			if (databaseLogging && logDetailedPower) {

				loadProfile.setLoad(AncillaryCommodity.ACTIVEPOWEREXTERNAL, currentTick, (int) Math.round(currentActivePowerExternal));
				loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, currentTick, (int) Math.round(currentActivePowerChpAutoConsumption));
				loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, currentTick, (int) Math.round(currentActivePowerChpFeedIn));
				loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, currentTick, (int) Math.round(currentActivePowerPvAutoConsumption));
				loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERFEEDIN, currentTick, (int) Math.round(currentActivePowerPvFeedIn));

				if (currentTick % 43200 == 0) {
					AncillaryCommodityLoadProfile tmpLoadProfile = loadProfile;
					loadProfile = tmpLoadProfile.cloneAfter(currentTick);

					tmpLoadProfile = tmpLoadProfile.getProfileWithoutDuplicateValues();
					DatabaseLoggerThread.enqueueDetailedPower(tmpLoadProfile);					
				}				
			}

			if (databaseLogging && logH0) {
				aggrActiveConsumption += currentActivePowerConsumption;
				aggrReactiveConsumption += currentReactivePowerExternal;


				if (currentTick % 60 == 0) {
					int dofWeek = TimeConversion.convertUnixTime2CorrectedWeekdayInt(currentTime);
					int dOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(currentTime);
					aggrH0ResultsWeekdays[dofWeek][(int) ((currentTick / 60) % 1440)][0] += aggrActiveConsumption;
					aggrH0ResultsWeekdays[dofWeek][(int) ((currentTick / 60) % 1440)][1] += aggrReactiveConsumption;
					h0ResultsCounter[dofWeek][(int) ((currentTick / 60) % 1440)]++;
					aggrH0ResultsDays[dOfYear % 365][0] += aggrActiveConsumption;
					aggrH0ResultsDays[dOfYear % 365][1] += aggrReactiveConsumption;
					h0ResultsCounterDays[dOfYear % 365]++;

					aggrActiveConsumption = 0;
					aggrReactiveConsumption = 0;
				}				
			}

			//System.out.println("currentActivePower: " + ((OSHSimulationResults)oshSimulationResults).activePowerExternal);
			//System.out.println("currentReactivePower: " + currentReactivePowerExternal);

			// MINUTEWISE POWER LOGGER
			if (currentTick % 60 == 0
					&& powerWriter != null) {
				powerWriter.println(currentTick 
						+ ";" + currentActivePowerConsumption 
						+ ";" + currentActivePowerPv 
						+ ";" + currentActivePowerPvAutoConsumption 
						+ ";" + currentActivePowerPvFeedIn 
						+ ";" + currentActivePowerChp 
						+ ";" + currentActivePowerChpAutoConsumption 
						+ ";" + currentActivePowerChpFeedIn
						+ ";" + currentActivePowerBatteryCharging
						+ ";" + currentActivePowerBatteryDischarging 
						+ ";" + currentActivePowerBatteryAutoConsumption 
						+ ";" + currentActivePowerBatteryFeedIn 
						+ ";" + currentActivePowerExternal 
						+ ";" + currentReactivePowerExternal 
						+ ";" + currentGasPowerExternal 
						+ ";" + costs[0] 
						+ ";" + costs[1] 
						+ ";" + costs[2]
						+ ";" + costs[3]
						+ ";" + costs[4]
						+ ";" + costs[5]
						+ ";" + epse.getPs().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPrice(currentTime));
			}

			//			if (comRegistry != null) {
			//				if (entityUUID != null) {
			//					System.out.println("SimEngine Daten abschicken");
			//					System.out.println(entityUUID);
			//					comRegistry.setStateOfSender(BuildingStateExchange.class,
			//							new BuildingStateExchange(entityUUID, currentTick, currentTime, currentActivePower,
			//									currentActivePowerConsumption, currentActivePowerChp, currentActivePowerChpFeedIn,
			//									currentActivePowerChpAutoConsumption, currentActivePowerPv,
			//									currentActivePowerPvFeedIn, currentActivePowerPvAutoConsumption,
			//									currentActivePowerBatteryCharging, currentActivePowerBatteryDischarging,
			//									currentActivePowerBatteryAutoConsumption, currentActivePowerBatteryFeedIn,
			//									currentActivePowerExternal, currentReactivePowerExternal, currentGasPowerExternal));
			//
			//				}
			//			}

		}
		else {
			//			System.out.println("ERROR");
		}
	}

	public void shutdown() {
		if (databaseLogging) {
			if (loggingIntervals != null && logIntervalls) {
				DatabaseLoggerThread.enqueueEps(priceSignals);	
				DatabaseLoggerThread.enqueuePls(powerLimits);
			}
			if (logH0) {
				double[][][] avgWeekDays = new double[7][1440][2];
				double[][] avgDays = new double[365][2];

				for (int i = 0; i < avgWeekDays.length; i++) {
					for (int j = 0; j < avgWeekDays[i].length; j++) {
						double factor = (double) h0ResultsCounter[i][j] * 3600000.0;
						for (int k = 0; k < avgWeekDays[i][j].length; k++) {
							avgWeekDays[i][j][k] = aggrH0ResultsWeekdays[i][j][k] / factor;
						}
					}
				}
				for (int i = 0; i < avgDays.length; i++) {
					double factor = ((double) h0ResultsCounterDays[i] / 1440.0) * 3600000.0;
					for (int j = 0; j < avgDays[i].length; j++) {
						avgDays[i][j] = aggrH0ResultsDays[i][j] / factor;
					}				
				}			

				DatabaseLoggerThread.enqueueH0(avgWeekDays, avgDays);
			}
		}
	}

}
