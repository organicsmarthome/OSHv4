package osh.mgmt.ipp.heating;

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
public class SpaceHeatingNonControllableIPP 
					extends HotWaterDemandNonControllableIPP {
	
	private static final long serialVersionUID = -1011574853269626608L;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SpaceHeatingNonControllableIPP(
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
	protected SpaceHeatingNonControllableIPP() {
		super();
	}	


	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getTimestamp() + "] SpaceHeatingDemandNonControllableIPP";
	}
}