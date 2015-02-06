package it.cnr.cool.service.workflow;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.UserCache;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.GroupsUtils;
import it.cnr.cool.web.PermissionService;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WorkflowService implements UserCache, InitializingBean{

	@Autowired
	private CMISService cmisService;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private CacheService cacheService;

	private Map<String, String> cache;

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);

	private static final String DEFINITIONS_URL = "service/api/workflow-definitions";

	@Override
	public String name() {
		return "workflowDefinitions";
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public String get(CMISUser user, BindingSession session) {
		if (cache.containsKey(user.getId()))
			return cache.get(user.getId());
		String link = cmisService.getBaseURL().concat(DEFINITIONS_URL);
		Response response = CmisBindingsHelper.getHttpInvoker(session).invokeGET(new UrlBuilder(link), session);
		JsonArray filteredDefinitions = new JsonArray();
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			InputStreamReader responseReader = new InputStreamReader(
					response.getStream());
			JsonArray definitions = new JsonParser().parse(responseReader)
					.getAsJsonObject().get("data").getAsJsonArray();
			for (int i = 0; i < definitions.size(); i++) {
				JsonObject definition = definitions.get(i).getAsJsonObject();
				String workflowName = definition.get("name").getAsString();

				boolean isAuthorized;

                if (user != null) {


                    isAuthorized = permissionService.isAuthorized(workflowName, "GET",
                            user.getId(), GroupsUtils.getGroups(user));
                } else {
                    isAuthorized = false;
                }

				LOGGER.debug(workflowName + " "
						+ (isAuthorized ? "authorized" : "unauthorized"));
				if (isAuthorized) {
					filteredDefinitions.add(definition);
				}
			}
			cache.put(user.getId(), filteredDefinitions.toString());
		} else {
			LOGGER.warn("unable to retrieve workflow definitions for user " + user.getId() + ", HTTP error code " + response.getResponseCode());
		}
		return filteredDefinitions.toString();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cache = new HashMap<String, String>();
		cacheService.register(this);
	}

	@Override
	public void clear(String username) {
		clearWorkflowDefinitions(username);
	}

	public void clearWorkflowDefinitions(String authority) {
		cache.remove(authority);
	}
}
