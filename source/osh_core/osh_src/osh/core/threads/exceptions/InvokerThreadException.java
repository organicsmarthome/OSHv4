package osh.core.threads.exceptions;

/**
 * 
 * @author Till Schuberth
 *
 */
public class InvokerThreadException extends Exception {

	private static final long serialVersionUID = -6723737922475608209L;

	
	public InvokerThreadException() {
	}

	public InvokerThreadException(String message) {
		super(message);
	}

	public InvokerThreadException(Throwable cause) {
		super(cause);
	}

	public InvokerThreadException(String message, Throwable cause) {
		super(message, cause);
	}

}
