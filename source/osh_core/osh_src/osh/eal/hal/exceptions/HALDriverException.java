package osh.eal.hal.exceptions;

/**
 * Exception class for the HAL-driver
 * 
 * @author Florian Allerding
 */
public class HALDriverException extends HALException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HALDriverException() {
		super();
	}

	public HALDriverException(String message) {
		super(message);
	}

	public HALDriverException(Throwable cause) {
		super(cause);
	}

	public HALDriverException(String message, Throwable cause) {
		super(message, cause);
	}

}
