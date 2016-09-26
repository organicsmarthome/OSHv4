package osh.mgmt.ipp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
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
import osh.driver.chiller.AdsorptionChillerModel;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Julian Feder, Sebastian Kramer, Ingo Mauser
 *
 */
public class ChillerIPP extends ControllableIPP<ISolution, IPrediction> {
	
	private static final long serialVersionUID = -515441464083361208L;

	private class AdditionalInfo {
		@SuppressWarnings("unused")
		public int noForcedOffs;
		public int noForcedOns;
	}

	private final int typicalStandbyActivePower = 10; // [W]
	private final int typicalRunningActivePower = 420; // [W]

	//TODO move to config
	/** prediction horizon in seconds */
//	public final static int RELATIVE_HORIZON = 3 * 3600; // 3 hours
//	public final static int RELATIVE_HORIZON = 6 * 3600; // 6 hours
//	public final static int RELATIVE_HORIZON = 12 * 3600; // 12 hours
//	public final static int RELATIVE_HORIZON = 18 * 3600; // 18 hours
	public final static int RELATIVE_HORIZON = 24 * 3600; // 24 hours
	
	/** slot length in [s] */
	public final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
	private final static int BITSPERACTIVATION = 4;

	/** is AdChiller on at the beginning */
	private boolean initialAdChillerState;
	private Map<Long, Double> temperaturePrediction;
	
	// temperature control
	private double coldWaterStorageMinTemp = 10.0;
	private double coldWaterStorageMaxTemp = 15.0;
	
	private double hotWaterStorageMinTemp = 55.0;
//	private double hotWaterStorageMaxTemp = 80.0;
	
	/** delta T below maximum cold water temperature (for forced cooling) */
	private double hysteresis = 1.0;
	
	private boolean initialState;
	
	// ### interdependent stuff ###
	
	private ArrayList<Activation> interdependentStartingTimes;
	
	/** used for iteration in interdependent calculation (ancillary time in the future) */
	private long interdependentTime;
	
	/** running times of chiller */
	private double interdependentCervisia;
	private boolean interdependentLastState;
	
	/** from cold water tank IPP */
	private double currentColdWaterTemperature = 12;
	/** from hot water tank IPP */
	private double currentHotWaterTemperature = 60;

	private boolean[] activationBits;
	private int currentActivationRunningTime;
	
	private SparseLoadProfile loadProfile = null;

	
	/**
	 * CONSTRUCTOR
	 */
	public ChillerIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			boolean toBeScheduled,
			boolean initialAdChillerState,
			Map<Long, Double> temperaturePrediction,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(
				deviceId, 
				logger,
				now, 
				getNecessaryNumberOfBits(RELATIVE_HORIZON), 
				toBeScheduled, 
				false, //needsAncillaryMeterStates
				true, //reactsToInputStates
				now + RELATIVE_HORIZON,
				now,
				DeviceTypes.ADSORPTIONCHILLER,
				new Commodity[]{Commodity.ACTIVEPOWER, 
						Commodity.REACTIVEPOWER, 
						Commodity.HEATINGHOTWATERPOWER,
						Commodity.COLDWATERPOWER},
				compressionType,
				compressionValue);
		
