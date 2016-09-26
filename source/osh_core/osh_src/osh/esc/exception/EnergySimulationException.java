package osh.esc.exception;

import osh.core.exceptions.OSHException;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class EnergySimulationException extends OSHException {

	private static final long serialVersionUID = 1L;

	public EnergySimulationException() {
		super();
	}

	public EnergySimulationException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnergySimulationException(String message) {
		super(message);
	}

	public EnergySimulationException(Throwable cause) {
		super(cause);
	}
	
}
