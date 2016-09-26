package osh.eal.hal;

import osh.core.exceptions.OSHException;
import osh.eal.hal.exceptions.HALDriverException;
import osh.eal.hal.exchange.IHALExchange;

/**
 * the class means the subject in the observer pattern for the HAL
 * @author Florian Allerding
 * 
 */
public interface IDriverDataPublisher {
	public void setOcDataSubscriber(IDriverDataSubscriber monitorObject);
	public void removeOcDataSubscriber(IDriverDataSubscriber monitorObject);
	
	public void updateOcDataSubscriber(IHALExchange halexchange) throws HALDriverException, OSHException;
}