		this.initialAdChillerState = initialAdChillerState;
		this.temperaturePrediction = temperaturePrediction;
	}
	

	// ### interdependent problem part stuff ###

	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean createLoadProfile,
			boolean keepPrediction) {
		
		// used for iteration in interdependent calculation
		this.interdependentStartingTimes = null;
		setOutputStates(null);
		this.interdependentInputStates = null;
		
		if (createLoadProfile) {
			this.loadProfile = new SparseLoadProfile();
		} 
		else {
			this.loadProfile = null;
		}
		
		this.stepSize = stepSize;
		
		this.interdependentCervisia = 0.0;
		
		if (maxReferenceTime != this.getReferenceTime()) {
			this.recalculateEncoding(maxReferenceTime, maxReferenceTime + RELATIVE_HORIZON);
		}
		this.interdependentTime = this.getReferenceTime();
		
		this.activationBits = getActivationBits(this.getReferenceTime(), solution, null);
		
		this.interdependentLastState = this.initialAdChillerState;
		
		this.currentActivationRunningTime = 0;
	}

	@Override
	public void calculateNextStep() {
		
		// update water temperatures
		if ( interdependentInputStates == null ) {
			logger.logDebug("No interdependentInputStates available.");
		}
		
		currentHotWaterTemperature = interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
		currentColdWaterTemperature = interdependentInputStates.getTemperature(Commodity.COLDWATERPOWER);
		
		// ### interdependent logic (hysteresis control) ###
		// cold water control (min, max temperatures) 
		boolean chillerNewState = this.interdependentLastState;
		boolean chillerHysteresisOn = false;
		boolean minColdWaterTankTemperatureOff = false;
		boolean minHotWaterTankTemperatureOff = false;
		
		// AdChiller control (forced on/off)
		if (interdependentLastState) {
			// cold water too cold -> off
			if ( currentColdWaterTemperature < coldWaterStorageMinTemp) {
				minColdWaterTankTemperatureOff = true;
				chillerNewState = false;					
			}
			else if (currentColdWaterTemperature >= coldWaterStorageMaxTemp - hysteresis
					&& currentColdWaterTemperature <= coldWaterStorageMaxTemp) {
				chillerNewState = true;
				chillerHysteresisOn = true;
			}
			// hot water too cold or hot water too hot -> off
			if (currentHotWaterTemperature < hotWaterStorageMinTemp) {
				minHotWaterTankTemperatureOff = true;
				chillerNewState = false;
			}
			//TODO add hot water maximum temperature control
		}
		else {
			if ( currentColdWaterTemperature > coldWaterStorageMaxTemp 
					&& currentHotWaterTemperature > hotWaterStorageMinTemp) {
				chillerHysteresisOn = true;
				chillerNewState = true;
			}
		}
		
		int i = (int) ( (this.interdependentTime - this.getReferenceTime()) / TIME_PER_SLOT );
		
		if (chillerHysteresisOn == false 
				&& minColdWaterTankTemperatureOff == false
				&& minHotWaterTankTemperatureOff == false
				&& i < activationBits.length) {
			chillerNewState = activationBits[i];
		}
		else {
			//NOTHING (KEEP STATE)
		}

		// ### set power profiles and interdependentCervisia
		
		if (chillerNewState) {
			// the later the better AND the less the better
			this.interdependentCervisia += 0.0001 * (activationBits.length - i); 
		}
		
		
		// calculate power values
		double activePower = typicalStandbyActivePower;
		double hotWaterPower = 0;
		double coldWaterPower = 0;
		
		if ( (chillerNewState && !this.interdependentLastState)
				|| (chillerNewState && currentActivationRunningTime % 60 == 0)
				|| ( !chillerNewState && this.interdependentLastState )
				|| ( this.interdependentTime == this.getReferenceTime()) ) {
			
			if (chillerNewState) {
				if (temperaturePrediction.get((interdependentTime / 300) * 300) == null) {
					@SuppressWarnings("unused")
					long time = (interdependentTime / 300) * 300;
					@SuppressWarnings("unused")
					int debug  = 0;
				}
				long secondsFromYearStart = TimeConversion.convertUnixTime2SecondsFromYearStart(interdependentTime);
				double outdoorTemperature = temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
				activePower = typicalRunningActivePower;
				coldWaterPower = AdsorptionChillerModel.chilledWaterPower(currentHotWaterTemperature, outdoorTemperature);
				hotWaterPower = (-1) * coldWaterPower / AdsorptionChillerModel.cop(currentHotWaterTemperature, outdoorTemperature);
			}
			
			if (loadProfile != null) {
				loadProfile.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, (int) activePower);
				loadProfile.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, (int) hotWaterPower);
				loadProfile.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, (int) coldWaterPower);
			}
			
			this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
			this.internalInterdependentOutputStates.setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			this.internalInterdependentOutputStates.setPower(Commodity.COLDWATERPOWER, coldWaterPower);
			
			
			setOutputStates(internalInterdependentOutputStates);
		} else {
			setOutputStates(null);
		}
		
		if ( chillerNewState && !this.interdependentLastState ) {
			// fixed costs per start, i.e., costs to turn on the CHP 
			// (not the variable costs for letting the CHP run)
			this.interdependentCervisia += 10.0;
		}
		
		this.interdependentLastState = chillerNewState;
		this.interdependentTime = this.interdependentTime + stepSize;
		if (chillerNewState) {
			this.currentActivationRunningTime = currentActivationRunningTime + stepSize;
		}
		else {
			this.currentActivationRunningTime = 0;
		}
	}
	
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		
		if (loadProfile == null) {
			return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
		} else {
			if (loadProfile.getEndingTimeOfProfile() > 0) {
				loadProfile.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, typicalStandbyActivePower);
				loadProfile.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
				loadProfile.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, 0);
			}
			
			SparseLoadProfile slp = loadProfile.getCompressedProfile(compressionType, compressionValue, compressionValue);
			return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
		}
	}

	@Override
	public ISolution transformToFinalInterdependetPhenotype(BitSet solution) {
		
		boolean[] ab = getActivationBits(this.getReferenceTime(), solution, null);
		
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
			ActivationList adchillerPhenotype = new ActivationList();
			adchillerPhenotype.setList(this.interdependentStartingTimes);
			return adchillerPhenotype;
		}
		else {
			return null;
		}
	}
	
	
	// ### OLD STUFF (best guess schedule, not interdependent)
	
	@Override
	public ActivationList transformToPhenotype(BitSet solution) {
		return null;
	}
	
	
	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
		this.setOptimizationHorizon(maxHorizon);
		this.setBitCount(getNecessaryNumberOfBits());
	}
	
	// HELPER STUFF
	
	private boolean[] getActivationBits(
			long now, 
			BitSet solution,
			AdditionalInfo ai) {

		if (ai != null) {
			ai.noForcedOffs = 0;
			ai.noForcedOns = 0;
		}

		int bitcount = getNecessaryNumberOfBits();
		boolean ret[] = new boolean[bitcount / BITSPERACTIVATION];

		boolean laststate = initialState;

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

			laststate = chpOn;
			
			ret[i / BITSPERACTIVATION] = chpOn;
		}

		return ret;
	}
	
	private int getNecessaryNumberOfBits() {
		return  (int) (Math.ceil((double) (this.getOptimizationHorizon() - this.getReferenceTime()) / ((double) TIME_PER_SLOT)) * BITSPERACTIVATION);
	}
	
	private static int getNecessaryNumberOfBits(int relativeHorizon) {
		return (int) (RELATIVE_HORIZON / TIME_PER_SLOT) * BITSPERACTIVATION;
	}

	// ### to string ###
	
	@Override
	public String problemToString() {
		AdditionalInfo ai = new AdditionalInfo();
		getActivationBits(getReferenceTime(), new BitSet(), ai);
		return "Chiller IPP , forced ons:" + ai.noForcedOns;
	}

	@Override
	public String solutionToString(BitSet bits) {
		return "Chiller IPP solution";
	}
}