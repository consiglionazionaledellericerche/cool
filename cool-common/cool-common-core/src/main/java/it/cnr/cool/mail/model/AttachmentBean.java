package it.cnr.cool.mail.model;

import java.io.Serializable;
import java.util.Arrays;

public class AttachmentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] attachmentByte;
	private String fileName;
	public AttachmentBean() {}
	public AttachmentBean(String fileName, byte[] attachmentByte) {
		this.fileName = fileName;
		this.attachmentByte = Arrays.copyOf(attachmentByte, attachmentByte.length);
	}
	/** ritorna il nome fisico dell'allegato */
	public String getFileName() {
		return fileName;
	}
	/** imposta il nome fisico dell'allegato */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getAttachmentByte() {
		return attachmentByte;
	}
	public void setAttachmentByte(byte[] attachmentByte) {
		this.attachmentByte = Arrays.copyOf(attachmentByte, attachmentByte.length);
	}
}
