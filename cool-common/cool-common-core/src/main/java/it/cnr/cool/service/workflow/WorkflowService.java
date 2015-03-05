package it.cnr.cool.service.workflow;

import com.google.gson.JsonArray;
import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.UserCache;
import it.cnr.cool.repository.WorkflowRepository;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.GroupsUtils;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class WorkflowService implements UserCache, InitializingBean{

	@Autowired
	private CacheService cacheService;

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);

    @Autowired
    private WorkflowRepository workflowRepository;


	@Override
	public String name() {
		return "workflowDefinitions";
	}

	@Override
	public void clear() {
        LOGGER.info("clear all workflow definitions");
        workflowRepository.clear();
	}

    @Override
    public void clear(String username) {
        LOGGER.info("clear workflow definitions for user " + username);
        workflowRepository.remove(username);
    }

	@Override
	public String get(CMISUser user, BindingSession session) {

        if (user != null) {
            List<String> groups = GroupsUtils.getGroups(user);
            String username = user.getId();

            LOGGER.info("retrieving workflow definitions");
            String definitions = workflowRepository.getDefinitions();

            LOGGER.info("retrieving workflow definitions for user " + username);
            String filteredDefinitions = workflowRepository.getFilteredDefinitions(username, groups, definitions);
            LOGGER.info("user " + username + " allowed to start " + filteredDefinitions);
            return filteredDefinitions;
        } else {
            LOGGER.info("user is null");
            return new JsonArray().toString();
        }

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cacheService.register(this);
	}


}
