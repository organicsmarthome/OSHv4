package osh.datatypes.registry.oc.ipp;

import java.io.Serializable;
import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.esc.IOCEnergySubject;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public abstract class InterdependentProblemPart<PhenotypeType extends ISolution, PredictionType extends IPrediction> 
							extends StateExchange 
							implements IOCEnergySubject, Serializable {

	private static final long serialVersionUID = 1852491971934065038L;
	
	private UUID deviceID;
	
	public IGlobalLogger logger;

	private int bitcount;
	
	private boolean toBeScheduled;
	
	/** flag if problem part needs ancillary meter state as an input state */
	private boolean needsAncillaryMeterState;
	
	/** flag if problem part needs input (is interdependent and thus dynamic), 
	 * if false part will not receive any input states */
	private boolean reactsToInputStates;
	
	/** flag if the problem part is completely static and will provide no load values */
	private boolean isCompletelyStatic;
	
	protected LimitedCommodityStateMap interdependentInputStates;
	//"Real" output states that will be given to the ESC
	private LimitedCommodityStateMap interdependentOutputStates;
	//states for internal use (so we dont have to constantly instantiate new Maps (time intensive)
	protected LimitedCommodityStateMap internalInterdependentOutputStates;
	
	protected Commodity[] allOutputCommodities;
	//as most ipps will not require input commodities we can leave that as an empty array and overwrite when necessary
	protected Commodity[] allInputCommodities = new Commodity[]{};
	
	protected AncillaryMeterState ancillaryMeterState;
	
	private long referenceTime;
	protected long interdependentTime;
	protected int stepSize;
	
	private int id;
	
	// necessary to distinguish PV power / CHP power / device power / appliance power for pricing 
	private DeviceTypes deviceType; 
	
	//How to compress the load profiles used
	protected LoadProfileCompressionTypes compressionType;
	protected int compressionValue;
	

	/** for Serialization, never use */
	@Deprecated
	protected InterdependentProblemPart() {
		super();
		this.logger = null;
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public InterdependentProblemPart(
			UUID deviceId, 
			IGlobalLogger logger,
			long timestamp, 
			int bitcount, 
			boolean toBeScheduled,
			boolean needsAncillaryMeterState,
			boolean reactsToInputStates,
			boolean isCompletelyStatic,
			long referenceTime,
			DeviceTypes deviceType,
			Commodity[] allOutputCommodities,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(deviceId, timestamp);
		
		this.deviceID = deviceId;
		
		this.logger = logger;
		
		this.bitcount = bitcount;
		this.toBeScheduled = toBeScheduled;
		this.needsAncillaryMeterState = needsAncillaryMeterState;
		this.reactsToInputStates = reactsToInputStates;
		this.isCompletelyStatic = isCompletelyStatic;
		this.referenceTime = referenceTime;
		this.deviceType = deviceType;
		this.allOutputCommodities = allOutputCommodities;
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
		this.internalInterdependentOutputStates = new LimitedCommodityStateMap(allOutputCommodities);
	}

	
//	/**
//	 * Get schedule without interdependencies with other devices, 
//	 * i.e., estimate the behavior!<br>
//	 * <br>
//	 * IMPORTANT: Normally, use getFinalInterdependentSchedule() after
//	 * doing the simulation stepwise.
//	 */
//	@Deprecated
//	public abstract Schedule getSchedule(BitSet solution);
	
	/**
	 * Transform to phenotype without interdependencies with other devices, 
	 * i.e., estimate the behavior!
	 * <br>
	 * IMPORTANT: Normally, use getFinalInterdependentSchedule() after
	 * doing the simulation stepwise.
	 */	
	public abstract ISolution transformToPhenotype(BitSet solution);
	
	
	/**
	 * Get final (full, complete) schedule that depends on other devices
	 * and remove other schedules
	 */
	public abstract Schedule getFinalInterdependentSchedule();
	
	
	/**
	 * Get final phenotype that depends on other devices
	 */
	public final EASolutionCommandExchange<PhenotypeType> transformToFinalInterdependetPhenotype(
			UUID sender, 
			UUID receiver, 
			long timestamp,
			BitSet solution) {
		if (solution.length() > this.getBitCount()) {
			logger.logError("Bitcount mismatch! Should be: " + this.getBitCount() + " but is: " + solution.size());
		}
		return new EASolutionCommandExchange<PhenotypeType>(
				sender, 
				receiver, 
				timestamp, 
				transformToFinalInterdependetPhenotype(solution));
	}
	
	/**
	 * Get final phenotype that depends on other devices
	 */
	public abstract PhenotypeType transformToFinalInterdependetPhenotype(BitSet solution);
	
	
	/**
	 * Get final prediction that depends on other devices
	 */
	public final EAPredictionCommandExchange<PredictionType> transformToFinalInterdependetPrediction(
			UUID sender, 
			UUID receiver, 
			long timestamp,
			BitSet solution) {
		if (solution.length() > this.getBitCount()) {
			logger.logError("Bitcount mismatch! Should be: " + this.getBitCount() + " but is: " + solution.size());
		}
		return new EAPredictionCommandExchange<PredictionType>(
				sender, 
				receiver, 
				timestamp, 
				transformToFinalInterdependetPrediction(solution));
	}

	/**
	 * Get final prediction that depends on other devices
	 */
	public abstract PredictionType transformToFinalInterdependetPrediction(BitSet solution);
	

	/**
	 * Is invoked before every reschedule:
	 * the IPP can adapts its current encoding to the current time
	 * (e.g., reduce number of bits and change control model)
	 * 
	 * @param currentTime - absolute time
	 * @param maxHorizon - absolute time
	 */
	public abstract void recalculateEncoding(long currentTime, long maxHorizon);
	
	@Override
	public void setCommodityInputStates(LimitedCommodityStateMap inputStates,
			AncillaryMeterState ancillaryMeterState) throws EnergySimulationException {
		this.interdependentInputStates = inputStates;
		this.ancillaryMeterState = ancillaryMeterState;
		
	}

	@Override
	public LimitedCommodityStateMap getCommodityOutputStates() throws EnergySimulationException {
		return interdependentOutputStates;
	}
	
	public void setOutputStates(LimitedCommodityStateMap states) {
		this.interdependentOutputStates = states;
	}
	
	// Getters & Setters	
	
	/** returns the number of bits. Don't change this value while scheduling! */
	public final int getBitCount() {
		return bitcount;
	}

	public void setBitCount(int bitcount) {
		this.bitcount = bitcount;
	}	
	
	public IGlobalLogger getGlobalLogger() {
		return logger;
	}

	public boolean isToBeScheduled() {
		return toBeScheduled;
	}
	
	@Override
	public boolean isNeedsAncillaryMeterState() {
		return needsAncillaryMeterState;
	}

	@Override
	public boolean isReactsToInputStates() {
		return reactsToInputStates;
	}
	
	@Override
	public void prepareForDeepCopy() {
		
	}
	
	public boolean isCompletelyStatic() {
		return isCompletelyStatic;
	}
	
	public Commodity[] getAllOutputcommodities() {
		return allOutputCommodities;
	}
	
	public Commodity[] getAllInputcommodities() {
		return allInputCommodities;
	}

	public long getReferenceTime() {
		return referenceTime;
	}
	
	public void setReferenceTime(long referenceTime) {
		this.referenceTime = referenceTime;
	}

	public DeviceTypes getDeviceType() {
		return deviceType;
	}

	@Override
	public UUID getDeviceID() {
		return deviceID;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public abstract String problemToString();

	@Override
	public String toString() {
		return problemToString();
	}
	
}
