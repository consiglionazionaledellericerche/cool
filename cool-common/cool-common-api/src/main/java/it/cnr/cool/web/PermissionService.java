package it.cnr.cool.web;

import javax.servlet.http.HttpSession;

public interface PermissionService {

	boolean isAuthorizedSession(String id, String method, HttpSession session);

}
