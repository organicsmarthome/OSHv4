package osh.eal.hal;

import osh.core.exceptions.OSHException;
import osh.eal.hal.exchange.IHALExchange;

/**
 * the observer in the design pattern
 * @author Florian Allerding
 * 
 */
public interface IDriverDataSubscriber {
	public void onDataFromCALDriver(IHALExchange exchangeObject) throws OSHException;
}
