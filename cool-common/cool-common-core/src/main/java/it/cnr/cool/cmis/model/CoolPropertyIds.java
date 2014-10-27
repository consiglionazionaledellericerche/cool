package it.cnr.cool.cmis.model;

public enum CoolPropertyIds {

	
	ALFCMIS_NODEREF("alfcmis:nodeRef"),
	/**
	 * Property of Documents
	 */
    LOGGER_TYPE_NAME("D:logger:document"),
    LOGGER_QUERY_NAME("logger:document"),
    LOGGER_USER("logger:user"),
    LOGGER_TYPE("logger:type"),
    LOGGER_CODE("logger:codice"),
    LOGGER_APPLICATION("logger:application"),
    
	NOTICE_TYPE_NAME("D:avvisi:document"),
	NOTICE_QUERY_NAME("avvisi:document"),
	NOTICE_TEXT("avvisi:text"),
	NOTICE_STYLE("avvisi:style"),
	NOTICE_TYPE("avvisi:type"),
	NOTICE_TITLE("avvisi:title"),
	NOTICE_DATA("avvisi:data"),
	NOTICE_NUMBER("avvisi:number"),
	NOTICE_SCADENZA("avvisi:dataScadenza"),
	NOTICE_AUTHORITY("avvisi:authority"),
	
	FAQ_TYPE_NAME("D:faq:document"),
	FAQ_QUERY_NAME("faq:document"),
	FAQ_TYPE("faq:type"),
	FAQ_QUESTION("faq:question"),
	FAQ_ANSWER("faq:answer"),
    FAQ_DATA("faq:data"),
    FAQ_NUMBER("faq:number"),
    FAQ_SHOW("faq:show");
    
    private final String value;

    CoolPropertyIds(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CoolPropertyIds fromValue(String v) {
        for (CoolPropertyIds c : CoolPropertyIds.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
