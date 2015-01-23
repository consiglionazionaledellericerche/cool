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
