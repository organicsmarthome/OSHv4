package osh.cal.exceptions;

/**
 * Exception class for the CAL-driver
 * 
 * @author Florian Allerding
 */
public class CALDriverException extends CALException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CALDriverException() {
		super();
	}

	public CALDriverException(String message) {
		super(message);
	}

	public CALDriverException(Throwable cause) {
		super(cause);
	}

	public CALDriverException(String message, Throwable cause) {
		super(message, cause);
	}

}
