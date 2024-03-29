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

package it.cnr.cool.web.scripts.exception;


import java.util.Objects;

public class ClientMessageException extends RuntimeException {
	public static int MESSAGE_CODE = 1002;
	private static final long serialVersionUID = 1L;
	public static ClientMessageException FILE_ALREDY_EXISTS = new ClientMessageException("message.file.alredy.exists","message.file.alredy.exists");
	public static ClientMessageException FILE_EMPTY = new ClientMessageException("message.error.allegati.empty","message.error.allegati.empty");
	
	private String keyMessage;

	public ClientMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientMessageException(String message) {
		super(message);
	}
	public ClientMessageException(String keyMessage, String message) {
		super(message);
		this.keyMessage = keyMessage;
	}

	public String getKeyMessage() {
		return keyMessage;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClientMessageException that = (ClientMessageException) o;
		return Objects.equals(keyMessage, that.keyMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keyMessage);
	}
}