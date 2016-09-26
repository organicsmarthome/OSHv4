package osh.mgmt.ipp.dhw;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.mgmt.ipp.HotWaterDemandNonControllableIPP;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class DomesticHotWaterNonControllableIPP 
					extends HotWaterDemandNonControllableIPP {
	
	private static final long serialVersionUID = -1011574853269626608L;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public DomesticHotWaterNonControllableIPP(
			UUID deviceId, 
			DeviceTypes deviceType,
			IGlobalLogger logger,
			long now,
			boolean toBeScheduled,
			SparseLoadProfile powerPrediction,
			Commodity usedCommodity,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(
				deviceId, 
				deviceType,
				logger,
				now,
				toBeScheduled,
				powerPrediction,
				usedCommodity,
				compressionType,
				compressionValue);
	}
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected DomesticHotWaterNonControllableIPP() {
		super();
	}	
	
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getTimestamp() + "] DomesticHotWaterDemandNonControllableIPP";
	}
}