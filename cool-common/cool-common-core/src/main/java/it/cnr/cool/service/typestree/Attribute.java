package it.cnr.cool.service.typestree;


public class Attribute {
	
	private String id;
	private String queryName;
	private String description;
	private String freeSearchSetName;
	private String displayName;	// XXX: utilizzarlo in un tooltip ??? 
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFreeSearchSetName() {
		return freeSearchSetName;
	}
	public void setFreeSearchSetName(String freeSearchSetName) {
		this.freeSearchSetName = freeSearchSetName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
