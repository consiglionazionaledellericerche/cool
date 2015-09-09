package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by francesco on 31/08/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContentTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContentTest.class);

    public static final String PATH = "/Data Dictionary/Email Templates/activities/activities-email_de.ftl";

    @Autowired
    private Content content;

    @Autowired
    private CMISService cmisService;

    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

    @Test
    public void testContentByPathGuest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        Response response = content.content(req, res, PATH, null, null, null);
        assertEquals(200, response.getStatus());
        String content = res.getContentAsString();
        LOGGER.info(content);
        assertTrue(content.length() > 0);
    }
    @Test
    public void testContentByNodeRefGuest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        String nodeRef = cmisService.createAdminSession().getObjectByPath(PATH).getId();
        LOGGER.info("the nodeRef of path {} is {}", PATH, nodeRef);
        Response response = content.content(req, res, null, nodeRef, null, null);
        assertEquals(200, response.getStatus());
        String content = res.getContentAsString();
        LOGGER.info(content);
        assertTrue(content.length() > 0);
    }


    @Test
    public void testContentNoQueryStringParams() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        Response response = content.content(req, res, null, null, null, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

}
