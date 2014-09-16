package it.cnr.cool.cmis.service;

public class LoginException extends Exception {

	private static final long serialVersionUID = 5324944578406003929L;

	public LoginException() {
	}

	public LoginException(String message) {
		super(message);
	}

	public LoginException(Throwable cause) {
		super(cause);
	}

	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}

}
