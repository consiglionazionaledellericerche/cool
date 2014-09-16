package it.cnr.cool.frontOfficeHandler;

public enum ErrorCode {
	AjaxError(1,true), AjaxDelay(2,false), Browser(3,false), AssertionFailure(4,false);
	
	private final int code;
	
	private final boolean alfrescoWrite;
	
	public int getCode() {
		return code;
	}

	public boolean isAlfrescoWrite() {
		return alfrescoWrite;
	}

	ErrorCode(int code, boolean alfrescoWrite){
		this.code = code;
		this.alfrescoWrite = alfrescoWrite;
	}

	public static ErrorCode fromValue(int index) {
		for (ErrorCode c : ErrorCode.values()) {
			if (c.getCode() == index) {
				return c;
			}
		}
		throw new IllegalArgumentException("errore con indice = " + index);
	}
}
