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

package it.cnr.bulkinfo.exception;

public class BulkinfoNameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5392514272061620836L;

	
	public BulkinfoNameException() {
	}

	public BulkinfoNameException(String message) {
		super(message);
	}

	public BulkinfoNameException(Throwable cause) {
		super(cause);
	}

	public BulkinfoNameException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
