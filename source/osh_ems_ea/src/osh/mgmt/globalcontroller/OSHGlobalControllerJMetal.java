package osh.mgmt.globalcontroller;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.GUIScheduleDebugExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.Fitness;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.esc.EnergyManagementProblem;
import osh.mgmt.globalcontroller.jmetal.esc.JMetalEnergySolverGA;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.simulation.DatabaseLoggerThread;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth, Sebastian Kramer
 *
 */
public class OSHGlobalControllerJMetal 
					extends GlobalController 
					implements IEventTypeReceiver, IHasState {

	private OSHGlobalObserver oshGlobalObserver;
	
	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals;
	private boolean newEpsPlsRecieved;
	
	private int epsOptimizationObjective;
	
	private int plsOptimizationObjective;
	private int varOptimizationObjective;
	private double upperOverlimitFactor;
	private double lowerOverlimitFactor;
	private long lasttimeSchedulingStarted;
	
	private OSHRandomGenerator optimizationMainRandomGenerator;
	private long optimizationMainRandomSeed;
	
	private GAParameters gaparameters;
	private String logDir;
	
	private int stepSize;
	private Boolean logGa;	

	UUID hotwaterTankID;
	
	
	/**
	 * CONSTRUCTOR
	 * @throws Exception 
	 */
	public OSHGlobalControllerJMetal(
			IOSHOC controllerbox,
			OSHParameterCollection configurationParameters,
			GAConfiguration gaConfiguration, OCEnergySimulationCore ocESC) throws Exception {
		super(controllerbox, configurationParameters, gaConfiguration, ocESC);
		
		this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
		this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
		try {
			this.gaparameters = new GAParameters(this.gaConfiguration);
		} catch (Exception ex) {
			getGlobalLogger().logError("Can't parse GAParameters, will shut down now!");
			throw ex;
		}
		
		try {
			this.upperOverlimitFactor = Double.valueOf(this.configurationParameters.getParameter("upperOverlimitFactor"));
		}
		catch (Exception e) {
			this.upperOverlimitFactor = 1.0;
			getGlobalLogger().logWarning("Can't get upperOverlimitFactor, using the default value: " + this.upperOverlimitFactor);
		}
		
		try {
			this.lowerOverlimitFactor = Double.valueOf(this.configurationParameters.getParameter("lowerOverlimitFactor"));
		}
		catch (Exception e) {
			this.lowerOverlimitFactor = 1.0;
			getGlobalLogger().logWarning("Can't get lowerOverlimitFactor, using the default value: " + this.lowerOverlimitFactor);
		}
		
		try {
			this.epsOptimizationObjective = Integer.valueOf(this.configurationParameters.getParameter("epsoptimizationobjective"));
		}
		catch (Exception e) {
			this.epsOptimizationObjective = 0;
			getGlobalLogger().logWarning("Can't get epsOptimizationObjective, using the default value: " + this.epsOptimizationObjective);
		}		
		
		try {
			this.plsOptimizationObjective = Integer.valueOf(this.configurationParameters.getParameter("plsoptimizationobjective"));
		}
		catch (Exception e) {
			this.plsOptimizationObjective = 0;
			getGlobalLogger().logWarning("Can't get plsOptimizationObjective, using the default value: " + this.plsOptimizationObjective);
		}
		
		try {
			this.varOptimizationObjective = Integer.valueOf(this.configurationParameters.getParameter("varoptimizationobjective"));
		}
		catch (Exception e) {
			this.varOptimizationObjective = 0;
			getGlobalLogger().logWarning("Can't get varOptimizationObjective, using the default value: " + this.varOptimizationObjective);
		}
		
		try {
			this.optimizationMainRandomSeed = Long.valueOf(this.configurationParameters.getParameter("optimizationMainRandomSeed"));
		} 
		catch (Exception e) {
			this.optimizationMainRandomSeed = 0xd1ce5bL;
			getGlobalLogger().logError("Can't get parameter optimizationMainRandomSeed, using the default value: " + this.optimizationMainRandomSeed);
		}
		this.optimizationMainRandomGenerator = new OSHRandomGenerator(new Random(optimizationMainRandomSeed));
		
		try {
			this.stepSize = Integer.valueOf(this.configurationParameters.getParameter("stepSize"));
		} 
		catch (Exception e) {
			this.stepSize = 60;
			getGlobalLogger().logError("Can't get parameter stepSize, using the default value: " + this.stepSize);
		}
		
		try {
			this.hotwaterTankID = UUID.fromString(this.configurationParameters.getParameter("hotWaterTankUUID"));
		} 
		catch (Exception e) {
			this.hotwaterTankID = UUID.fromString("00000000-0000-4857-4853-000000000000");
			getGlobalLogger().logError("Can't get parameter hotwaterTankUUID, using the default value: " + this.hotwaterTankID);
		}
		
		this.logDir = getOSH().getOSHstatus().getLogDir();
		
		getGlobalLogger().logDebug("Optimization StepSize = " + stepSize);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		// safety first...
		if ( getGlobalObserver() instanceof OSHGlobalObserver ) {
			this.oshGlobalObserver = (OSHGlobalObserver) getGlobalObserver();
		} 
		else {
			throw new OSHException("this global controller only works with global observers of type " + OSHGlobalObserver.class.getName());
		}
		
		this.getOSH().getTimer().registerComponent(this, 1);
//		
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), EpsStateExchange.class, RegistryType.COM, RegistryType.OC);
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), PlsStateExchange.class, RegistryType.COM, RegistryType.OC);
		
		this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
		this.getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
		
