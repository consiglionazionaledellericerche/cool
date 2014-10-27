package it.cnr.cool.web.scripts.exception;


public class CMISApplicationException extends RuntimeException {
	public static int MESSAGE_CODE = 1001;
	private static final long serialVersionUID = 1L;

	public CMISApplicationException(String string, Exception e) {
		super(string, e);
	}

	public CMISApplicationException(String string) {
		super(string);
	}

}
