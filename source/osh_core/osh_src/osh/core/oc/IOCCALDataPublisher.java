package osh.core.oc;

import osh.cal.ICALExchange;
import osh.cal.exceptions.CALDriverException;
import osh.core.exceptions.OSHException;

/**
 * the class means the subject in the observer pattern for the CAL
 * 
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public interface IOCCALDataPublisher {
	public void setOcDataSubscriber(IOCCALDataSubscriber monitorObject);
	public void removeOcDataSubscriber(IOCCALDataSubscriber monitorObject);
	
	public void updateOcDataSubscriber(ICALExchange calexchange) throws CALDriverException, OSHException;
}
