package osh.mgmt.ipp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.time.Activation;
import osh.datatypes.time.ActivationList;
import osh.driver.chp.model.GenericChpModel;

/**
 * 
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Sebastian Kramer
 *
 */
public class DachsChpIPP 
					extends ControllableIPP<ISolution, IPrediction> {

	private static final long serialVersionUID = 7540352071581934211L;
	
	// fixed costs per start, i.e., costs to turn on the CHP 
	// (not the variable costs for letting the CHP run)
	@SuppressWarnings("unused")
	private double fixedCostPerStart;
	
	private double forcedOnOffStepMultiplier;
	private int forcedOffAdditionalCost;
	
	@SuppressWarnings("unused")
	private double chpOnCervisiaStepSizeMultiplier;
	
	/** slot length in [s] */
	private final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
	private final static int BITSPERACTIVATION = 4;

	
	private final boolean initialState;
	/** minimum running time of CHP in seconds */
	private final int minRunTime;
	//TODO add maxRunTime
	
	// temperature control
	private double hotWaterStorageMinTemp;
	private double hotWaterStorageMaxTemp;
	private double hysteresis;
	
	private double currentWaterTemperature;
	
	@SuppressWarnings("unused")
	private int noForcedOffs;
	@SuppressWarnings("unused")
	private int noForcedOns;
	
	
	// ### interdependent stuff ###
	/** used for iteration in interdependent calculation */
	private long interdependentTime;
	
	private ArrayList<Activation> interdependentStartingTimes;
	private long interdependentTimeOfFirstBit;
	private double interdependentCervisia;
	private boolean interdependentLastState;

	private SparseLoadProfile lp = null;
	private boolean[] ab;
	
	private GenericChpModel masterModel;
	private GenericChpModel actualModel;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public DachsChpIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			boolean toBeScheduled,
			boolean initialState,
			int minRunTime,
			GenericChpModel chpModel,
			int relativeHorizon,
			double hotWaterStorageMinTemp,
			double hotWaterStorageMaxTemp,
			double hysteresis,
			double currentWaterTemperature,
			double fixedCostPerStart,
			double forcedOnOffStepMultiplier,
			int forcedOffAdditionalCost,
			double chpOnCervisiaStepSizeMultiplier,
			LoadProfileCompressionTypes compressionType,
			int compressionValue
			) {
		super(deviceId, 
				logger, 
				now, 
				getNecessaryNumberOfBits(relativeHorizon), 
				toBeScheduled, 
				false, //does not need ancillary meter state as Input State
				true, //reacts to input states
				now + relativeHorizon, 
				now, 
				DeviceTypes.CHPPLANT,
				new Commodity[]{Commodity.ACTIVEPOWER,
						Commodity.REACTIVEPOWER,
						Commodity.HEATINGHOTWATERPOWER,
						Commodity.NATURALGASPOWER
				},
				compressionType,
				compressionValue);
		
		this.initialState = initialState;
		this.minRunTime = minRunTime;
		
		this.hotWaterStorageMinTemp =  hotWaterStorageMinTemp;
		this.hotWaterStorageMaxTemp =  hotWaterStorageMaxTemp;
		this.hysteresis = hysteresis;
		this.currentWaterTemperature = currentWaterTemperature;
		
		this.masterModel = chpModel;

		this.fixedCostPerStart = fixedCostPerStart;
		
		this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
		this.forcedOffAdditionalCost = forcedOffAdditionalCost;
		
		this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;
		
		this.allInputCommodities = new Commodity[]{Commodity.HEATINGHOTWATERPOWER};
	}
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected DachsChpIPP() {
		super();
		initialState = true;
		minRunTime = 0;
		hotWaterStorageMinTemp = 0;
		hotWaterStorageMaxTemp = 0;
		hysteresis = 0;
		currentWaterTemperature = 0;
	}


	// ### interdependent problem part stuff ###

	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean createLoadProfile,
			boolean keepPrediction) {
		
		this.stepSize = stepSize;
		if (createLoadProfile)
			this.lp = new SparseLoadProfile();
		else
			this.lp = null;
		
		// used for iteration in interdependent calculation
		this.interdependentStartingTimes = null;
		setOutputStates(null);
		this.interdependentInputStates = null;
		
		this.interdependentCervisia = 0.0;
		
		if (maxReferenceTime != this.getReferenceTime()) {
			recalculateEncoding(maxReferenceTime, this.getOptimizationHorizon());
		}
		this.interdependentTimeOfFirstBit = this.getReferenceTime();
		this.interdependentTime = this.getReferenceTime();
		
		this.ab = getActivationBits(this.interdependentTimeOfFirstBit, solution);		

		this.noForcedOffs = 0;
		this.noForcedOns = 0;
		
		this.interdependentLastState = initialState;
		
		this.actualModel = masterModel.clone();		
	}

	@Override
	public void calculateNextStep() {

		this.currentWaterTemperature = interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
	
		
		// ### interdependent logic (repair functionality) ###
			// hot water temperature control (min, max) 
			// i.e., CHP control (forced on/off)
		boolean chpOn = false;
		boolean plannedState = false;
		boolean hysteresisOn = false;
		
		int i = (int) ( (this.interdependentTime - this.interdependentTimeOfFirstBit) / TIME_PER_SLOT );
		
		if (i < this.ab.length) {				
			chpOn = this.ab[i];
			plannedState = chpOn;
		}
		
		// temperature control
		{
			if (chpOn) {
				//planned state: on
				if ( currentWaterTemperature > hotWaterStorageMaxTemp ) {
					// hot water too hot -> OFF
					chpOn = false;
					noForcedOffs++;
					
				}			
			} 
			else {
				//planned state: off
				if (currentWaterTemperature <= hotWaterStorageMinTemp) {
					// hot water too cold -> ON
					chpOn = true;
					noForcedOns++;
				} 
				else if (interdependentLastState && currentWaterTemperature <= hotWaterStorageMinTemp + hysteresis) {
					//hysteresis keep on
					chpOn = true;
					hysteresisOn = true;
				}
			}
			
			// either forced on or forced off
			if (chpOn != plannedState && !hysteresisOn) {
				//avoid forced on/offs
				interdependentCervisia = interdependentCervisia + forcedOnOffStepMultiplier * stepSize + forcedOffAdditionalCost;
			}
		}
		
		//ignore shutOff when minRunTime is not reached (only when not forced off)
		if (!chpOn 
				&& !plannedState 
				&& interdependentLastState 
				&& (interdependentTime - actualModel.getRunningSince()) < minRunTime) {
			chpOn = true;
		}	
		
		//switched on or off
		if (chpOn != interdependentLastState) {
			actualModel.setRunning(chpOn, this.interdependentTime);
		}
		
		actualModel.calcPowerAvg(this.interdependentTime, this.interdependentTime + stepSize);

		int activePower = actualModel.getAvgActualActivePower();
		int reactivePower = actualModel.getAvgActualReactivePower();
		int thermalPower = actualModel.getAvgActualThermalPower();
		int gasPower = actualModel.getAvgActualGasPower();

		// set power
		if (lp != null) {
			this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, activePower);
			this.lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, reactivePower);
			this.lp.setLoad(Commodity.NATURALGASPOWER, this.interdependentTime, gasPower);
			this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, thermalPower);
		}
		
		boolean hasValues = false;
		
		if (activePower != 0) {
			this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
			hasValues = true;
		} else {
			this.internalInterdependentOutputStates.resetCommodity(Commodity.ACTIVEPOWER);
		}
		
		if (reactivePower != 0) {
			this.internalInterdependentOutputStates.setPower(Commodity.REACTIVEPOWER, reactivePower);
			hasValues = true;
		} else {
			this.internalInterdependentOutputStates.resetCommodity(Commodity.REACTIVEPOWER);
		}
		
		if (thermalPower != 0) {
			this.internalInterdependentOutputStates.setPower(Commodity.HEATINGHOTWATERPOWER, thermalPower);
			hasValues = true;
		} else {
			this.internalInterdependentOutputStates.resetCommodity(Commodity.HEATINGHOTWATERPOWER);
		}
		
		if (gasPower != 0) {
			this.internalInterdependentOutputStates.setPower(Commodity.NATURALGASPOWER, gasPower);
			hasValues = true;
		} else {
			this.internalInterdependentOutputStates.resetCommodity(Commodity.NATURALGASPOWER);
		}
		
		if (hasValues) {
			setOutputStates(internalInterdependentOutputStates);
		} else {
			setOutputStates(null);
		}
		
		this.interdependentLastState = chpOn;
		this.interdependentTime += stepSize;
	}
	
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		if (this.lp != null) {
			return new Schedule(
					this.lp.getCompressedProfile(
								compressionType, 
								compressionValue, 
								compressionValue), 
					this.interdependentCervisia, 
					this.getDeviceType().toString());
		}
		else {
			return new Schedule(new SparseLoadProfile(), this.interdependentCervisia,this.getDeviceType().toString());
		}
	}

	@Override
	public ISolution transformToFinalInterdependetPhenotype(BitSet solution) {
		
		boolean[] ab = getActivationBits(this.interdependentTimeOfFirstBit, solution);
		
		this.interdependentStartingTimes = new ArrayList<Activation>();
		long timeoffirstbit = getReferenceTime();
		Activation currentactivation = null;

		for (int i = 0; i < ab.length; i++) {
			if (ab[i]) {
				// turn on
				if (currentactivation == null) {
					currentactivation = new Activation();
					currentactivation.startTime = timeoffirstbit + i * TIME_PER_SLOT;
					currentactivation.duration = TIME_PER_SLOT;
				} 
				else {
					currentactivation.duration += TIME_PER_SLOT;
				}
			} 
			else {
				// turn off
				if (currentactivation != null) {
					interdependentStartingTimes.add(currentactivation);
					currentactivation = null;
				}
			}
		}

		if (currentactivation != null) {
			interdependentStartingTimes.add(currentactivation);
			currentactivation = null;
		}
		
		if (this.interdependentStartingTimes != null) {
			ActivationList chpPhenotype = new ActivationList();
			chpPhenotype.setList(this.interdependentStartingTimes);
			return chpPhenotype;
		}
		else {
			return null;
		}
	}
	
	// ### best guess schedule without interdependencies ###
	
