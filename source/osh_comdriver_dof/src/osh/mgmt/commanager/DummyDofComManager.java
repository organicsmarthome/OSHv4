package osh.mgmt.commanager;

import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSHOC;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class DummyDofComManager extends ComManager {

	public DummyDofComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		//NOTHING
	}

}
