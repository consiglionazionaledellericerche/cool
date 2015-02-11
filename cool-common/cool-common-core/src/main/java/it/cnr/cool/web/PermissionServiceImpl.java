package it.cnr.cool.web;

import com.google.gson.*;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.repository.PermissionRepository;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.GroupsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import java.util.List;
import java.util.Collections;

public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

	private static final String USER = "user";
	private static final String GROUP = "group";
	private static final String ALL = "all";

	@Autowired
	private MailService mailService;

	protected UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}


    public String getRbacAsString() {
        LOGGER.debug("requested RBAC, could be already cache");
        return permissionRepository.getRbac();
    }


	public JsonObject loadPermission() {

        String s = getRbacAsString();
        return new JsonParser().parse(s).getAsJsonObject();
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
		return doDelete(id, method, list, type, authority);
	}

	private boolean doDelete(String id, methods method, lists list, types type,
			String authority) {

        JsonObject json = loadPermission();

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
						return update(json);

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
							return update(json);
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

        JsonObject json = loadPermission();

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
		return update(json);
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

        JsonObject json = loadPermission();

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
		return update(json);
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

        JsonObject json = loadPermission();

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
		return update(json);
	}




    public boolean isAuthorizedCMIS(String id, String method, CMISUser user) {

        if (user == null) {
            user = new CMISUser("guest");
            user.setCapabilities(Collections.singletonMap(CMISUser.CAPABILITY_GUEST, true));
        }

        String username = user.getId();

        List<String> groups = GroupsUtils.getGroups(user);

        return isAuthorized(id, method, username, groups);

    }


	/**
	 *
	 * Check if user is authorized to access the functionality "id" with method
	 * "method". Note that all functionalities are forbidden by default.
	 *
	 * @param id
	 * @param method
	 * @param username
	 * @return true if user is authorized to access the functionality "id" with
	 *         method "method"
	 */
	public boolean isAuthorized(String id, String method, String username, List<String> groups) {

        JsonObject json = loadPermission();

		id = id.replaceAll("^/+", "");

		if (json == null) {
			LOGGER.error("permissions error");
			try {
				mailService.send("Permissions error on cool", "Offending request id:"+id+", method:"+method+", user"+username);
			} catch (MailException e) {
				LOGGER.warn("unable to send mail: Offending request id:"+id+", method:"+method+", user"+ username, e);
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


    public boolean update (JsonObject j) {
        return permissionRepository.update(j.toString());
    }


}
