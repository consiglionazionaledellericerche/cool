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

package it.cnr.cool.security.service.impl.alfresco;

public class CMISAuthority {
	private String authorityType;
	private String shortName;
	private String fullName;
	private String displayName;
		
	public CMISAuthority() {
		super();
	}
	public CMISAuthority(String authorityType, String shortName,
			String fullName, String displayName) {
		super();
		this.authorityType = authorityType;
		this.shortName = shortName;
		this.fullName = fullName;
		this.displayName = displayName;
	}
	public String getAuthorityType() {
		return authorityType;
	}
	public void setAuthorityType(String authorityType) {
		this.authorityType = authorityType;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CMISAuthority))
			return false;
		CMISAuthority cmisAuthority = (CMISAuthority)obj;
		if (cmisAuthority.getAuthorityType().equals(authorityType) && cmisAuthority.getShortName().equals(shortName))
			return true;
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return shortName.hashCode();
	}
}
