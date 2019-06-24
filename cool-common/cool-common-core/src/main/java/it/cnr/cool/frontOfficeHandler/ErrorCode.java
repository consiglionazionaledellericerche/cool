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
