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

package it.cnr.cool.service.frontOffice;

public enum TypeDocument {
	Notice("notice", "notice"), Log("log", "logs"), Faq("faq", "FAQ");

	private final String name;
	private String folder;

	TypeDocument(String name, String folder){
		this.name = name;
		this.folder = folder;
	}
	
	public static TypeDocument fromValue(String name) {
		for (TypeDocument c : TypeDocument.values()) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new IllegalArgumentException("errore con indice = " + name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getFolder() {
		return folder;
	}
}
