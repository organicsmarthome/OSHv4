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
public abstract class NonControllableIPP<PhenotypeType extends ISolution, PredictionType extends IPrediction> 
							extends InterdependentProblemPart<PhenotypeType, PredictionType> {	

	private static final long serialVersionUID = -6744029462291912653L;
	

	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected NonControllableIPP() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public NonControllableIPP (
			UUID deviceId, 
			IGlobalLogger logger,
			boolean toBeScheduled,
			boolean needsAncillaryMeterState,
			boolean reactsToInputStates,
			boolean isCompletelyStatic,
			long referenceTime,
			DeviceTypes deviceType,
			Commodity[] allOutputCommodities,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(
				deviceId, 
				logger, 
				referenceTime, 
				0, 
				toBeScheduled, 
				needsAncillaryMeterState, 
				reactsToInputStates,
				isCompletelyStatic,
				referenceTime, 
				deviceType,
				allOutputCommodities,
				compressionType, 
				compressionValue);
	}
	
	
	@Override
	public final void setBitCount(int bitcount) {
		if (bitcount != 0) throw new IllegalArgumentException("bitcount != 0");
	}
	
	@Override
	public final PhenotypeType transformToPhenotype(BitSet solution) {
		return null;
	}
	
	@Override
	public final PhenotypeType transformToFinalInterdependetPhenotype(BitSet solution) {
		return null;
	}
	
	@Override
	public PredictionType transformToFinalInterdependetPrediction(BitSet solution) {
		return null;
	}
	
	@Override
	public long getOptimizationHorizon() {
		return 0;
	}
}
