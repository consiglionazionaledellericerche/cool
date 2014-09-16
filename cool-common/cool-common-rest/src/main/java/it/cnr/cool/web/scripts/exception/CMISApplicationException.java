package it.cnr.cool.web.scripts.exception;

import org.springframework.extensions.webscripts.WebScriptException;

public class CMISApplicationException extends WebScriptException {
	public static int MESSAGE_CODE = 1001;

	public CMISApplicationException(String msgId, Throwable cause) {
		super(MESSAGE_CODE, msgId, cause);
	}
	
	public CMISApplicationException(String msgId) {
		super(MESSAGE_CODE, msgId);
	}
	
	public CMISApplicationException(int status, String msgId) {
		super(status, msgId);
	}

	private static final long serialVersionUID = 1L;

}
