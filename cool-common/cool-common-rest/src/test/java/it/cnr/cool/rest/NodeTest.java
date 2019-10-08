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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.LoginException;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class NodeTest {

    private static final String STRINGA = "test requisiti";

    private static final String CMIS_DOCUMENT = "cmis:document";

    private static final String NOME_DOCUMENTO = "Node di test";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NodeTest.class);

    private static final String PLACEHOLDER = "XXXXX";
    private static final String contentType = "multipart/form-data; boundary=----WebKitFormBoundary";

    private static final String DATA_PATH = "/Data Dictionary/Node Test";
    private final static Date data = new Date();
    private static Folder folder;
    private Session adminSession;
    private CmisObject parentObject;
    @Value("${user.admin.username}")
    private String adminUserName;
    @Value("${user.admin.password}")
    private String adminPassword;
    @Autowired
    private Node node;
    @Autowired
    private CMISService cmisService;
    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

    @AfterAll
    public static void tearDown() {
        ItemIterable<CmisObject> nodes = folder.getChildren();

        for (CmisObject node : nodes) {
            node.delete(true);
        }
    }

    @BeforeEach
    public void setUp() {
        adminSession = cmisService.createAdminSession();
        try {
            folder = (Folder) adminSession.getObjectByPath(DATA_PATH);
        } catch (CmisObjectNotFoundException e) {
            Folder dd = (Folder) adminSession
                    .getObjectByPath("/Data Dictionary");
            Map<String, Serializable> properties = new HashMap<String, Serializable>();
            properties.put("cmis:name", "Node Test");
            properties.put("cmis:objectTypeId", "F:jconon_call:folder");
            properties.put("jconon_call:requisiti", STRINGA);

            ObjectId id = adminSession.createFolder(properties, dd);
            folder = (Folder) adminSession.getObject(id);
        }

        parentObject = folder.getFolderParent();
    }

    @Test
    public void testPostHTML() {

        HttpServletRequest request = makeRequest("POST", "node.insert",
                parentObject.getId());
        Response response = node.postHTML(request);

        assertTrue(response.getStatus() == Status.OK.getStatusCode());

        String html = response.getEntity().toString();
        LOGGER.debug(html);

        // extract nodeRef from HTML (il noderef è la chiave del un campo contenuto dentro "attachments" del json)
        JSONObject json = new JSONObject(html.substring(html.indexOf("attachments") - 2, html.indexOf("</")));
        String nodeRef = (String) json.getJSONObject("attachments").keys().next();

        LOGGER.debug("created node " + nodeRef);
        assertNotNull(nodeRef);

        // test cleanup
        deleteDocument(nodeRef);

    }

    @Test
    public void testPostInsert() {

        String nodeRef = createDocument();
        LOGGER.debug("created node " + nodeRef);
        assertNotNull(nodeRef);

        // test cleanup
        deleteDocument(nodeRef);
    }

    @Test
    public void testPostUpdate() {
        // prepare test
        String nodeRef = createDocument();

        // do actual test
        String nodeRefUpdate = updateDocument(nodeRef);

        LOGGER.debug("updated node " + nodeRef);
        assertNotNull(nodeRefUpdate);
        //controllo il noderef escludendo la version che sarà ovviamente diversa
        assertEquals(nodeRef.substring(0, nodeRef.indexOf(";")), nodeRefUpdate.substring(0, nodeRefUpdate.indexOf(";")));

        // test cleanup
        deleteDocument(nodeRef);
    }


    @Test
    public void testDelete() throws IOException {
        // prepare test
        String nodeRef = createDocument();

        // do actual test
        JsonElement jsonDelete = deleteDocument(nodeRef);
        assertTrue(jsonDelete.getAsJsonObject().get("attachments")
                .getAsJsonObject().entrySet().size() == 0);
    }

    // test GET Object
    @Test
    public void testGetCmisObject() {

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("nodeRef", folder.getId());
        Response resp = node.get(req);

        assertNotNull(resp);


        assertEquals(getValueFromResponse(resp, "jconon_call:requisiti"),
                STRINGA);
        assertEquals(getValueFromResponse(resp, PropertyIds.PARENT_ID), parentObject.getId());
        //gli "aspect" sono diventati "secondaryTypes"
        assertNotNull(getValueFromResponse(resp, PropertyIds.SECONDARY_OBJECT_TYPE_IDS));
        assertNotNull(getValueFromResponse(resp, PropertyIds.OBJECT_ID));
        assertFalse(() -> getValueFromResponse(resp, "jconon_call:pubblicato"));

    }

    @Test
    public void testGetCmisObjectCachable() throws LoginException {

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CMISService.AUTHENTICATION_HEADER, cmisAuthenticatorFactory.getTicket(adminUserName, adminPassword));
        req.setParameter("cachable", "true");
        req.setParameter("nodeRef", folder.getId());

        Response resp = node.get(req);

        assertNotNull(resp);

        assertEquals(getValueFromResponse(resp, "jconon_call:requisiti"),
                STRINGA);
        assertEquals(getValueFromResponse(resp, PropertyIds.PARENT_ID), parentObject.getId());
        assertNotNull(getValueFromResponse(resp, PropertyIds.SECONDARY_OBJECT_TYPE_IDS));
        assertNotNull(getValueFromResponse(resp, PropertyIds.OBJECT_ID));
        assertFalse(() -> getValueFromResponse(resp, "jconon_call:pubblicato"));
    }

    @Test
    public void testMetadata() throws LoginException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CMISService.AUTHENTICATION_HEADER, cmisAuthenticatorFactory.getTicket(adminUserName, adminPassword));
        MultivaluedMap<String, String> formParams = new MultivaluedHashMap<String, String>();
        formParams.add(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT);
        long prefix = data.getTime();
        formParams.add(PropertyIds.NAME, NOME_DOCUMENTO + "-" + prefix);
        formParams.add(PropertyIds.PARENT_ID, parentObject.getId());
        // creazione documento di test

        Response response = node.metadata(request, formParams);
        // verifica campi della response
        assertTrue(getValueFromResponse(response, PropertyIds.NAME)
                .equals(NOME_DOCUMENTO + "-" + prefix));
        assertTrue(getValueFromResponse(response, PropertyIds.OBJECT_TYPE_ID).equals(CMIS_DOCUMENT));
        String nodeRef = getValueFromResponse(response, PropertyIds.OBJECT_ID);
        LOGGER.debug(nodeRef);
        // rimozione del documento creato nel test
        JsonElement jsonDelete = deleteDocument(nodeRef);
        assertTrue(jsonDelete.getAsJsonObject().get("attachments")
                .getAsJsonObject().entrySet().size() == 0);
    }


    // private methods

    private String createDocument() {

        HttpServletRequest request = makeRequest("POST",
                "node.insert", parentObject.getId());
        Response response = node.post(request);
        LOGGER.debug(response.toString());
        String content = response.getEntity().toString();
        LOGGER.info(content);
        JsonElement json = new JsonParser().parse(content);
        LOGGER.debug(json.toString());
        String nodeRef = getNodeRef(json);
        LOGGER.debug(nodeRef);
        return nodeRef;
    }

    private String updateDocument(String nodeRef) {
        HttpServletRequest request = makeRequest("POST",
                "node.update", nodeRef);
        Response response = node.post(request);
        LOGGER.debug("status: " + response.getStatus());
        String content = response.getEntity().toString();
        LOGGER.info(content);
        JsonElement json = new JsonParser().parse(content);
        LOGGER.debug(content);
        String nodeRefUpdate = getNodeRef(json);
        LOGGER.debug(nodeRefUpdate);
        return nodeRefUpdate;
    }


    private JsonElement deleteDocument(String nodeRef) {
        HttpServletRequest request = makeRequest("DELETE",
                "node.delete",
                nodeRef);

        Response response = node.delete(request);
        LOGGER.debug(response.getEntity().toString());
        LOGGER.debug("status: " + response.getStatus());
        String content = response.getEntity().toString();
        LOGGER.debug(content);
        JsonElement json = new JsonParser().parse(content);
        LOGGER.debug(json.toString());
        return json;
    }


    // UTILITY METHODS

    private String getNodeRef(JsonElement j) {
        return j.getAsJsonObject().get("attachments").getAsJsonObject().entrySet().iterator().next().getKey();
    }

    private HttpServletRequest makeRequest(String method, String path,
                                           String nodeRef) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setContentType(contentType);
        try {
            request.addHeader(CMISService.AUTHENTICATION_HEADER, cmisAuthenticatorFactory.getTicket(adminUserName, adminPassword));
        } catch (LoginException e) {
            LOGGER.error("unable to add header auth", e);
        }

        try {
            InputStream is = getClass().getResourceAsStream("/requests/" + path);
            String content = IOUtils.toString(is);
            content = content.replace(PLACEHOLDER, nodeRef);
            content = content.replace("VisualElementsManifest0",
                    "VisualElementsManifest0" + data.getTime());

            LOGGER.debug(content);

            request.setContent(content.getBytes());
        } catch (IOException e) {
            LOGGER.error("unable to load request body " + path, e);
        }

        return request;
    }

    public <T extends Serializable> T getValueFromResponse(Response response, String key) {
        final Map<String, T> map = Optional.ofNullable(response.getEntity())
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElse(Collections.emptyMap());
        return map.entrySet()
                .stream()
                .filter(stringStringEntry -> stringStringEntry.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(null);
    }

}