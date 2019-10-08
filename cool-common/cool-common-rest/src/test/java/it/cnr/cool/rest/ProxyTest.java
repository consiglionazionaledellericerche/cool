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

import com.google.gson.JsonParser;
import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.LoginException;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.util.MimeTypes;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class ProxyTest {

    private static final String username = "admin";

    private static final String URL = "url";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyTest.class);

    @Autowired
    private Proxy proxy;
    @Autowired
    private CMISService cmisService;

    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

    @Test
    public void testBackendAlfresco() throws IOException {

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.setMethod("GET");
        req.addParameter("url", "/service/cnr/groups/zones");
        proxy.get(req, "alfresco", res);

        assertEquals(HttpStatus.OK.value(), res.getStatus());

        String content = res.getContentAsString();

        LOGGER.info(content);
        assertTrue(new JsonParser().parse(content).isJsonObject());
    }


    @Test
    public void testBackendAlfrescoFail() throws IOException {

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.setMethod("GET");
        req.addParameter("url", "/service/api/server");
        proxy.get(req, "alfresco-public", res);

        assertEquals(HttpStatus.OK.value(), res.getStatus());

        String content = res.getContentAsString();

        LOGGER.info(content);
        assertTrue(new JsonParser().parse(content).isJsonObject());
    }

    @Test
    public void testGet() throws IOException {

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter(URL, "service/api/people/" + username);

        MockHttpServletResponse res = new MockHttpServletResponse();

        proxy.get(req, null, res);

        String content = res.getContentAsString();

        LOGGER.debug(content);

        JSONObject json = new JSONObject(content);

        LOGGER.info(json.toString());

        assertEquals(username, json.get("userName"));

    }

    @Test
    public void testPost() throws IOException, LoginException {

        MockHttpServletRequest req = new MockHttpServletRequest();

        String ticket = cmisAuthenticatorFactory.getTicket("admin", "admin");

        req.addHeader(CMISService.AUTHENTICATION_HEADER, ticket);

        req.setParameter(URL, "service/cnr/utils/javascript-execution");
        req.setContentType(MimeTypes.JSON.mimetype());
        req.setContent("{\"command\":\"33*3\"}".getBytes());

        MockHttpServletResponse res = new MockHttpServletResponse();

        proxy.post(req, res);

        String content = res.getContentAsString();
        JSONObject json = new JSONObject(content);

        LOGGER.info(json.toString());
        assertEquals(99, json.getJSONObject("output").getInt("content"));
    }

    @Test
    public void testPut() throws IOException, LoginException {

        MockHttpServletRequest req = new MockHttpServletRequest();

        String ticket = cmisAuthenticatorFactory.getTicket("admin", "admin");

        req.addHeader(CMISService.AUTHENTICATION_HEADER, ticket);

        req.setParameter(URL, "service/api/people/" + username);
        req.setContentType(MimeTypes.JSON.mimetype());
        String cognome = "rossi";
        String jsonData = "{\"lastName\":\"" + cognome + "\"}";
        req.setContent(jsonData.getBytes());

        MockHttpServletResponse res = new MockHttpServletResponse();

        proxy.put(req, res);

        String content = res.getContentAsString();
        JSONObject json = new JSONObject(content);

        LOGGER.info(json.toString());

        assertEquals(cognome, json.getString("lastName"));
    }

    @Test
    public void testDelete() throws IOException {

        MockHttpServletRequest req = new MockHttpServletRequest();

        req.setParameter(URL, "service/api/people/" + username + "_ALTER_EGO");

        MockHttpServletResponse res = new MockHttpServletResponse();

        proxy.delete(req, res);

        assertEquals(HttpStatus.FORBIDDEN.value(), res.getStatus());


    }

}
