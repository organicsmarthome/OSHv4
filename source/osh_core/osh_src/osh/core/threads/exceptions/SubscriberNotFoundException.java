package osh.core.threads.exceptions;

/**
 * 
 * @author Till Schuberth
 *
 */
public class SubscriberNotFoundException extends Exception {

	private static final long serialVersionUID = -6895306986311063509L;

	public SubscriberNotFoundException() {
	}

	public SubscriberNotFoundException(String message) {
		super(message);
	}

	public SubscriberNotFoundException(Throwable cause) {
		super(cause);
	}

	public SubscriberNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
