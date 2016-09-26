package osh.core.exceptions;

/**
 * Exception class for the oc manager
 * 
 * @author Florian Allerding
 */
public class OCManagerException extends Exception {

	/**
	 * SV UID
	 */
	private static final long serialVersionUID = 1L;

	
	public OCManagerException() {
		// NOTHING
	}

	public OCManagerException(String arg0) {
		super(arg0);
		// NOTHING
	}

	public OCManagerException(Throwable arg0) {
		super(arg0);
		// NOTHING
	}

	public OCManagerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// NOTHING
	}

}
