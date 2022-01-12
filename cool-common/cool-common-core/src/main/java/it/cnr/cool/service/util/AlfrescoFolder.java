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

package it.cnr.cool.service.util;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;


public class AlfrescoFolder {

	private final HashMap<String, Object> attr;
	private final String data;

	private String state;

	public AlfrescoFolder(CmisObject folder, boolean isLeaf) {
		attr = new HashMap<String, Object>();
		attr.put("id", folder.getId());
		attr.put("rel", "folder");
		attr.put("type",folder.getType().getId());
		attr.put(
				"allowableActions",
				Optional.ofNullable(folder.getAllowableActions())
						.map(AllowableActions::getAllowableActions)
						.orElse(Collections.emptySet())
		);
		data = folder.getName();
		if (!isLeaf) {
			state = "closed";
		}
	}

	public AlfrescoFolder(QueryResult folder, boolean isLeaf) {
		attr = new HashMap<String, Object>();
		attr.put("id", folder.getPropertyValueById(PropertyIds.OBJECT_ID));
		attr.put("rel", "folder");
		attr.put("type",
				folder.getPropertyValueById(PropertyIds.OBJECT_TYPE_ID));
		attr.put(
				"allowableActions",
				Optional.ofNullable(folder.getAllowableActions())
						.map(AllowableActions::getAllowableActions)
						.orElse(Collections.emptySet())
		);
		data = folder.getPropertyValueById(PropertyIds.NAME);
		if (!isLeaf) {
			state = "closed";
		}
	}

	public HashMap<String, Object> getAttr() {
		return attr;
	}

	public String getData() {
		return data;
	}

	public String getState() {
		return state;
	}
}