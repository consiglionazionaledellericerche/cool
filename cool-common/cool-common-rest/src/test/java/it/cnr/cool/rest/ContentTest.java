/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.rest;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by francesco on 31/08/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MainTestContext.class})
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
