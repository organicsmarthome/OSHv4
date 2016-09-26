package osh.mgmt.localobserver;

import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolLocalObserver extends LocalObserver {

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public BcontrolLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}


	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		//NOTHING
	}

	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		return null;
	}
	
}
