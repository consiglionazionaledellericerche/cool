package it.cnr.cool.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.web.PermissionService;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpInvoker;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

/**
 * Created by francesco on 05/03/15.
 */

@Repository
public class WorkflowRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRepository.class);

    private static final String DEFINITIONS_URL = "service/api/workflow-definitions";

    public static final String FILTERED_WORKFLOW_DEFINITIONS = "filtered-workflow-definitions";

    @Autowired
    private CMISService cmisService;

    @Autowired
    private PermissionService permissionService;


    @Cacheable("workflow-definitions")
    public String getDefinitions() {

        LOGGER.info("Loading workflow definitions");

        String link = cmisService.getBaseURL().concat(DEFINITIONS_URL);

        BindingSession session = cmisService.getAdminSession();

        HttpInvoker httpInvoker = CmisBindingsHelper.getHttpInvoker(session);
        UrlBuilder url = new UrlBuilder(link);
        Response response = httpInvoker.invokeGET(url, session);

        if (response.getResponseCode() == HttpStatus.SC_OK) {

            try {
                String json = IOUtils.toString(response.getStream());
                LOGGER.debug(json);
                return json;
            } catch (IOException e) {
                LOGGER.error("error extracting content", e);
                throw new RuntimeException("unable to retrieve workflow definitions");
            }


        } else {
            LOGGER.error("unable to retrieve workflow definitions, HTTP error code " + response.getResponseCode());
            throw new RuntimeException("unable to retrieve workflow definitions");
        }



    }


    @Cacheable(value= FILTERED_WORKFLOW_DEFINITIONS, key="#username")
    public String getFilteredDefinitions(String username, List<String> groups, String json) {

        LOGGER.info("Loading workflow definitions for user " + username + " belonging to groups " + groups.toString());

        JsonArray definitions = new JsonParser().parse(json)
                .getAsJsonObject().get("data").getAsJsonArray();

        JsonArray filteredDefinitions = new JsonArray();

        for (int i = 0; i < definitions.size(); i++) {
            JsonObject definition = definitions.get(i).getAsJsonObject();
            String workflowName = definition.get("name").getAsString();

            boolean isAuthorized = permissionService.isAuthorized(workflowName, "GET",
                        username, groups);

            LOGGER.debug(workflowName + " "
                    + (isAuthorized ? "authorized" : "unauthorized"));
            if (isAuthorized) {
                filteredDefinitions.add(definition);
            }
        }

        return filteredDefinitions.toString();


    }

    @CacheEvict(value= FILTERED_WORKFLOW_DEFINITIONS, key = "#username")
    public void remove(String username) {
        LOGGER.info("cleared workflow definitions for user " + username);
    }



    @CacheEvict(value= FILTERED_WORKFLOW_DEFINITIONS, allEntries=true)
    public void clear() {
        LOGGER.info("cleared all workflow definitions");
    }
}
