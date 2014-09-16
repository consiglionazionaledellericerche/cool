package it.cnr.cool.rest;

import static org.junit.Assert.assertTrue;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/cool-common-rest-test-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class WorkflowDefinitionsTest {

    private static final String USER_NAME = "spaclient";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkflowDefinitionsTest.class);

	@Autowired
	private WorkflowDefinitions workflowDefinitions;

	@Test
	public void testGetDefinitionsUser() {

		MockHttpServletRequest req = new MockHttpServletRequest();

		Response definitions = workflowDefinitions.getDefinitions(req,
				USER_NAME);

		JSONObject jsonObj = new JSONObject(definitions.getEntity().toString());
		LOGGER.info(jsonObj.toString());
		assertTrue(jsonObj.getJSONArray("definitions").length() > 0);

	}

    @Test
    public void testGetDefinitions() {

        CMISUser user = new CMISUser(USER_NAME);
        user.setGroups(new ArrayList<CMISGroup>());

        MockHttpSession session = new MockHttpSession();
		session.setAttribute(CMISUser.SESSION_ATTRIBUTE_KEY_USER_OBJECT, user);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setSession(session);

		Response definitions = workflowDefinitions.getDefinitions(req, null);
		JSONObject jsonObj = new JSONObject(definitions.getEntity().toString());
		LOGGER.info(jsonObj.toString());

		assertTrue(jsonObj.getJSONArray("definitions").length() > 0);

    }

}
