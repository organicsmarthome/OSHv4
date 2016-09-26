package osh.core.oc;

import osh.core.exceptions.OSHException;
import osh.eal.hal.exchange.IHALExchange;

/**
 * the observer in the design pattern
 * 
 * @author Florian Allerding
 */
public interface IOCHALDataSubscriber {
	
	public void onDataFromOcComponent(IHALExchange exchangeObject) throws OSHException;
	
}
