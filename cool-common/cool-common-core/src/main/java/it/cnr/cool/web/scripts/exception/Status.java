package it.cnr.cool.web.scripts.exception;

public class Status {

	private int code;
	private String message;
	private boolean redirect;

	public void setCode(int code) {
		this.code = code;

	}

	public void setMessage(String message) {
		this.message = message;

	}

	public void setRedirect(boolean redirect) {
		this.redirect = redirect;

	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean getRedirect() {
		return redirect;
	}

}
