package osh.mgmt.globalcontroller.jmetal.esc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.esc.IOCEnergySubject;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.OCEnergySimulationCore;
import osh.esc.UUIDCommodityMap;
import osh.esc.exception.EnergySimulationException;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.simulation.exception.SimulationEngineException;
import osh.utils.DeepCopy;

/**
 * Problem to be solved by solver / optimizer
 * 
 * @author Ingo Mauser, Sebastian Kramer
 */
public class EnergyManagementProblem extends Problem {

	private final int STEP_SIZE;

	private static final long serialVersionUID = 1L;	

	private IFitness fitnessfunction;

	// active nodes, need information about commodity input states (new IPP)
	private List<InterdependentProblemPart<?, ?>> activeNeedsInput;
	// active nodes
	private List<InterdependentProblemPart<?, ?>> activeWorksAlone;
	// passive nodes
	private List<InterdependentProblemPart<?, ?>> passive;
	// static PP
	private List<InterdependentProblemPart<?, ?>> staticParts;

	int[][] bitPositions;

	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals;

	private long ignoreLoadProfileBefore;
	private long ignoreLoadProfileAfter;

	private boolean multiThreading = false;
	private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedActiveNeedsInput = null;
	private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedActiveWorksAlone = null;
	private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedPassive = null;
	private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedStatic = null;
	private ObjectArrayList<OCEnergySimulationCore> multiOCs = null;

	private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesActiveNI;
	private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesActiveWA;
	private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesPassive;
	private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesStatic;
	
	Set<UUID> passiveUUIDs = new HashSet<UUID>();	
	Set<UUID> activeNeedInputUUIDs = new HashSet<UUID>();

	private Long maxReferenceTime;
	private Long maxOptimizationHorizon;
	
	private boolean keepPrediction = false;

	private OCEnergySimulationCore ocEnergySimulationCore;
	
