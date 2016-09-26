package osh.cal;

import osh.core.exceptions.OSHException;

/**
 * the observer in the design pattern
 * @author Florian Allerding
 * 
 */
public interface IComDataSubscriber {
	public void onDataFromCALDriver(ICALExchange exchangeObject) throws OSHException;
}
