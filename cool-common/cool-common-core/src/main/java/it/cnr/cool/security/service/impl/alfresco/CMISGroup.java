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

import java.io.Serializable;

public class CMISGroup implements Serializable {
	private String group_name;
	private String display_name;
	
	public CMISGroup() {
		super();
	}
	
	public CMISGroup(String group_name, String display_name) {
		super();
		this.group_name = group_name;
		this.display_name = display_name;
	}

	public String getGroup_name() {
		return group_name;
	}
	public void setGroupName(String itemName) {
		this.group_name = itemName;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplayName(String displayName) {
		this.display_name = displayName;
	}
	public String getItemName() {
		return group_name;
	}
	public void setItemName(String itemName) {
		this.group_name = itemName;
	}

}
