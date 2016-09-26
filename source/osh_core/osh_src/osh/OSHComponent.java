package osh;

import osh.core.OSHRandomGenerator;
import osh.core.interfaces.IOSH;
import osh.core.logging.IGlobalLogger;
import osh.eal.hal.HALRealTimeDriver;

/**
 * Super class for all components managed by the OCManager or HALManager
 * 
 * @author Florian Allerding, Ingo Mauser
 */
public abstract class OSHComponent {
	
	private IOSH theOrganicSmartHome;
	
	
	/**
	 * CONSTRUCTOR
	 * @param theOrganicSmartHome
	 */
	public OSHComponent(IOSH theOrganicSmartHome) {
		super();
		this.theOrganicSmartHome = theOrganicSmartHome;
	}
	

	public IGlobalLogger getGlobalLogger() {
		return theOrganicSmartHome.getLogger();
	}

	public OSHRandomGenerator getRandomGenerator() {
		return theOrganicSmartHome.getRandomGenerator();
	}
	
	protected IOSH getOSH() {
		return theOrganicSmartHome;
	}

	public HALRealTimeDriver getTimer() {
		return theOrganicSmartHome.getTimer();
	}
	
}
