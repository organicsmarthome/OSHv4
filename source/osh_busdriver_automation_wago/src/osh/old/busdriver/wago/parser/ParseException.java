package osh.old.busdriver.wago.parser;

public class ParseException extends Exception {

	private static final long serialVersionUID = -393606594536719512L;

	public ParseException() {
		super();
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

}
