package osh.cal.exceptions;

import osh.core.logging.OSHLoggerCore;

/**
 * Exception superclass for the CAL
 * 
 * @author Florian Allerding
 */
public class CALException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CALException() {
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An unknown error near the HAL occured...");
		}
	}

	public CALException(String message) {
		super(message);
	}

	public CALException(Throwable cause) {
		super(cause);
	}

	public CALException(String message, Throwable cause) {
		super(message, cause);
	}

}
