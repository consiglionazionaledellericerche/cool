package it.cnr.cool.security;


public enum PermissionEnum {
	CMIS_ALL("cmis:all"),
    CMIS_READ("cmis:read"),
    CMIS_WRITE("cmis:write"),
	COORDINATOR("{http://www.alfresco.org/model/content/1.0}cmobject.Coordinator"),
    COLLABORATOR("{http://www.alfresco.org/model/content/1.0}cmobject.Collaborator"),
    CONTRIBUTOR("{http://www.alfresco.org/model/content/1.0}cmobject.Contributor"),
    EDITOR("{http://www.alfresco.org/model/content/1.0}cmobject.Editor"),
    CONSUMER("{http://www.alfresco.org/model/content/1.0}cmobject.Consumer"),
    ALFRESCO_ALL("{http://www.alfresco.org/model/security/1.0}All.All");

    private final String value;

    PermissionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
	
}
