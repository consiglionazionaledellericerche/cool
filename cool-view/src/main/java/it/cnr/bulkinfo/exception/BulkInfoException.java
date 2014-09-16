package it.cnr.bulkinfo.exception;

public class BulkInfoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -229428488095613436L;

	public BulkInfoException() {
	}

	public BulkInfoException(String message) {
		super(message);
	}

	public BulkInfoException(Throwable cause) {
		super(cause);
	}

	public BulkInfoException(String message, Throwable cause) {
		super(message, cause);
	}

}
