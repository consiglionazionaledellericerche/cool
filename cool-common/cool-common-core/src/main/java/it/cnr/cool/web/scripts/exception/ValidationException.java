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


public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public static int MESSAGE_CODE = 1002;
	private Status status;

	public ValidationException(String message) {
		this(MESSAGE_CODE, message);
	}
	
	public ValidationException(int code, String message) {
		status = new Status();
		status.setCode(code);
		status.setMessage(message);
		status.setRedirect(true);
	}

	public Status getStatus() {
		return status;
	}
	
}
