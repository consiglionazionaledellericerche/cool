package it.cnr.cool.web.scripts.exception;


public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public static int MESSAGE_CODE = 1002;
	private Status status;

	public ValidationException(String message) {
		this(MESSAGE_CODE, message);
	}
	
	public ValidationException(int code, String message) {
		status = new Status();
		status.setCode(code);
		status.setMessage(message);
		status.setRedirect(true);
	}

	public Status getStatus() {
		return status;
	}
	
}
