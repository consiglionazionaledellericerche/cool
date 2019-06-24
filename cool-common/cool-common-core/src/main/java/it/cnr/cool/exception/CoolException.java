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

package it.cnr.cool.exception;

import org.apache.commons.httpclient.HttpStatus;

public class CoolException extends RuntimeException {

	private static final long serialVersionUID = -508395529009828599L;
	private final int status;
	public CoolException() {
		status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	public CoolException(String message) {
		this(message, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public CoolException(String message, int status) {
		super(message);
		this.status = status;
	}

	public CoolException(Throwable cause) {
		this(cause, HttpStatus.SC_INTERNAL_SERVER_ERROR);	
	}

	public CoolException(Throwable cause, int status) {
		super(cause);
		this.status = status;	
	}

	public CoolException(String message, Throwable cause) {
		this(message, cause, HttpStatus.SC_INTERNAL_SERVER_ERROR);	
	}

	public CoolException(String message, Throwable cause, int status) {
		super(message, cause);
		this.status = status;	
	}
	
	public int getStatus() {
		return status;
	}

}
