package it.cnr.cool.service.util;

import java.util.HashMap;

import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;



public class AlfrescoFolder {

	private final HashMap<String, Object> attr;
	private final String data;

	private String state;

	public AlfrescoFolder(QueryResult folder, boolean isLeaf) {
		attr = new HashMap<String, Object>();
		attr.put("id", folder.getPropertyValueById(PropertyIds.OBJECT_ID));
		attr.put("rel", "folder");
		attr.put("type",
				folder.getPropertyValueById(PropertyIds.OBJECT_TYPE_ID));
		attr.put("allowableActions", folder.getAllowableActions()
				.getAllowableActions());
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