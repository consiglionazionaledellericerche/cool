package it.cnr.cool.exception;

import org.apache.commons.httpclient.HttpStatus;

public class CoolException extends RuntimeException {

	private static final long serialVersionUID = -508395529009828599L;
	private final int status;
	public CoolException() {
		status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	public CoolException(String message) {
		this(message, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public CoolException(String message, int status) {
		super(message);
		this.status = status;
	}

	public CoolException(Throwable cause) {
		this(cause, HttpStatus.SC_INTERNAL_SERVER_ERROR);	
	}

	public CoolException(Throwable cause, int status) {
		super(cause);
		this.status = status;	
	}

	public CoolException(String message, Throwable cause) {
		this(message, cause, HttpStatus.SC_INTERNAL_SERVER_ERROR);	
	}

	public CoolException(String message, Throwable cause, int status) {
		super(message, cause);
		this.status = status;	
	}
	
	public int getStatus() {
		return status;
	}

}
