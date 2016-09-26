package osh.simulation.exception;

/**
 * Exception class for simulation subjects
 * 
 * @author Florian Allerding
 */
public class SimulationSubjectException extends SimulationException {

	/** Serial ID */
	private static final long serialVersionUID = 1L;

	public SimulationSubjectException() {
		super();
	}

	public SimulationSubjectException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimulationSubjectException(String message) {
		super(message);
	}

	public SimulationSubjectException(Throwable cause) {
		super(cause);
	}
}