	Object2IntOpenHashMap<UUID> uuidIntMap;
	

	
	/**
	 * CONSTRUCTOR
	 * @throws ClassNotFoundException
	 */
	public EnergyManagementProblem(
			List<InterdependentProblemPart<?, ?>> problemparts,
			OCEnergySimulationCore ocESC,
			int[][] bitPositions,
			EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
			long ignoreLoadProfileBefore, 
			long ignoreLoadProfileAfter,
			OSHRandomGenerator randomGenerator, 
			IGlobalLogger globalLogger,
			IFitness fitnessFunction,
			int STEP_SIZE) 
					throws ClassNotFoundException {
		super(new PseudoRandom(randomGenerator));

		this.bitPositions = bitPositions;

		this.activeNeedsInput = new ArrayList<InterdependentProblemPart<?, ?>>();
		this.activeWorksAlone = new ArrayList<InterdependentProblemPart<?, ?>>();
		this.passive = new ArrayList<InterdependentProblemPart<?, ?>>();
		this.staticParts = new ArrayList<InterdependentProblemPart<?, ?>>();

		this.priceSignals = priceSignals;
		this.powerLimitSignals = powerLimitSignals;

		this.ignoreLoadProfileBefore = ignoreLoadProfileBefore;
		this.ignoreLoadProfileAfter = ignoreLoadProfileAfter;

		this.STEP_SIZE = STEP_SIZE;

		if (ignoreLoadProfileBefore == ignoreLoadProfileAfter) {
			//TODO remove load profile from problem
		}

		// ENERGY SIMULATION

		// create EnergySimulationCore
		this.ocEnergySimulationCore = ocESC;


		this.fitnessfunction = fitnessFunction;
		int numberOfBits = 0;

		Set<UUID> allUUIDs = new HashSet<UUID>();
		Set<UUID> activeUUIDs = new HashSet<UUID>();
		Set<UUID> passiveUUIDs = new HashSet<UUID>();
		
		uuidIntMap = new Object2IntOpenHashMap<UUID>(problemparts.size());
		uuidIntMap.defaultReturnValue(-1);
		
		Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap = new Object2ObjectOpenHashMap<UUID, Commodity[]>(problemparts.size());
		Object2ObjectOpenHashMap<UUID, Commodity[]> uuidInputMap = new Object2ObjectOpenHashMap<UUID, Commodity[]>(problemparts.size());
		
		for (InterdependentProblemPart<?, ?> part : problemparts) {
			if (uuidIntMap.put(part.getDeviceID(), part.getId()) != -1) {
				throw new IllegalArgumentException("multiple IPPs with same UUID");
			}
			if (!part.isCompletelyStatic()) {
				allUUIDs.add(part.getDeviceID());				
				numberOfBits += part.getBitCount();
				uuidOutputMap.put(part.getDeviceID(), part.getAllOutputcommodities());
				uuidInputMap.put(part.getDeviceID(), part.getAllInputcommodities());
			}
		}

		ocEnergySimulationCore.splitActivePassive(allUUIDs, activeUUIDs, passiveUUIDs);

		//split parts in 4 lists
		// calc maxReferenceTime of parts
		for (InterdependentProblemPart<?, ?> part : problemparts) {
			if (activeUUIDs.contains(part.getDeviceID())) {
				if (part.isReactsToInputStates()) {
					activeNeedsInput.add(part);
				} else {
					activeWorksAlone.add(part);
				}				
			} else if(passiveUUIDs.contains(part.getDeviceID())) {
				passive.add(part);
			} else {
				if (part.isCompletelyStatic()) 
					staticParts.add(part);
				else
					throw new IllegalArgumentException("part is neither active nor passive");
			}

			if (maxReferenceTime == null) {
				maxReferenceTime = part.getReferenceTime();
			}
			else {
				maxReferenceTime = Math.max(maxReferenceTime, part.getReferenceTime());
			}
		}

		Set<UUID> allActive = new HashSet<UUID>();

		for (InterdependentProblemPart<?, ?> part : activeNeedsInput) {
			allActive.add(part.getDeviceID());
			this.activeNeedInputUUIDs.add(part.getDeviceID());
		}
		for (InterdependentProblemPart<?, ?> part : activeWorksAlone) {
			allActive.add(part.getDeviceID());
		}
		for (InterdependentProblemPart<?, ?> part : passive) {
			this.passiveUUIDs.add(part.getDeviceID());
		}

		ocEnergySimulationCore.initializeGrids(allActive, activeNeedInputUUIDs, passiveUUIDs, 
				uuidIntMap, uuidOutputMap, uuidInputMap);

		maxOptimizationHorizon = maxReferenceTime;

		// calc maxOptimizationHorizon
		for (InterdependentProblemPart<?, ?> part : problemparts) {
			if (part instanceof ControllableIPP<?, ?>) {
				maxOptimizationHorizon = Math.max(((ControllableIPP<?, ?>) part).getOptimizationHorizon(), maxOptimizationHorizon);
			}
		}

		numberOfVariables_  = 1;
		numberOfObjectives_ = 1;
		numberOfConstraints_= 0;
		problemName_        = "ControllerBox";

		solutionType_	= new BinarySolutionType(this) ;

		length_			= new int[numberOfVariables_];
		length_[0] 		= numberOfBits ;

	}

