package osh.simulation.exception;

/**
 * Exception class for the global simulation engine
 * @author ???
 * @category smart-home ControllerBox Simulation
 */
public class GlobalSimulationEngineException extends SimulationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GlobalSimulationEngineException() {
		super();
	}

	public GlobalSimulationEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public GlobalSimulationEngineException(String message) {
		super(message);
	}

	public GlobalSimulationEngineException(Throwable cause) {
		super(cause);
	}

}
