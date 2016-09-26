package osh.simulation.exception;

import osh.core.logging.OSHLoggerCore;

/**
 * Exception superclass for the simulation core
 * 
 * @author Florian Allerding
 */
public class SimulationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimulationException() {
		OSHLoggerCore.cb_Main_Logger.error("An unknown error near the Simulation occured...");
	}

	public SimulationException(String message) {
		super(message);
		OSHLoggerCore.cb_Main_Logger.error("An error near the Simulation occured: " +message);
	}

	public SimulationException(Throwable cause) {
		super(cause);
		OSHLoggerCore.cb_Main_Logger.error("An error near the Simulation occured: " +cause.getStackTrace());
	}

	public SimulationException(String message, Throwable cause) {
		super(message, cause);
		OSHLoggerCore.cb_Main_Logger.error("An error near the Simulation occured: " +cause.getStackTrace());
	}

}
