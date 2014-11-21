package it.cnr.cool.exception;

public class CoolClientException extends CoolException {
	private static final long serialVersionUID = 1L;

	public CoolClientException() {
	}

	public CoolClientException(String message) {
		super(message);
	}

	public CoolClientException(Throwable cause) {
		super(cause);
	}

	public CoolClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
