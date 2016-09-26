package osh.mgmt.localcontroller.ipp;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.StaticIPP;

/**
 * ENDPROGRAMMED IPP -> nothing to do
 * 
 * @author Ingo Mauser
 *
 */
public class FutureAppliancesStaticIPP extends StaticIPP<ISolution, IPrediction> {
	
	private static final long serialVersionUID = 4075698811201735555L;

	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected FutureAppliancesStaticIPP() {
		super();
	}	
	
	/**
	 * CONSTRUCTOR
	 */
	public FutureAppliancesStaticIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long timestamp, 
			boolean toBeScheduled,
			long optimizationHorizon, 
			DeviceTypes deviceType, 
			long referenceTime,
			Schedule schedule,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger,
				timestamp, 
				toBeScheduled,
				optimizationHorizon, 
				deviceType, 
				referenceTime,
				schedule,
				compressionType,
				compressionValue,
				"FutureStaticApplianceIPP");
	}

}
