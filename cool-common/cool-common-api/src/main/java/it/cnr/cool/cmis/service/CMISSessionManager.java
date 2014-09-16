package it.cnr.cool.cmis.service;

import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;

public interface CMISSessionManager {

	Session getCurrentCMISSession(HttpSession session);

	Session createAdminSession();

}
