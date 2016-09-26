package osh.core.exceptions;

/**
 * Exception class for for local and global o/c-units 
 * 
 * @author Florian Allerding
 */
public class OCUnitException extends OSHException {

	/**  */
	private static final long serialVersionUID = 1L;

	public OCUnitException() {
		super();
	}

	public OCUnitException(String message) {
		super(message);
	}

	public OCUnitException(Throwable cause) {
		super(cause);
	}

	public OCUnitException(String message, Throwable cause) {
		super(message, cause);
	}

}
