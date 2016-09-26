package osh.mgmt.localobserver;

import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.BacNetThermalExchange;



/**
 * 
 * @author Kaibin Bao
 *
 */
@Deprecated
public class BacNetThermalLocalObserver extends LocalObserver {

	
	public BacNetThermalLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
	}



	@Override
	public void onDeviceStateUpdate() {
		IHALExchange hx = getObserverDataObject();
		if( hx instanceof BacNetThermalExchange ) {
//			getGlobalLogger().logDebug("got thermal information: " + hx.toString());
		}
	}



	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		return null;
	}



}
