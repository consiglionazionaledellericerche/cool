package it.cnr.cool.web;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface PermissionService {
	public static enum methods {GET, POST, PUT, DELETE};
	public static enum lists {whitelist, blacklist};
	public static enum types {all, group, user};

	boolean isAuthorizedSession(String id, String method, HttpSession session);
	boolean add(String id, methods method, lists list, types type, String authority);
	boolean delete(String id, methods method, lists list, types type, String authority);
    String getRbacAsString();
    boolean isAuthorized(String id, String method, String username, List<String> groups);
}
