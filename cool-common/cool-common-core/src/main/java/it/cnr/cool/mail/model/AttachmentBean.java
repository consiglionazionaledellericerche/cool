package it.cnr.cool.mail.model;

import java.io.Serializable;
import java.util.Arrays;

public class AttachmentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] attachmentByte;
	private String fileName;
	private boolean inline = false;
	private String contentType;
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

	public boolean isInline() {
		return inline;
	}

	public AttachmentBean setInline(boolean inline) {
		this.inline = inline;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public AttachmentBean setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
}
