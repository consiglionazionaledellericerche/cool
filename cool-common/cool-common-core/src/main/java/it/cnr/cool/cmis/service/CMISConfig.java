package it.cnr.cool.cmis.service;

import java.util.Map;

public class CMISConfig {
    private static final long serialVersionUID = -7240662670935682511L;
    public static final String GUEST_USERNAME = "user.guest.username";
    public static final String GUEST_PASSWORD = "user.guest.password";
    public static final String ADMIN_USERNAME = "user.admin.username";
    public static final String ADMIN_PASSWORD = "user.admin.password";
    private final Map<String, String>  serverParameters;
    
	public CMISConfig(Map<String, String> serverParameters) {
		super();
		this.serverParameters = serverParameters;
	}

	public Map<String, String> getServerParameters() {
		return serverParameters;
	}
	
}
