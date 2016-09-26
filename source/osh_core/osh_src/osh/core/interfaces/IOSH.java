package osh.core.interfaces;

import osh.core.DataBroker;
import osh.core.LifeCycleStates;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.eal.hal.HALRealTimeDriver;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public interface IOSH {

	public boolean isSimulation();
	public IGlobalLogger getLogger();
	public IOSHStatus getOSHstatus();
	
	public HALRealTimeDriver getTimer();
	public OSHRandomGenerator getRandomGenerator();
	
	public LifeCycleStates getCurrentLifeCycleState();
	
	public DataBroker getDataBroker();
	
}
