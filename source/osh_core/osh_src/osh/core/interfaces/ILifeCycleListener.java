package osh.core.interfaces;

import osh.core.exceptions.OSHException;

/**
 * Interface for life-cycle-methods
 * necessary for the participation on the life-cycle-manager process
 * @author Ingo Mauser
 */
public interface ILifeCycleListener  {

	/**
	 * invoked when the system starts running
	 * ...in case of use please override
	 */
	public void onSystemRunning() throws OSHException;
	
	/**
	 * invoked when the system is going down
	 * in case of a simulation it might be usefull when you want to write your data...
	 * ...in case of use please override
	 */
	public void onSystemShutdown() throws OSHException;
	
	/**
	 * invoked when the complete OC-units, the HAL and all the drivers are up
	 * ...in case of use please override 
	 */
	public void onSystemIsUp() throws OSHException;
	
	/**
	 * invoked when the system has to make a pause...
	 * ...in case of use please override
	 */
	public void onSystemHalt() throws OSHException;
	
	/**
	 * invoked when the system resumes from the pause
	 * ...in case of use please override
	 */
	public void onSystemResume() throws OSHException;
	
	/**
	 * invoked on a critical error to create perhaps a post mortem image for debugging
	 * ...in case of use please override
	 */
	public void onSystemError() throws OSHException;
}
