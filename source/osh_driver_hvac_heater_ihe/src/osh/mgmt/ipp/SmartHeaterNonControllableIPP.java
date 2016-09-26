package osh.mgmt.ipp;

import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.ihe.SmartHeaterModel;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class SmartHeaterNonControllableIPP 
					extends NonControllableIPP<ISolution, IPrediction> {
	
	private static final long serialVersionUID = -7540136211941577232L;
	
	private SmartHeaterModel model;
	
	private final int temperatureSetting;
	private final int initialState;
	private final long[] timestampOfLastChangePerSubElement;
	
	
	// ### interdependent stuff ###
	/** used for iteration in interdependent calculation */
	private long interdependentTime;
	
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
	public SmartHeaterNonControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			int temperatureSetting, 
			int initialState,
			long[] timestampOfLastChangePerSubElement,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger, 
				false, //does not cause scheduling
				true, //needs ancillary meter state as Input State
				true, //reacts to input states
				false, //is not static
				now,
				DeviceTypes.INSERTHEATINGELEMENT,
				new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
				compressionType,
				compressionValue);
		
		this.temperatureSetting = temperatureSetting;
		this.initialState = initialState;
		this.timestampOfLastChangePerSubElement = timestampOfLastChangePerSubElement;
		
		this.model = new SmartHeaterModel(
				temperatureSetting, 
				initialState,
				timestampOfLastChangePerSubElement);
		
		this.allInputCommodities = new Commodity[]{Commodity.HEATINGHOTWATERPOWER};
	}
	
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected SmartHeaterNonControllableIPP() {
		super();
		temperatureSetting = 0;
		initialState = 0;
		timestampOfLastChangePerSubElement = null;
	}


	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
		// get new temperature of tank
		//  better not...new IPP instead
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
		this.interdependentTime = this.getReferenceTime();
		setOutputStates(null);
		this.interdependentInputStates = null;
		this.ancillaryMeterState = null;
		
		this.model = new SmartHeaterModel(
				temperatureSetting, 
				initialState,
				timestampOfLastChangePerSubElement);
	}

	@Override
	public void calculateNextStep() {		
		
		if (this.interdependentInputStates != null) {
			
			double availablePower = 0;
			
			if (ancillaryMeterState != null) {
				
				// #1
//				double chpFeedIn = 0;
//				double pvFeedIn = 0;
//				if (ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
//					chpFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPower());
//				}
//				if (ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null) {
//					pvFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPower());
//				}
//				availablePower = (int) (chpFeedIn + pvFeedIn);
				
				// #2
					availablePower = (int) ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
					
//					if (availablePower > 1500) {
//						@SuppressWarnings("unused")
//						int xxx = 0;
//					}
			}
			
			// get temperature of tank
			double temperature = interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
			
			// update state
			this.model.updateAvailablePower(this.interdependentTime, availablePower, temperature);
			
			// update interdependentOutputStates
			this.internalInterdependentOutputStates.setPower(
					Commodity.ACTIVEPOWER, this.model.getPower());
			this.internalInterdependentOutputStates.setPower(
					Commodity.HEATINGHOTWATERPOWER, -this.model.getPower());
			setOutputStates(internalInterdependentOutputStates);
			
			if (lp != null) {
				lp.setLoad(Commodity.ACTIVEPOWER, interdependentTime, (int) this.model.getPower());
				lp.setLoad(Commodity.HEATINGHOTWATERPOWER, interdependentTime, (int) -this.model.getPower());
			}
		}
		else {
			getGlobalLogger().logDebug("interdependentInputStates == null");
		}
		
		this.interdependentTime += stepSize;
	}
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		if (lp != null) {
			if (lp.getEndingTimeOfProfile() > 0) {
				lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, 0);
//				lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, 0);
				lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
			}
			return new Schedule(lp.getCompressedProfile(compressionType, compressionValue, compressionValue), 0, this.getDeviceType().toString());
		} else {
			return new Schedule(new SparseLoadProfile(), 0, this.getDeviceType().toString());
		}
	}

	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getReferenceTime() + "] SmartHeaterNonControllableIPP setTemperature=" + temperatureSetting + " initialState=" + initialState;
	}
}