	public void initMultithreading() {
		multiThreading = true;
		multiThreadedActiveNeedsInput = new ObjectArrayList<List<InterdependentProblemPart<?, ?>>>();
		multiThreadedActiveWorksAlone = new ObjectArrayList<List<InterdependentProblemPart<?, ?>>>();
		multiThreadedPassive = new ObjectArrayList<List<InterdependentProblemPart<?, ?>>>();
		multiThreadedStatic = new ObjectArrayList<List<InterdependentProblemPart<?, ?>>>();
		multiOCs = new ObjectArrayList<OCEnergySimulationCore>();

		multiThreadedMasterCopiesActiveNI = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
		multiThreadedMasterCopiesActiveWA = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
		multiThreadedMasterCopiesPassive = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
		multiThreadedMasterCopiesStatic = new ObjectArrayList<InterdependentProblemPart<?, ?>>();

		//set logger to null so that deep copy does not try to copy it
		IGlobalLogger temp = null;
		for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
			temp = part.logger;
			part.logger = null;
			part.prepareForDeepCopy();
		}
		for (InterdependentProblemPart<?, ?> part : this.staticParts) {
			temp = part.logger;
			part.logger = null;		
			part.prepareForDeepCopy();
		}
		for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
			temp = part.logger;
			part.logger = null;
			part.prepareForDeepCopy();
			if (part instanceof NonControllableIPP<?, ?>) {
				//initialize completely static IPP so we can save that time on every copy
				part.initializeInterdependentCalculation(maxReferenceTime, new BitSet(), STEP_SIZE, false, false);
			}
		}
		for (InterdependentProblemPart<?, ?> part : this.passive) {
			temp = part.logger;
			part.logger = null;
			part.prepareForDeepCopy();
			if (part instanceof NonControllableIPP<?, ?>) {
				//initialize completely static IPP so we can save that time on every copy
				part.initializeInterdependentCalculation(maxReferenceTime, new BitSet(), STEP_SIZE, false, false);
			}
		}

		//create one master copy per ProblemPart
		for (int j = 0; j < activeNeedsInput.size(); j++) {
			InterdependentProblemPart<?, ?> part = activeNeedsInput.get(j);
			multiThreadedMasterCopiesActiveNI.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
		}
		for (int j = 0; j < activeWorksAlone.size(); j++) {
			InterdependentProblemPart<?, ?> part = activeWorksAlone.get(j);
			multiThreadedMasterCopiesActiveWA.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
		}
		for (int j = 0; j < passive.size(); j++) {
			InterdependentProblemPart<?, ?> part = passive.get(j);
			multiThreadedMasterCopiesPassive.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
		}
		for (int j = 0; j < staticParts.size(); j++) {
			InterdependentProblemPart<?, ?> part = staticParts.get(j);
			multiThreadedMasterCopiesStatic.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
		}

		//restore logger
		for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
			part.logger = temp;		
		}
		for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
			part.logger = temp;		
		}
		for (InterdependentProblemPart<?, ?> part : this.passive) {
			part.logger = temp;		
		}
		for (InterdependentProblemPart<?, ?> part : this.staticParts) {
			part.logger = temp;		
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized List<InterdependentProblemPart<?, ?>>[] requestIPPCopys() {
		List<InterdependentProblemPart<?, ?>>[] ret = (ObjectArrayList<InterdependentProblemPart<?, ?>>[]) new ObjectArrayList<?>[4];

		if (multiThreadedActiveNeedsInput.size() != 0) {
			ret[0] = multiThreadedActiveNeedsInput.remove(0);
			ret[1] = multiThreadedActiveWorksAlone.remove(0);
			ret[2] = multiThreadedPassive.remove(0);
			ret[3] = multiThreadedStatic.remove(0);
		} else {
			ObjectArrayList<InterdependentProblemPart<?, ?>> niList = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
			ObjectArrayList<InterdependentProblemPart<?, ?>> waList = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
			ObjectArrayList<InterdependentProblemPart<?, ?>> paList = new ObjectArrayList<InterdependentProblemPart<?, ?>>();
			ObjectArrayList<InterdependentProblemPart<?, ?>> stList = new ObjectArrayList<InterdependentProblemPart<?, ?>>();

			for (int j = 0; j < multiThreadedMasterCopiesActiveNI.size(); j++) {
				InterdependentProblemPart<?, ?> part = multiThreadedMasterCopiesActiveNI.get(j);
				niList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
			}
			for (int j = 0; j < multiThreadedMasterCopiesActiveWA.size(); j++) {
				InterdependentProblemPart<?, ?> part = multiThreadedMasterCopiesActiveWA.get(j);
				waList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
			}
			for (int j = 0; j < multiThreadedMasterCopiesPassive.size(); j++) {
				InterdependentProblemPart<?, ?> part = multiThreadedMasterCopiesPassive.get(j);
				paList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
			}
			for (int j = 0; j < multiThreadedMasterCopiesStatic.size(); j++) {
				InterdependentProblemPart<?, ?> part = multiThreadedMasterCopiesStatic.get(j);
				stList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
			}

			ret[0] = niList;
			ret[1] = waList;
			ret[2] = paList;
			ret[3] = stList;
		}

		return ret;
	}
	
	private synchronized OCEnergySimulationCore requestOCESC() {
		if (multiOCs.size() != 0) {
			return multiOCs.remove(0);
		} else {
			return (OCEnergySimulationCore) DeepCopy.copy(ocEnergySimulationCore);
		}
	}

	private synchronized void freeIPPCopys(List<InterdependentProblemPart<?, ?>> needsI, List<InterdependentProblemPart<?, ?>> worksA,
			List<InterdependentProblemPart<?, ?>> passive, List<InterdependentProblemPart<?, ?>> staticParts) {
		multiThreadedActiveNeedsInput.add(needsI);
		multiThreadedActiveWorksAlone.add(worksA);
		multiThreadedPassive.add(passive);
		multiThreadedStatic.add(staticParts);
	}
	
	private synchronized void freeOCESC(OCEnergySimulationCore ocesc) {
		multiOCs.add(ocesc);
	}

	public void finalizeGrids() {
		ocEnergySimulationCore.finalizeGrids();
	}
	
	public void evaluateFinalTime(Solution solution, boolean log) throws JMException {		
		multiThreading = false;
		keepPrediction = true;
		evaluate(solution, log);
		keepPrediction = false;
		finalizeGrids();
	}
	
	@Override
	public void evaluate(Solution solution) throws JMException {
		evaluate(solution, false);
	}


	private void evaluate(Solution solution, boolean log) throws JMException {

		List<InterdependentProblemPart<?, ?>> activeNeedsInput = null;
		List<InterdependentProblemPart<?, ?>> activeWorksAlone = null;
		List<InterdependentProblemPart<?, ?>> passive = null;
		List<InterdependentProblemPart<?, ?>> staticParts = null;
		List<InterdependentProblemPart<?, ?>> allIPPs = null;
		OCEnergySimulationCore ocEnergySimulationCore = null;

		if (multiThreading) {
			List<InterdependentProblemPart<?, ?>>[] list = requestIPPCopys();
			activeNeedsInput = list[0];
			activeWorksAlone = list[1];
			passive = list[2];
			staticParts = list[3];
			ocEnergySimulationCore = requestOCESC();
		} else {
			activeNeedsInput = this.activeNeedsInput;
			activeWorksAlone = this.activeWorksAlone;
			passive = this.passive;
			staticParts = this.staticParts;
			ocEnergySimulationCore = this.ocEnergySimulationCore;
		}
		allIPPs = new ObjectArrayList<InterdependentProblemPart<?, ?>>(activeNeedsInput.size() + activeWorksAlone.size() + passive.size() + staticParts.size());
		allIPPs.addAll(activeNeedsInput);
		allIPPs.addAll(activeWorksAlone);
		allIPPs.addAll(passive);
		allIPPs.addAll(staticParts);
		


		try {
			double fitness = 0;

			Binary variable = (Binary) solution.getDecisionVariables()[0];			

			// calculate interdependent parts

			// initialize
			for (InterdependentProblemPart<?, ?> part : allIPPs) {
				int bitpos = 0;
				try {
					bitpos = bitPositions[part.getId()][0];
				}
				catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				int bitposEnd = bitPositions[part.getId()][1];
				part.initializeInterdependentCalculation(
						maxReferenceTime,
						variable.bits_.get(bitpos, bitposEnd),
						STEP_SIZE,
						false,
						keepPrediction
						);
				
				if ((part.getBitCount() == 0 && (bitposEnd - bitpos) != 0) 
						|| (part.getBitCount() > 0 && (bitposEnd - bitpos) != part.getBitCount())) {
					throw new IllegalArgumentException("Bitcount mismatch");
				}
			}

			// go through step by step
			AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();
			ancillaryMeter.initSequential();

			ObjectArrayList<InterdependentProblemPart<?, ?>> allActive = new ObjectArrayList<InterdependentProblemPart<?, ?>>(activeNeedsInput.size() + activeWorksAlone.size());
			allActive.addAll(activeNeedsInput);
			allActive.addAll(activeWorksAlone);
			
			InterdependentProblemPart<?, ?>[] allActiveArray = new InterdependentProblemPart<?, ?>[allActive.size()];
			allActiveArray = allActive.toArray(allActiveArray);
			
			InterdependentProblemPart<?, ?>[] allActiveNIArray = new InterdependentProblemPart<?, ?>[activeNeedsInput.size()];
			allActiveNIArray = activeNeedsInput.toArray(allActiveNIArray);
			
			InterdependentProblemPart<?, ?>[] passiveArray = new InterdependentProblemPart<?, ?>[passive.size()];
			passiveArray = passive.toArray(passiveArray);
			
			// go through in steps of STEP_SIZE (in ticks)
			try {
				//init the maps for the commodity states
				UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActiveArray, uuidIntMap, true);
				
				UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passiveArray, uuidIntMap, true);

				long t;

				//let all passive states calculate their first state
				for (InterdependentProblemPart<?, ?> part : passiveArray) {
					part.calculateNextStep();
					passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
				}
				
				//dummy AncillaryMeterState, we don't know the state of the ancillaryMeter at t=t_start, thus set all to zero
				AncillaryMeterState meterState = new AncillaryMeterState();

				//send the first passive state to active nodes
				ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, activeNeedInputUUIDs, passiveToActiveMap);

				// iterate
				for (t = maxReferenceTime; t < maxOptimizationHorizon + STEP_SIZE; t += STEP_SIZE) {

					//let all active states calculate their next step
					for (InterdependentProblemPart<?, ?> part : allActiveArray) {
						part.calculateNextStep();
						activeToPassiveMap.put(part.getId(), part.getCommodityOutputStates());
					}

					//send active state to passive nodes, save meter state				
					ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passiveArray, passiveUUIDs, meterState);

					//send loads to the ancillary meter profile
					ancillaryMeter.setLoadSequential(meterState, new Long(t));
					
					//let all passive states calculate their next step
					for (InterdependentProblemPart<?, ?> part : passiveArray) {
						part.calculateNextStep();
						passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
					}

					//send new passive states to active nodes
					ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, activeNeedInputUUIDs, passiveToActiveMap);
					
				}

			} catch (EnergySimulationException ex) {
				throw new SimulationEngineException(ex);
			}
			ancillaryMeter.endSequential();
			ancillaryMeter.setEndingTimeOfProfile(maxOptimizationHorizon);


			// calculate variable fitness depending on price signals...
			fitness = this.fitnessfunction.getFitnessValue(
					ignoreLoadProfileBefore, 
					ignoreLoadProfileAfter,
					ancillaryMeter, 
					priceSignals,
					powerLimitSignals
					);
			
			// add lukewarm cervisia (i.e. additional fixed costs...)
			for (InterdependentProblemPart<?, ?> problempart : allIPPs) {
				double add = problempart.getFinalInterdependentSchedule().getLukewarmCervisia();
				fitness += add;
				
				if (log && !((Binary) solution.getDecisionVariables()[0]).bits_.get(0) && add != 0) {
					OSH_gGAMultiThread.logCervisia(problempart.getDeviceType(), add);
				}
			}

			solution.setObjective(0, fitness); //small value is good value
			solution.setFitness(fitness);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		if (multiThreading) {
			freeIPPCopys(activeNeedsInput, activeWorksAlone, passive, staticParts);
			freeOCESC(ocEnergySimulationCore);
		}
	}

	public void evaluateWithDebuggingInformation(BitSet bits,
			AncillaryCommodityLoadProfile ancillaryMeter,
			TreeMap<Long, Double> predictedTankTemp, 
			TreeMap<Long, Double> predictedHotWaterDemand,
			TreeMap<Long, Double> predictedHotWaterSupply,
			List<Schedule> schedules,
			boolean keepPrediction,
			UUID hotwaterTankID) throws JMException {
		
		ancillaryMeter.initSequential();

		List<InterdependentProblemPart<?, ?>> activeNeedsInput = this.activeNeedsInput;
		List<InterdependentProblemPart<?, ?>> activeWorksAlone = this.activeWorksAlone;
		List<InterdependentProblemPart<?, ?>> passive = this.passive;
		List<InterdependentProblemPart<?, ?>> staticParts = this.staticParts;

		List<InterdependentProblemPart<?, ?>> allIPPs = new ArrayList<InterdependentProblemPart<?, ?>>(activeNeedsInput);
		allIPPs.addAll(activeWorksAlone);
		allIPPs.addAll(passive);
		allIPPs.addAll(staticParts);

		try {

			// calculate interdependent parts
			
			// initialize
			for (InterdependentProblemPart<?, ?> part : allIPPs) {
				int bitpos = 0;
				try {
					bitpos = bitPositions[part.getId()][0];
				}
				catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				int bitposEnd = bitPositions[part.getId()][1];
				part.initializeInterdependentCalculation(
						maxReferenceTime,
						bits.get(bitpos, bitposEnd),
						STEP_SIZE,
						true,
						keepPrediction
						);
				if ((part.getBitCount() == 0 && (bitposEnd - bitpos) != 0) 
						|| (part.getBitCount() != 0 && (bitposEnd - bitpos) != part.getBitCount())) {
					throw new IllegalArgumentException("Bitcount mismatch");
				}
			}

			List<InterdependentProblemPart<?, ?>> allActive = new LinkedList<InterdependentProblemPart<?, ?>>(activeNeedsInput);
			allActive.addAll(activeWorksAlone);
			
			InterdependentProblemPart<?, ?>[] allActiveArray = new InterdependentProblemPart<?, ?>[allActive.size()];
			allActiveArray = allActive.toArray(allActiveArray);
			
			InterdependentProblemPart<?, ?>[] allActiveNIArray = new InterdependentProblemPart<?, ?>[activeNeedsInput.size()];
			allActiveNIArray = activeNeedsInput.toArray(allActiveNIArray);
			
			InterdependentProblemPart<?, ?>[] passiveArray = new InterdependentProblemPart<?, ?>[passive.size()];
			passiveArray = passive.toArray(passiveArray);

			// go through in steps of STEP_SIZE (in ticks)
			try {
				long t;

				//init the maps for the commodity states
				UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActive, uuidIntMap);
				UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passive, uuidIntMap);

				//let all passive states calculate their first state
				for (InterdependentProblemPart<?, ?> part : passive) {
					part.calculateNextStep();
					passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
				}

				for (IOCEnergySubject simSub : passive) {
					LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
					if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
						if (simSub.getDeviceID().equals(hotwaterTankID)) {
							predictedTankTemp.put(maxReferenceTime, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
						}
					} else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
						if (simSub.getDeviceID().equals(hotwaterTankID)) {
							predictedTankTemp.put(maxReferenceTime, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
						}
					}	
				}					

				//dummy AncillaryMeterState, we dont know the state of the ancillaryMeter at t=start, so set all to zero

				AncillaryMeterState meterState = new AncillaryMeterState();
				
				//send the first passive state to active nodes
				ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, activeNeedInputUUIDs, passiveToActiveMap);					

				for (t = maxReferenceTime; t < maxOptimizationHorizon + STEP_SIZE; t = t
						+ STEP_SIZE) {

					activeToPassiveMap.clearInnerStates();

					//let all active states calculate their next step
					for (InterdependentProblemPart<?, ?> part : allActive) {
						part.calculateNextStep();
						activeToPassiveMap.put(part.getDeviceID(), part.getCommodityOutputStates());
					}				

					//generally setting demand to 0, will add to this if there is really a demand
					predictedHotWaterDemand.put(t, 0.0);
					predictedHotWaterSupply.put(t, 0.0);

					for (IOCEnergySubject simSub : allActive) {
						LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
						
						if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
							double demand =  outputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
							if (demand > 0.0) {
								Double current = predictedHotWaterDemand.get(t);
								predictedHotWaterDemand.put(t, current + demand);
							} else if (demand < 0.0) {
								Double current = predictedHotWaterSupply.get(t);
								predictedHotWaterSupply.put(t, current + demand);
							}
						} else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
							double demand =  outputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);
							if (demand > 0.0) {
								Double current = predictedHotWaterDemand.get(t);
								predictedHotWaterDemand.put(t, current + demand);
							} else if (demand < 0.0) {
								Double current = predictedHotWaterSupply.get(t);
								predictedHotWaterSupply.put(t, current + demand);
							}
						}
					}

					//send active state to passive nodes, save meterstate				
					ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passiveArray, passiveUUIDs, meterState);

					ancillaryMeter.setLoadSequential(meterState, t);

					passiveToActiveMap.clearInnerStates();
					//let all passive states calculate their next step
					for (InterdependentProblemPart<?, ?> part : passive) {
						part.calculateNextStep();
						passiveToActiveMap.put(part.getDeviceID(), part.getCommodityOutputStates());
					}

					for (IOCEnergySubject simSub : passive) {
						
						LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
						if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
							if (simSub.getDeviceID().equals(hotwaterTankID)) {
								predictedTankTemp.put(t + STEP_SIZE, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
							}
						} else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
							if (simSub.getDeviceID().equals(hotwaterTankID)) {
								predictedTankTemp.put(t + STEP_SIZE, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
							}
						}	
					}
					
					//send new passive states to active nodes
					ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, activeNeedInputUUIDs, passiveToActiveMap);
				}
				ancillaryMeter.endSequential();
				ancillaryMeter.setEndingTimeOfProfile(maxOptimizationHorizon);

				//add final schedules
				for (InterdependentProblemPart<?, ?> part : allIPPs) {
					schedules.add(part.getFinalInterdependentSchedule());
				}	

			} catch (EnergySimulationException ex) {
				throw new SimulationEngineException(ex);
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
