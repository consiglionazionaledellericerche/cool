package it.cnr.bulkinfo.cool.exception;

public class BulkinfoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -229428488095613436L;

	public BulkinfoException() {
	}

	public BulkinfoException(String message) {
		super(message);
	}

	public BulkinfoException(Throwable cause) {
		super(cause);
	}

	public BulkinfoException(String message, Throwable cause) {
		super(message, cause);
	}

}
