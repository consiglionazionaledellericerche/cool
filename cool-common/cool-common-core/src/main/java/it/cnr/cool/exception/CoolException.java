package it.cnr.cool.exception;

public class CoolException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -508395529009828599L;

	public CoolException() {
	}

	public CoolException(String message) {
		super(message);
	}

	public CoolException(Throwable cause) {
		super(cause);
	}

	public CoolException(String message, Throwable cause) {
		super(message, cause);
	}

}
