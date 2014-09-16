package it.cnr.cool.web.scripts.exception;


public class StatusApplicationException extends Exception {
	private static final long serialVersionUID = 1L;

	public static int MESSAGE_CODE = 1001;
	private Status status;

	public StatusApplicationException(String message) {
		this(MESSAGE_CODE, message);
	}
	
	public StatusApplicationException(int code, String message) {
		status = new Status();
		status.setCode(code);
		status.setMessage(message);
		status.setRedirect(true);
	}

	public Status getStatus() {
		return status;
	}
	
}
