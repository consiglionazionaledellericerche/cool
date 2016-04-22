package it.cnr.cool.web.scripts.exception;


public class ClientMessageException extends RuntimeException {
	public static int MESSAGE_CODE = 1002;
	private static final long serialVersionUID = 1L;
	public static ClientMessageException FILE_ALREDY_EXISTS = new ClientMessageException("message.file.alredy.exists");
	public static ClientMessageException FILE_EMPTY = new ClientMessageException("message.error.allegati.empty");
	
	private String keyMessage;

	public ClientMessageException(String message) {
		super(message);
	}

	public String getKeyMessage() {
		return keyMessage;
	}
}