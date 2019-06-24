/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
