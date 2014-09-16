package it.cnr.bulkinfo.exception;

public class BulkInfoNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -944194476074241295L;

	public BulkInfoNotFoundException() {
	}

	public BulkInfoNotFoundException(String message) {
		super(message);
	}

	public BulkInfoNotFoundException(Throwable cause) {
		super(cause);
	}

	public BulkInfoNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