//		CostChecker.init(epsOptimizationObjective, plsOptimizationObjective, varOptimizationObjective, upperOverlimitFactor, lowerOverlimitFactor);
		
		this.lasttimeSchedulingStarted = getTimer().getUnixTimeAtStart() + 60;
	}
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();
		
		// shutting down threadpool
		OSH_gGAMultiThread.shutdown();
//		CostChecker.shutDown();
	}
	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T ex) throws OSHException {
		if (ex instanceof StateChangedExchange) {
			StateChangedExchange exsc = (StateChangedExchange) ex;
			newEpsPlsRecieved = true;
			
			if (exsc.getType().equals(EpsStateExchange.class)) {
				EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulentity());
				this.priceSignals = eee.getPriceSignals();
			}
			else if (exsc.getType().equals(PlsStateExchange.class)) {
				PlsStateExchange eee = this.getOCRegistry().getState(PlsStateExchange.class, exsc.getStatefulentity());
				this.powerLimitSignals = eee.getPowerLimitSignals();
			}
		}
		else {
			getGlobalLogger().logError("ERROR in " + this.getClass().getCanonicalName() + ": UNKNOWN EventExchange from UUID " + ex.getSender());
		}
	}

	
	@Override
	public void onNextTimePeriod() throws OSHException {
		
		long now = getTimer().getUnixTime();
		
		// check whether rescheduling is required and if so do rescheduling
		handleScheduling();
		
		// save current EPS and PLS to registry for logger
		{
			EpsPlsStateExchange epse = new EpsPlsStateExchange(
					getUUID(), 
					now, 
					this.priceSignals, 
					this.powerLimitSignals,
					this.epsOptimizationObjective,
					this.plsOptimizationObjective,
					this.varOptimizationObjective,
					this.upperOverlimitFactor,
					lowerOverlimitFactor,
					newEpsPlsRecieved);
			
			newEpsPlsRecieved = false;			
			
			this.getOCRegistry().setState(
					EpsPlsStateExchange.class,
					this,
					epse);
		}
		
	}
	
	/**
	 * decide if a (re-)scheduling is necessary
	 * @throws OSHException 
	 */
	private void handleScheduling() throws OSHException {
		
		boolean reschedulingRequired = false;
		
		//check if something has been changed:
		for (InterdependentProblemPart<?, ?> problemPart : getOshGlobalObserver().getProblemParts()) {
			if (problemPart.isToBeScheduled() && problemPart.getTimestamp() >= this.lasttimeSchedulingStarted) {
				reschedulingRequired = true;
			}
		}
		
		if (reschedulingRequired) {
			this.lasttimeSchedulingStarted = getTimer().getUnixTime();
			startScheduling();
		}

	}
	
	/**
	 * is triggered to 
	 * @throws OSHException 
	 */
	public void startScheduling() throws OSHException {
		
		if (this.ocESC == null) {
			throw new OSHException("OC-EnergySimulationCore not set, optimisation impossible, crashing now");
		}
		
		//retrieve information of ga should log to database
		if (logGa == null) {
			logGa = DatabaseLoggerThread.isLogGA();
			if (logGa) {
				OSH_gGAMultiThread.initLogging();
			};
		}

		EnumMap<AncillaryCommodity,PriceSignal> tempPriceSignals = new EnumMap<>(AncillaryCommodity.class);
		EnumMap<AncillaryCommodity,PowerLimitSignal> tempPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
		
		//TODO: Check if necessary to synchronize full object (this)
		//TODO: Check why keySet and not entrySet
		
		// Cloning necessary, because of possible price signal changes during optimization
		synchronized (priceSignals) {
			for (AncillaryCommodity vc : this.priceSignals.keySet()) {
				tempPriceSignals.put(vc, this.priceSignals.get(vc).clone());
			}
		}
		if ( tempPriceSignals.size() == 0 ) {
			getGlobalLogger().logError("No valid price signal available. Cancel scheduling!");
			return;
		}
		
		synchronized (powerLimitSignals) {
			for (AncillaryCommodity vc : this.powerLimitSignals.keySet()) {
				tempPowerLimitSignals.put(vc, this.powerLimitSignals.get(vc).clone());
			}
		}
		if ( tempPowerLimitSignals.size() == 0 ) {
			getGlobalLogger().logError("No valid power limit signal available. Cancel scheduling!");
			return;
		}
		
//		boolean showSolverDebugMessages = getControllerBoxStatus().getShowSolverDebugMessages();
		boolean showSolverDebugMessages = true;
		
		OSHRandomGenerator optimisationRunRandomGenerator = new OSHRandomGenerator(new Random(this.optimizationMainRandomGenerator.getNextLong()));
		
		// it is a good idea to use a specific random Generator for the EA, 
		// to make it comparable with other optimizers...
		JMetalEnergySolverGA solver = new JMetalEnergySolverGA(
				getGlobalLogger(), 
				optimisationRunRandomGenerator, 
				showSolverDebugMessages, 
				gaparameters,
				getTimer().getUnixTime(),
				stepSize,
				logDir);
		
		List<InterdependentProblemPart<?, ?>> problemparts = getOshGlobalObserver().getProblemParts();
		List<BitSet> solutions;
		SolutionWithFitness resultWithAll;
		
		if (!getOshGlobalObserver().getAndResetProblempartChangedFlag()) {
			return; //nothing new, return
		}
		
		// debug print
		getGlobalLogger().logDebug("=== scheduling... ===");
		long now = getTimer().getUnixTime();
		
		int[][] bitPositions = new int[problemparts.size()][2];
		int bitPosStart = 0;
		int bitPosEnd = 0;
		int counter = 0;
		long ignoreLoadProfileAfter = now;
		int usedBits = 0;
		
		long maxHorizon = now;
		
		for (InterdependentProblemPart<?, ?> problem : problemparts) {
			if (problem instanceof ControllableIPP<?, ?>) {
				maxHorizon = Math.max(((ControllableIPP<?, ?>) problem).getOptimizationHorizon(), maxHorizon);
			}			
		}

		for (InterdependentProblemPart<?, ?> problem : problemparts) {
			problem.recalculateEncoding(now, maxHorizon);
			problem.setId(counter);
			if (problem.getBitCount() > 0) {
				bitPosEnd = bitPosStart + problem.getBitCount();
			} else {
				bitPosEnd = bitPosStart;
			}
			bitPositions[counter] = new int[]{bitPosStart, bitPosEnd};
			counter++;
			bitPosStart += problem.getBitCount();
			usedBits += problem.getBitCount();
		}
		ignoreLoadProfileAfter = Math.max(ignoreLoadProfileAfter, maxHorizon);
		
		boolean hasGUI = this.getControllerBoxStatus().hasGUI();
		boolean isReal = !this.getControllerBoxStatus().isSimulation();
		
		
		try {
			IFitness fitnessFunction = new Fitness(
					this.getGlobalLogger(),
					this.epsOptimizationObjective,
					this.plsOptimizationObjective,
					this.varOptimizationObjective,
					this.upperOverlimitFactor,
					this.lowerOverlimitFactor);
			
				resultWithAll = solver.getSolution(
					problemparts,
					this.ocESC,
					bitPositions,
					tempPriceSignals,
					tempPowerLimitSignals,
					getTimer().getUnixTime(),
					fitnessFunction);
				solutions =  resultWithAll.getBitSet();
			
			
			if ((hasGUI || isReal) && usedBits != 0) {
				TreeMap<Long, Double> predictedTankTemp = new TreeMap<Long, Double>();
				TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<Long, Double>();
				TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<Long, Double>();
				List<Schedule> schedules = new ArrayList<Schedule>();
				AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();
				
				EnergyManagementProblem debugProblem = new EnergyManagementProblem(
						problemparts, this.ocESC, bitPositions, priceSignals,
						powerLimitSignals, now, ignoreLoadProfileAfter,
						optimisationRunRandomGenerator, getGlobalLogger(), fitnessFunction, stepSize);
				
				debugProblem.evaluateWithDebuggingInformation(
						resultWithAll.getFullSet(),
						ancillaryMeter,
						predictedTankTemp, 
						predictedHotWaterDemand, 
						predictedHotWaterSupply, 
						schedules, 
						true, 
						hotwaterTankID);
				
				//better be sure
				debugProblem.finalizeGrids();
				
				this.getOCRegistry().setState(
						GUIHotWaterPredictionStateExchange.class,
						this,
						new GUIHotWaterPredictionStateExchange(getUUID(),
								getTimer().getUnixTime(), predictedTankTemp, predictedHotWaterDemand, predictedHotWaterSupply));
				
				this.getOCRegistry().setState(
						GUIAncillaryMeterStateExchange.class, 
						this, 
						new GUIAncillaryMeterStateExchange(getUUID(), getTimer().getUnixTime(), ancillaryMeter));
			
				//sending schedules last so the wait command has all the other things (waterPred, Ancillarymeter) first
				// Send current Schedule to GUI (via Registry to Com)
				this.getOCRegistry().setState(
						GUIScheduleStateExchange.class,
						this,
						new GUIScheduleStateExchange(getUUID(), getTimer()
								.getUnixTime(), schedules, stepSize));
			
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			getGlobalLogger().logError(e);
			return;
		}
		

		int min = Math.min(solutions.size(), problemparts.size());
		if (solutions.size() != problemparts.size()) {
			getGlobalLogger().logDebug("jmetal: problem list and solution list don't have the same size");
		}
		
		GUIScheduleDebugExchange debug = new GUIScheduleDebugExchange(getUUID(), getTimer().getUnixTime());
		
		for (int i = 0; i < min; i++) {
			InterdependentProblemPart<?, ?> part = problemparts.get(i);
			LocalController lc = getLocalController(part.getDeviceID());
			BitSet bits = solutions.get(i);

			if (lc != null) {
					this.getOCRegistry().sendCommand(
							EASolutionCommandExchange.class, 
							part.transformToFinalInterdependetPhenotype(
									null, 
									part.getDeviceID(), 
									getTimer().getUnixTime(),
									bits));
			} 
			else if (/* lc == null && */ part.getBitCount() > 0) {
				throw new NullPointerException("got a local part with used bits but without controller! (UUID: " + part.getDeviceID() + ")");
			}
//			this sends a prediction of the waterTemperatures to the waterTankObserver, so the waterTank can trigger a reschedule
//			when the actual temperatures are too different to the prediction
			if (part.transformToFinalInterdependetPrediction(bits) != null) {
				this.getOCRegistry().sendCommand(
						EAPredictionCommandExchange.class, 
						part.transformToFinalInterdependetPrediction(
								null, 
								part.getDeviceID(), 
								getTimer().getUnixTime(),
								bits));
			}
			
			if (hasGUI) {
				StringBuilder debugstr = new StringBuilder();
				debugstr.append(getTimer().getUnixTime() + ";");
				debugstr.append(part.getSender() + ";");
				debugstr.append(part.problemToString() + ";");
				if (part instanceof ControllableIPP<?, ?>) {
					debugstr.append(((ControllableIPP<?, ?>) part)
							.solutionToString(bits));
				}
				debug.addString(part.getSender(), debugstr.toString());
			}
		}
		
		if (hasGUI && usedBits != 0)
			getOCRegistry().sendEvent(GUIScheduleDebugExchange.class, debug);
		
		getGlobalLogger().logDebug("===    EA done    ===");
		
		//lasttimeScheduled = getTimer().getUnixTime();
	}	

	@Override
	public UUID getUUID() {
		return getGlobalObserver().getAssignedOCUnit().getUnitID();
	}
	
	public OSHGlobalObserver getOshGlobalObserver() {
		return oshGlobalObserver;
	}

}

