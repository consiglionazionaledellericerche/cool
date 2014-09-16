package it.cnr.cool.cmis.model;

public enum DocumentType {

    CMIS_DOCUMENT("cmis:document", "cmis:document");

    private final String value;
    private final String queryName;

    DocumentType(String v, String queryName) {
        value = v;
        this.queryName = queryName;
    }

    public String value() {
        return value;
    }

    public String queryName() {
        return queryName;
    }

    public static DocumentType fromValue(String v) {
        for (DocumentType c : DocumentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
