package osh.eal.hal.exceptions;

/**
 * Exception class for the HAL-manager 
 * 
 * @author Florian Allerding
 */
public class HALManagerException extends HALException {

	/**  */
	private static final long serialVersionUID = 1L;

	public HALManagerException() {
		super();
	}

	public HALManagerException(String message) {
		super(message);
	}

	public HALManagerException(Throwable cause) {
		super(cause);
	}

	public HALManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
