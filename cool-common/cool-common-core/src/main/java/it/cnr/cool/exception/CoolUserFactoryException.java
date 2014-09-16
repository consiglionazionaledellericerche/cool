package it.cnr.cool.exception;

public class CoolUserFactoryException extends Exception {

	private static final long serialVersionUID = 5198158057151202552L;

	public CoolUserFactoryException() {
	}

	public CoolUserFactoryException(String message) {
		super(message);
	}

	public CoolUserFactoryException(Throwable cause) {
		super(cause);
	}

	public CoolUserFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
