package it.cnr.cool.web.scripts.exception;

import org.springframework.extensions.webscripts.WebScriptException;

public class ClientMessageException extends WebScriptException {
	public static int MESSAGE_CODE = 1002;
	private static final long serialVersionUID = 1L;
	public static ClientMessageException FILE_ALREDY_EXISTS = new ClientMessageException("message.file.alredy.exists");
	
	private String keyMessage;
	
	public ClientMessageException(String msgId, Throwable cause) {
		super(MESSAGE_CODE, msgId, cause);
		
	}
	
	public ClientMessageException(String msgId) {
		super(MESSAGE_CODE, msgId);
		this.keyMessage = msgId;
	}
	
	public ClientMessageException(int status, String msgId) {
		super(status, msgId);
	}

	public String getKeyMessage() {
		return keyMessage;
	}

}
