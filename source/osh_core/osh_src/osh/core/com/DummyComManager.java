package osh.core.com;

import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.interfaces.IOSHOC;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class DummyComManager extends ComManager {

	public DummyComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		//NOTHING
	}

}