//	@Override
//	public Schedule getSchedule(BitSet solution) {
//		SparseLoadProfile pr = new SparseLoadProfile();
//		double cervisia = 0.0;
//		
//		long timeoffirstbit = getReferenceTime();
//		boolean laststate;
//		boolean activationbits[] = getActivationBits(timeoffirstbit, solution);
//		
//		laststate = initialState;
//
//		for (int i = 0; i < activationbits.length; i++) {
//			boolean chpOn = activationbits[i];
//			long timeStartSlot = timeoffirstbit + i * TIME_PER_SLOT;
//
//			if (chpOn) {
//				// the later the better AND the less the better
//				cervisia += 0.001 * (activationbits.length - i); 
//			}
//
//			if (chpOn == true && laststate == false) {
//				pr.setLoad(Commodity.ACTIVEPOWER, timeStartSlot, typicalActivePower);
//				pr.setLoad(Commodity.NATURALGASPOWER, timeStartSlot, typicalGasPower);
//				laststate = true;
//				// fixed costs per start
//				// costs to turn on the CHP 
//				// (not the variable costs for letting the CHP run) (random value)
//				cervisia += fixedCostPerStart;
//			} 
//			else if (chpOn == false && laststate == true) {
//				pr.setLoad(Commodity.ACTIVEPOWER, timeStartSlot, 0);
//				pr.setLoad(Commodity.NATURALGASPOWER, timeStartSlot, 0);
//				laststate = false;
//			}
//		}
//		
//		if (laststate == true) {
//			pr.setLoad(Commodity.ACTIVEPOWER, this.getOptimizationHorizon(), 0);
//			pr.setLoad(Commodity.NATURALGASPOWER, this.getOptimizationHorizon(), 0);
//		}
//		
//		return new Schedule(pr, cervisia, this.getDeviceType().toString());
//	}

	@Override
	public ActivationList transformToPhenotype(BitSet solution) {
		ArrayList<Activation> starttimes = new ArrayList<Activation>();
		long timeoffirstbit = getReferenceTime();
		
		boolean[] activationBits = getActivationBits(timeoffirstbit, solution);
		Activation currentactivation = null;

		for (int i = 0; i < activationBits.length; i++) {
			if (activationBits[i]) {
				// turn on
				if (currentactivation == null) {
					currentactivation = new Activation();
					currentactivation.startTime = timeoffirstbit + i * TIME_PER_SLOT;
					currentactivation.duration = TIME_PER_SLOT;
				} 
				else {
					currentactivation.duration += TIME_PER_SLOT;
				}
			} 
			else {
				// turn off
				if (currentactivation != null) {
					starttimes.add(currentactivation);
					currentactivation = null;
				}
			}
		}

		if (currentactivation != null) {
			starttimes.add(currentactivation);
			currentactivation = null;
		}
		
		ActivationList chpPhenotype = new ActivationList();
		chpPhenotype.setList(starttimes);
		return chpPhenotype;
	}


	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
		this.setOptimizationHorizon(maxHorizon);
		this.setBitCount(getNecessaryNumberOfBits());
	}
	
	// ### helper stuff ###
	
	private boolean[] getActivationBits(
			long now, 
			BitSet solution) {

		int bitcount = getNecessaryNumberOfBits();
		boolean ret[] = new boolean[bitcount / BITSPERACTIVATION];

		boolean laststate = initialState;
		long runningFor = 0;
		
		if (initialState) {
			runningFor = masterModel.getRunningForAtTimestamp(now);
		}
		
		for (int i = 0; i < bitcount; i += BITSPERACTIVATION) {
			boolean chpOn;
			
			// automaton
			boolean anded = true, ored = false; // and / or
			for (int j = 0; j < BITSPERACTIVATION; j++) {
				anded &= solution.get(i + j);
				ored |= solution.get(i + j);
			}
			if (anded == false && ored == true) { // bits are not all equal
				chpOn = laststate; // keep last state
			} 
			else {
				chpOn = solution.get(i); // all 1 -> on, all 0 -> off
			}
			// end automaton
			
			// enforce minimum operating time
			if (laststate 
					&& !chpOn 
					&& runningFor < minRunTime) {
				chpOn = true;
			}
			// enforce maximum operating time
			//(TODO max time)
			
			if (chpOn) {
				runningFor += TIME_PER_SLOT;
			} 
			else {
				runningFor = 0; 
			}

			laststate = chpOn;
			
			ret[i / BITSPERACTIVATION] = chpOn;			
		}
		return ret;
	}
	
	private int getNecessaryNumberOfBits() {
		return  Math.round((float) (this.getOptimizationHorizon() - this.getReferenceTime()) / ((float) TIME_PER_SLOT)) * BITSPERACTIVATION;
	}
	
	private static int getNecessaryNumberOfBits(int relativeHorizon) {
		return (int) (relativeHorizon / TIME_PER_SLOT) * BITSPERACTIVATION;
	}
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "DachsChpIPP [" + getReferenceTime() + "] [" + getOptimizationHorizon() + "]";
	}

	@Override
	public String solutionToString(BitSet bits) {
		boolean[] ab = getActivationBits(getReferenceTime(), bits);
		return "[" + getReferenceTime() + "] [" + getOptimizationHorizon() + "] " + ab;
		
	}

}