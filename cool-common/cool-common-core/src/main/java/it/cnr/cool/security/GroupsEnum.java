package it.cnr.cool.security;


public enum GroupsEnum {
	CONCORSI("GROUP_CONCORSI");

    private final String value;

    GroupsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
	
}
