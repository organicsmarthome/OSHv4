package osh.datatypes.registry.oc.ipp;

import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;

/**
 * IPP for devices without any interaction (e.g., appliance is off)
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public abstract class StaticIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction> 
							extends NonControllableIPP<PhenotypeType, PredictionType> {	

	private static final long serialVersionUID = -8858902765784429939L;

	private Schedule schedule;
	private String description;
	
	
	/**
	 * CONSTRUCTOR for serialization usage only, do not use
	 */
	@Deprecated
	protected StaticIPP() {
		super();
	}	
	
	/**
	 * CONSTRUCTOR
	 */
	public StaticIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long timestamp, 
			boolean toBeScheduled,
			long optimizationHorizon, 
			DeviceTypes deviceType, 
			long referenceTime,
			Schedule schedule,
			LoadProfileCompressionTypes compressionType,
			int compressionValue,
			String description) {
		
		super(
				deviceId, 
				logger, 
				toBeScheduled, 
				false, //does not need ancillary meter state as Input State
				false, //does not react to input states
				true, //is static
				referenceTime,
				deviceType,
				new Commodity[]{
				},
				compressionType,
				compressionValue);

		this.schedule = schedule;
		this.description = description;
	}

	
	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean createLoadProfile,
			boolean keepPrediction) {
		this.stepSize = stepSize;
		this.internalInterdependentOutputStates = null;
		setOutputStates(null);
		// do nothing
	}
	
	@Override
	public void calculateNextStep() {
		// do nothing		
	}
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		return schedule;
	}
	
//	@Override
//	public Schedule getSchedule(BitSet solution) {
//		return schedule;
//	}

	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
	}
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getReferenceTime() + "] " + description;
	}
	
}
