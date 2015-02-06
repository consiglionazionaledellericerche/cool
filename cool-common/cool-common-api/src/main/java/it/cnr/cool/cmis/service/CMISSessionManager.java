package it.cnr.cool.cmis.service;



import org.apache.chemistry.opencmis.client.api.Session;

import javax.servlet.http.HttpServletRequest;

public interface CMISSessionManager {

	Session getCurrentCMISSession(HttpServletRequest request);

	Session createAdminSession();

}
