package it.cnr.cool.cmis.model;

public enum FolderType {
	CMIS_FOLDER("cmis:folder", "cmis:folder");
	
    private final String value;
    private final String queryName;

    FolderType(String v, String queryName) {
        value = v;
        this.queryName = queryName;
    }

    public String value() {
        return value;
    }

    public String queryName() {
        return queryName;
    }

    public static FolderType fromValue(String v) {
        for (FolderType c : FolderType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
