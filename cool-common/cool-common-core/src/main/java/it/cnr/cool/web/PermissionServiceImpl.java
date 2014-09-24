package it.cnr.cool.web;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class PermissionServiceImpl implements PermissionService {

	public static enum methods {GET, POST, PUT, DELETE};
	public static enum lists {whitelist, blacklist};
	public static enum types {all, group, user};

	private String rbacPath;

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

	private static final String USER = "user";
	private static final String GROUP = "group";
	private static final String ALL = "all";

	private static final String MESSAGE_TEMPLATE = "%s is %s to %s %s";

	@Autowired
	private CMISService cmisService;

	@Autowired
	private MailService mailService;

	private JsonObject json = null;

	protected UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@PostConstruct
	public void loadPermission() {
		try {
			Session session = cmisService.createAdminSession();
			InputStream is = cmisService.getDocumentInputStream(session,
					rbacPath);
			String s = IOUtils.toString(is);
			json = new JsonParser().parse(s).getAsJsonObject();
		} catch (IOException e) {
			LOGGER.error("error retrieving permissions", e);
		} catch (JsonParseException e) {
			LOGGER.error("error retrieving permissions", e);
		}
	}


	/**
	 *
	 * Delete permission (if present)
	 *
	 * @param id
	 *            identifier
	 * @param method
	 *            GET/POST/UPDATE/DELETE
	 * @param list
	 *            whitelist/blacklist
	 * @param type
	 *            user/group/all
	 * @param authority
	 *            username/group name
	 * @return true if permission has been deleted correctly
	 */
	public boolean delete(String id, methods method, lists list, types type,
			String authority) {

		loadPermission();

		return doDelete(id, method, list, type, authority);
	}

	private boolean doDelete(String id, methods method, lists list, types type,
			String authority) {
		if (json.has(id)){
			JsonObject jsonId = json.get(id).getAsJsonObject();
			if(jsonId.has(method.toString())){
				JsonObject jsonMethod = jsonId.get(method.toString()).getAsJsonObject();
				if(jsonMethod.has(list.toString())){

					JsonObject jsonList = jsonMethod.get(list.toString())
							.getAsJsonObject();

					// types.all viene gestito diversamente da user e group
					if(type == types.all) {

						jsonList.remove(type.toString());

						LOGGER.debug(jsonList.toString());
						return update();

					} else if (jsonList.has(type.toString())) {
						JsonArray jsonPermission = jsonList.get(type.toString())
								.getAsJsonArray();

						if (contains(jsonPermission, authority)) {

							JsonArray updatedAuthorities = new JsonArray();

							for (int i = 0; i < jsonPermission.size(); i++) {
								JsonElement authorities = jsonPermission.get(i);
								if (!authorities.getAsString()
										.equals(authority)) {
									updatedAuthorities.add(authorities);
								}
							}

							jsonList.add(type.toString(), updatedAuthorities);

							LOGGER.debug(jsonList.toString());
							return update();
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * enableAll
	 * Esegue l'inserimento di "all":true per il path selezionato nella whitelist
	 * (ed eventualmente lo rimuove dalla blacklist)
	 *
	 * @param id
	 * @param method
	 * @return true se l'operazione ha successo
	 */
	public boolean enableAll(String id, methods method) {

		if(id == null || id.equals("") || method == null) {
			throw new IllegalArgumentException();
		}

		loadPermission();

		if (!json.has(id)) {
			json.add(id, new JsonObject());
		}

		JsonObject jsonId = json.get(id).getAsJsonObject();

		if (!jsonId.has(method.toString())) {
			jsonId.add(method.toString(), new JsonObject());
		}

		JsonObject jsonMethod = jsonId.get(method.toString()).getAsJsonObject();

		// rimuovi dalla blackist, se presente
		if(jsonMethod.has(lists.blacklist.toString())) {
			doDelete(id, method, lists.blacklist, types.all, "true");
//			loadPermission();
//			jsonMethod = json.get(id).getAsJsonObject().get(method.toString()).getAsJsonObject();
		}

		if (!jsonMethod.has(lists.whitelist.toString())) {
			jsonMethod.add(lists.whitelist.toString(), new JsonObject());
		}

		JsonObject jsonList = jsonMethod.get(lists.whitelist.toString()).getAsJsonObject();
		if (!jsonList.has(types.all.toString())) {
			jsonList.addProperty(types.all.toString(), true);
		}

		LOGGER.debug(jsonId.toString());
		return update();
	}

	/**
	 * disableAll
	 * Esegue l'inserimento di "all":true per il path selezionato nella whitelist
	 * (ed eventualmente lo rimuove dalla blacklist)
	 *
	 * @param id
	 * @param method
	 * @return true se l'operazione ha successo
	 */
	public boolean disableAll(String id, methods method) {

		if(id == null || id.equals("") || method == null) {
			throw new IllegalArgumentException();
		}

		loadPermission();

		if (!json.has(id)) {
			json.add(id, new JsonObject());
		}

		JsonObject jsonId = json.get(id).getAsJsonObject();

		if (!jsonId.has(method.toString())) {
			jsonId.add(method.toString(), new JsonObject());
		}

		JsonObject jsonMethod = jsonId.get(method.toString()).getAsJsonObject();

		// rimuovi dalla whitelist, se presente
		if(jsonMethod.has(lists.whitelist.toString())) {
			doDelete(id, method, lists.whitelist, types.all, "true");
			// ho bisogno di ricaricare i permessi
//			loadPermission();
//			jsonMethod = json.get(id).getAsJsonObject().get(method.toString()).getAsJsonObject();
		}

		if (!jsonMethod.has(lists.blacklist.toString())) {
			jsonMethod.add(lists.blacklist.toString(), new JsonObject());
		}

		JsonObject jsonList = jsonMethod.get(lists.blacklist.toString()).getAsJsonObject();
		if (!jsonList.has(types.all.toString())) {
			jsonList.addProperty(types.all.toString(), true);
		}

		LOGGER.debug(jsonId.toString());
		return update();
	}

	/**
	 *
	 * Add new permission
	 *
	 * @param id
	 *            identifier
	 * @param method
	 *            GET/POST/UPDATE/DELETE
	 * @param list
	 *            whitelist/blacklist
	 * @param type
	 *            user/group/all
	 * @param authority
	 *            username/group name
	 * @return true if permission has been added correctly
	 * @throws IllegalArgumentException if arguments are wrong
	 */
	public boolean add(String id,
			methods method,
			lists list,
			types type,
			String authority) {

		if(type == types.all) {
			if(list == lists.whitelist)
				return enableAll(id, method);
			else
				return disableAll(id, method);
		} else {
			return executeAdd(id, method, list, type, authority);
		}
	}

	private boolean executeAdd(String id, methods method, lists list,
			types type, String authority) {
		if(id == null || id.equals("") || method == null || list == null || type == null ||
				authority == null || authority.equals("")) {
			throw new IllegalArgumentException();
		}

		loadPermission();

		if (!json.has(id)) {
			json.add(id, new JsonObject());
		}

		JsonObject jsonId = json.get(id).getAsJsonObject();

		if (!jsonId.has(method.toString())) {
			jsonId.add(method.toString(), new JsonObject());
		}

		JsonObject jsonMethod = jsonId.get(method.toString()).getAsJsonObject();

		if (!jsonMethod.has(list.toString())) {
			jsonMethod.add(list.toString(), new JsonObject());
		}

		JsonObject jsonList = jsonMethod.get(list.toString())
				.getAsJsonObject();

		if (!jsonList.has(type.toString())) {
			jsonList.add(type.toString(), new JsonArray());
		}

		JsonArray jsonPermission = jsonList.get(type.toString())
				.getAsJsonArray();

		if (!contains(jsonPermission, authority)) {
			jsonPermission.add(new JsonPrimitive(authority));
		}

		LOGGER.debug(jsonId.toString());
		return update();
	}


	private boolean update() {

		LOGGER.debug(json.toString());
		try {
			Session session = cmisService.createAdminSession();
			cmisService.updateDocument(session, rbacPath, json.toString());
		} catch (Exception e) {
			return false;
		}

		return true;
	}



	@Override
	public boolean isAuthorizedSession(String id, String method,
			HttpSession session) {

		CMISUser user = cmisService.getCMISUserFromSession(session);
		// se l'utente non è loggato => user = null
		boolean authorized = isAuthorized(id, method, user);
		String message = String.format(MESSAGE_TEMPLATE, user == null ? "guest" : user.getId(), authorized ? "authorized" : "unauthorized", method, id);
		if (authorized) {
			LOGGER.debug(message);
		} else {
			LOGGER.info(message);
		}
		return authorized;
	}

	/**
	 *
	 * Check if user is authorized to access the functionality "id" with method
	 * "method". Note that all functionalities are forbidden by default.
	 *
	 * @param id
	 * @param method
	 * @param user
	 * @return true if user is authorized to access the functionality "id" with
	 *         method "method"
	 */
	public boolean isAuthorized(String id, String method, CMISUser user) {

		if (user == null)
			user = new CMISUser("guest");

		String username = user.getId();

		List<String> groups = new ArrayList<String>();

		if (user.getGroups() != null) {
			for (CMISGroup g : user.getGroups()) {
				groups.add(g.getItemName());
			}
		}
		if (!username.equals("guest") )
			groups.add("GROUP_EVERYONE");

		id = id.replaceAll("^/+", "");

		if (json == null) {
			LOGGER.error("permissions error");
			try {
				mailService.send("Permissions error on cool", "Offending request id:"+id+", method:"+method+", user"+user);
			} catch (MailException e) {
				LOGGER.warn("unable to send mail: Offending request id:"+id+", method:"+method+", user"+user, e);
			}
			return false;
		}

		JsonObject p = json.getAsJsonObject(id);

		if (p == null || p.get(method) == null) {
			LOGGER.debug(username + " blocked by default to " + method + " "
					+ id);
			return false; // blocked by default
		}

		JsonObject w = p.getAsJsonObject(method).getAsJsonObject("whitelist");
		JsonObject b = p.getAsJsonObject(method).getAsJsonObject("blacklist");

		// check permission specific for user
		if (w != null && w.has(USER)
				&& contains(w.getAsJsonArray(USER), username)) {
			return true;
		}

		if (b != null && b.has(USER)
				&& contains(b.getAsJsonArray(USER), username)) {
			return false;
		}

		// check permission specific for group
		if (w != null && w.has(GROUP)) {
			for (String group : groups) {
				if (contains(w.getAsJsonArray(GROUP), group)) {
					return true;
				}
			}
		}

		if (b != null && b.has(GROUP)) {
			for (String group : groups) {
				if (contains(b.getAsJsonArray(GROUP), group)) {
					return false;
				}
			}
		}

		// all
		if (w != null && w.has(ALL) && w.get(ALL).getAsBoolean())
			return true;

		if (b != null && b.has(ALL) && b.get(ALL).getAsBoolean())
			return false;

		return false;
	}

	private boolean contains(JsonArray array, String element) {

		for (int i = 0; i < array.size(); i++) {
			if (array.get(i).getAsString().equals(element))
				return true;
		}

		return false;
	}

	public JsonObject getJson() {
		return json;
	}

	public void setRbacPath(String rbacPath) {
		LOGGER.info("using RBAC " + rbacPath);
		this.rbacPath = rbacPath;
	}
}
