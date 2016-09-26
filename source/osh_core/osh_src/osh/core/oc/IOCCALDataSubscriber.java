package osh.core.oc;

import osh.cal.ICALExchange;
import osh.core.exceptions.OSHException;

/**
 * the observer in the design pattern
 * 
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public interface IOCCALDataSubscriber {
	
	public void onDataFromOcComponent(ICALExchange exchangeObject) throws OSHException;
	
}
