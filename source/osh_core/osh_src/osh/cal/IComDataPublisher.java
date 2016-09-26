package osh.cal;

import osh.core.exceptions.OSHException;
import osh.eal.hal.exceptions.HALDriverException;

/**
 * the class means the subject in the observer pattern for the HAL
 * @author Florian Allerding
 * 
 */
public interface IComDataPublisher {
	public void setComDataSubscriber(IComDataSubscriber monitorObject);
	public void removeComDataSubscriber(IComDataSubscriber monitorObject);
	
	public void updateComDataSubscriber(ICALExchange halexchange) throws HALDriverException, OSHException;
}
