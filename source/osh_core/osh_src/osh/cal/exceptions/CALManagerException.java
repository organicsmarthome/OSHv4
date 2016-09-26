package osh.cal.exceptions;

/**
 * Exception class for the CAL-manager 
 * 
 * @author Florian Allerding
 */
public class CALManagerException extends CALException {

	/**  */
	private static final long serialVersionUID = 1L;

	public CALManagerException() {
		super();
	}

	public CALManagerException(String message) {
		super(message);
	}

	public CALManagerException(Throwable cause) {
		super(cause);
	}

	public CALManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
