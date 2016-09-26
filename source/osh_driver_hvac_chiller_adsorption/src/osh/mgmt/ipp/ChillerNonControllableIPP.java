package osh.mgmt.ipp;

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
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.chiller.AdsorptionChillerModel;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder
 *
 */
public class ChillerNonControllableIPP extends NonControllableIPP<ISolution, IPrediction> {

	/**	 */
	private static final long serialVersionUID = -8273266479216299094L;
	
	private final int typicalStandbyActivePower = 10; // [W]
	private final int typicalRunningActivePower = 420; // [W]

	/** prediction horizon in seconds */
	public final static int RELATIVE_HORIZON = 0; // no horizon...it's stupid...
	
	/** slot length in [s] */
	public final static long TIME_PER_SLOT = 5 * 60; // 5 minutes

	/** is AdChiller on at the beginning */
	private boolean initialAdChillerState;
	private Map<Long, Double> temperaturePrediction;
	
	// temperature control
	private double coldWaterStorageMinTemp = 10.0;
	private double coldWaterStorageMaxTemp = 15.0;
	
	private double hotWaterStorageMinTemp = 55.0;
	private double hotWaterStorageMaxTemp = 80.0;
	
//	/** delta T below maximum cold water temperature */
//	private double hysteresis = 1.0;
	
	
	// ### interdependent stuff ###
	/** used for iteration in interdependent calculation (ancillary time in the future) */
	private long interdependentTime;
	/** running times of chiller */
	private double interdependentCervisia;
	private boolean interdependentLastState;
	
	/** from cold water tank IPP */
	private double currentColdWaterTemperature = 12;
	/** from hot water tank IPP */
	private double currentHotWaterTemperature = 60;
	
	private SparseLoadProfile lp = null;

	
	/**
	 * CONSTRUCTOR
	 * @param deviceId
	 * @param now
	 * @param electricalpower
	 * @param typicalThermalpower
	 * @param neededEnergy
	 * @param remainingRunningTime
	 */
	public ChillerNonControllableIPP(
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
				toBeScheduled,
				false, //needsAncillaryMeterState
				true, //reactsToInputStates
				false, //is not static
				now, 
				DeviceTypes.ADSORPTIONCHILLER,
				new Commodity[]{Commodity.ACTIVEPOWER,
						Commodity.REACTIVEPOWER,
						Commodity.HEATINGHOTWATERPOWER,
						Commodity.COLDWATERPOWER
				},
				compressionType,
				compressionValue);
		
		this.initialAdChillerState = initialAdChillerState;
		this.temperaturePrediction = temperaturePrediction;
		
		this.allInputCommodities = new Commodity[]{Commodity.HEATINGHOTWATERPOWER, Commodity.COLDWATERPOWER};
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
		setOutputStates(null);
		this.interdependentInputStates = null;
		
		if (createLoadProfile) {
			lp = new SparseLoadProfile();
		} else {
			lp = null;
		}
		
		if (this.getReferenceTime() != maxReferenceTime) {
			setReferenceTime(maxReferenceTime);
		}
		
		this.stepSize = stepSize;
		
		this.interdependentCervisia = 0.0;
		this.interdependentTime = this.getReferenceTime();
		
		this.interdependentLastState = this.initialAdChillerState;
	}

	@Override
	public synchronized void calculateNextStep() {
		
		
		// ### interdependent logic (hysteresis control) ###
			// "cold water control" (min, max temperatures) 
		
		// update water temperatures
		
		if ( interdependentInputStates == null ) {
			System.out.println("No interdependentInputStates available.");
		}
		
		currentHotWaterTemperature = interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

		currentColdWaterTemperature = interdependentInputStates.getTemperature(Commodity.COLDWATERPOWER);

		boolean chillerNewState = this.interdependentLastState;
		
		// AdChiller control (forced on/off)
		
		if (this.interdependentLastState) {
			if ( currentColdWaterTemperature <= coldWaterStorageMinTemp) {
				chillerNewState = false;					
			}
			if (currentHotWaterTemperature < hotWaterStorageMinTemp
					|| currentHotWaterTemperature > hotWaterStorageMaxTemp) {
				chillerNewState = false;
			}
		}
		else {
			if ( currentColdWaterTemperature > coldWaterStorageMaxTemp 
					&& currentHotWaterTemperature > hotWaterStorageMinTemp
					&& currentHotWaterTemperature < hotWaterStorageMaxTemp) {
				chillerNewState = true;
			}
		}

		// ### set power profiles and interdependentCervisia
		
//		if (chillerNewState) {
//			// the later the better AND the less the better
//			this.interdependentCervisia += 0.001 * (ab.length - i); 
//		}
		
		
		// calculate power values
		double activePower = typicalStandbyActivePower;
		double hotWaterPower = 0;
		double coldWaterPower = 0;
		
		if (chillerNewState) {
			long secondsFromYearStart = TimeConversion.convertUnixTime2SecondsFromYearStart(interdependentTime);
			double outdoorTemperature = temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
			activePower = typicalRunningActivePower;
			coldWaterPower = AdsorptionChillerModel.chilledWaterPower(currentHotWaterTemperature, outdoorTemperature);
		}
		
		if ( chillerNewState
				|| ( !chillerNewState && this.interdependentLastState )
				|| ( this.interdependentTime - this.getReferenceTime() == 0) ) {
			
			
			if (lp != null) {
				lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, (int) activePower);
				lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, (int) hotWaterPower);
				lp.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, (int) coldWaterPower);
			}
			
			this.internalInterdependentOutputStates.setPower(
					Commodity.ACTIVEPOWER, activePower);
			this.internalInterdependentOutputStates.setPower(
					Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			this.internalInterdependentOutputStates.setPower(
					Commodity.COLDWATERPOWER, coldWaterPower);
			
			setOutputStates(internalInterdependentOutputStates);
		}
		
		if ( chillerNewState && !this.interdependentLastState) {
			// fixed costs per start, i.e., costs to turn on the CHP 
			// (not the variable costs for letting the CHP run)
			this.interdependentCervisia += 10.0;
		}
		
		this.interdependentLastState = chillerNewState;
		this.interdependentTime = this.interdependentTime + stepSize;
	}
	
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		
		if (lp == null) {
			return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
		} else {
			if (lp.getEndingTimeOfProfile() > 0) {
				lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, typicalStandbyActivePower);
				lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
				lp.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, 0);
			}
			
			SparseLoadProfile slp = lp.getCompressedProfileByDiscontinuities(50);
			return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
		}
	}
	
	// HELPER STUFF

	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "Chiller NonControllableIPP";
	}

	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		//NOTHING
	}

}
