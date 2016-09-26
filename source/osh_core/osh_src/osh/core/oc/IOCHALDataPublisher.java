package osh.core.oc;

import osh.core.exceptions.OSHException;
import osh.eal.hal.exceptions.HALDriverException;
import osh.eal.hal.exchange.IHALExchange;

/**
 * the class means the subject in the observer pattern for the HAL
 * 
 * @author Florian Allerding
 */
public interface IOCHALDataPublisher {
	public void setOcDataSubscriber(IOCHALDataSubscriber monitorObject);
	public void removeOcDataSubscriber(IOCHALDataSubscriber monitorObject);
	
	public void updateOcDataSubscriber(IHALExchange halexchange) throws HALDriverException, OSHException;
}
