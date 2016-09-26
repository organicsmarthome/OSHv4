package osh.datatypes.registry.oc.ipp;

import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;

/**
 * 
 * @author Sebastian Kramer, Ingo Mauser
 * 
 */
public abstract class ControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction> 
							extends InterdependentProblemPart<PhenotypeType, PredictionType> {

	private static final long serialVersionUID = 7350946372573454658L;
	
	/** absolute time in unix time [s] */
	private long optimizationHorizon;
	
	
	/**
	 * CONSTRUCTOR for serialization usage only, do not use
	 */
	@Deprecated
	protected ControllableIPP() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public ControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long timestamp, 
			int bitcount, 
			boolean toBeScheduled,
			boolean needsAncillaryMeterState,
			boolean reactsToInputStates,
			long optimizationHorizon,
			long referenceTime,
			DeviceTypes deviceType,
			Commodity[] allOutputCommodities,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(deviceId, logger, timestamp, bitcount, toBeScheduled, needsAncillaryMeterState, 
				reactsToInputStates, false, referenceTime, deviceType, allOutputCommodities, compressionType, compressionValue);
		this.optimizationHorizon = optimizationHorizon;
	}

	@Override
	public long getOptimizationHorizon() {
		return optimizationHorizon;
	}
	
	public void setOptimizationHorizon(long optimizationHorizon) {
		this.optimizationHorizon = optimizationHorizon;
	}

	public abstract String solutionToString(BitSet bits);
	
	@Override
	public PredictionType transformToFinalInterdependetPrediction(BitSet solution) {
		return null;
	}
	
}
