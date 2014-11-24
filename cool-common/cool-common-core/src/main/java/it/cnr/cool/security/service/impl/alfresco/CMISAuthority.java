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
