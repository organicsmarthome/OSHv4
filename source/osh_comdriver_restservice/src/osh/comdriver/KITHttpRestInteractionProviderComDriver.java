package osh.comdriver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;


/**
 * 
 * @author Kaibin Bao
 *
 */
public class KITHttpRestInteractionProviderComDriver extends HttpRestInteractionProviderBusDriver {

	public KITHttpRestInteractionProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
	}

	@Override
	String getEnvironment() {
		return "kit";
	}

}
