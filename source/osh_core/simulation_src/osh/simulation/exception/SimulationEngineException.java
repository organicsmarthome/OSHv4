package osh.simulation.exception;

/**
 * Exception class for the simulation engine
 * 
 * @author Florian Allerding
 */
public class SimulationEngineException extends SimulationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimulationEngineException() {
		super();
	}

	public SimulationEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimulationEngineException(String message) {
		super(message);
	}

	public SimulationEngineException(Throwable cause) {
		super(cause);
	}

}