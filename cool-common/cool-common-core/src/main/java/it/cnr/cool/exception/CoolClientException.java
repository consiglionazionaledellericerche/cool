package it.cnr.cool.exception;

public class CoolClientException extends RuntimeException {

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
