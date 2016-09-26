package osh.eal.hal.exceptions;

import osh.core.logging.OSHLoggerCore;

/**
 * Exception superclass for the HAL
 * 
 * @author Florian Allerding
 */
public class HALException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HALException() {
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An unknown error near the HAL occured...");
		}
	}

	public HALException(String message) {
		super(message);
	}

	public HALException(Throwable cause) {
		super(cause);
	}

	public HALException(String message, Throwable cause) {
		super(message, cause);
	}

}
