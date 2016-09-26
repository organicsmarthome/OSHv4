package osh.core.exceptions;

import osh.core.logging.OSHLoggerCore;

/**
 * superclass for exceptions near the controllerbox/OSH
 * 
 * @author Florian Allerding
 */
public class OSHException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OSHException() {
		super(" I'm sorry, Dave. I'm afraid I can't do that. ");
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An unknown error near the Controller occured..." + "I'm sorry, Dave. I'm afraid I can't do that. ");
		}
	}

	public OSHException(String message) {
		super(" I'm sorry, Dave. I'm afraid I can't do that..... " + message);
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An error near the Controller occured: " +message);
		}
	}

	public OSHException(Throwable cause) {
		super(" I'm sorry, Dave. I'm afraid I can't do that. ", cause);
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An error near the Controller occured...");
		}
	}

	public OSHException(String message, Throwable cause) {
		super(" I'm sorry, Dave. I'm afraid I can't do that..... " + message, cause);
		if (OSHLoggerCore.cb_Main_Logger != null) {
			OSHLoggerCore.cb_Main_Logger.error("An error near the Controller occured: " +message);
		}
	}

}
