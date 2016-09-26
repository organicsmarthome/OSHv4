package osh.mgmt.localobserver;

import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class WaterTankLocalObserver extends LocalObserver {
	
	protected final double kWhToWsDivisor = 3600000.0;
	
	protected double temperatureInLastIPP = 0.0;
	
	protected double currentTemperature = 70.0;
	
	protected double currentMinTemperature = 20.0;
	protected double currentMaxTemperature  = 20.0;

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public WaterTankLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}


	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		return null;
	}

}
