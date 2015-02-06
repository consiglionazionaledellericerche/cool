package it.cnr.cool.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.LoginException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class NodeTest {

	private static final String STRINGA = "test requisiti";

	private static final String CMIS_DOCUMENT = "cmis:document";

	private static final String NOME_DOCUMENTO = "Node di test";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeTest.class);

	private static final String PLACEHOLDER = "XXXXX";
	private static final String contentType = "multipart/form-data; boundary=----WebKitFormBoundary";

	private static final String DATA_PATH = "/Data Dictionary/Node Test";

	private Session adminSession;
	private CmisObject parentObject;
	private final static Date data = new Date();
	@Autowired
	private Node node;

	@Autowired
	private CMISService cmisService;

	private static Folder folder;

	@Before
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

	@AfterClass
	public static void tearDown() {
		ItemIterable<CmisObject> nodes = folder.getChildren();

		for (CmisObject node : nodes) {
			node.delete(true);
		}
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
        JSONObject json = new JSONObject(html.substring(html.indexOf("attachments") -2, html.indexOf("</") ));
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

		JSONObject jsonObj = new JSONObject((String) resp.getEntity());

		assertEquals(jsonObj.getString("jconon_call:requisiti"),
				STRINGA);
		assertEquals(jsonObj.getString("cmis:parentId"), parentObject.getId());
        //gli "aspect" sono diventati "secondaryTypes"
		assertNotNull(jsonObj.getJSONArray("secondaryTypes"));
		assertNotNull(jsonObj.getString("cmis:objectId"));
		assertFalse(jsonObj.getBoolean("jconon_call:pubblicato"));

	}

	@Test
	public void testGetCmisObjectCachable() throws LoginException {

		MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CMISService.AUTHENTICATION_HEADER, cmisService.getTicket("admin", "admin"));
		req.setParameter("cachable", "true");
		req.setParameter("nodeRef", folder.getId());

		Response resp = node.get(req);

		assertNotNull(resp);

		JSONObject jsonObj = new JSONObject((String) resp.getEntity());

		assertEquals(jsonObj.getString("jconon_call:requisiti"),
				STRINGA);
		assertEquals(jsonObj.getString("cmis:parentId"), parentObject.getId());
		assertNotNull(jsonObj.getJSONArray("secondaryTypes"));
		assertNotNull(jsonObj.getString("cmis:objectId"));
		assertFalse(jsonObj.getBoolean("jconon_call:pubblicato"));
	}

	@Test
	public void testMetadata() throws LoginException {

		MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CMISService.AUTHENTICATION_HEADER, cmisService.getTicket("admin", "admin"));
		MultivaluedMap<String, String> formParams = new MultivaluedHashMap<String, String>();
		formParams.add(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT);
		long prefix = data.getTime();
		formParams.add(PropertyIds.NAME, NOME_DOCUMENTO + "-" + prefix);
		formParams.add(PropertyIds.PARENT_ID, parentObject.getId());
		// creazione documento di test
		Response response = node.metadata(request, formParams);
		LOGGER.debug(response.toString());
		String content = response.getEntity().toString();
		LOGGER.info(content);
		// parsing del contenuto delle response
		JsonElement json = new JsonParser().parse(content);
		LOGGER.debug(json.toString());
		// verifica campi della response
		assertTrue(json.getAsJsonObject().get(PropertyIds.NAME).getAsString()
				.equals(NOME_DOCUMENTO + "-" + prefix));
		assertTrue(json.getAsJsonObject().get(PropertyIds.OBJECT_TYPE_ID)
				.getAsString().equals(CMIS_DOCUMENT));
		String nodeRef = json.getAsJsonObject().get(PropertyIds.OBJECT_ID)
				.getAsString();
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
		LOGGER.debug(content.toString());
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
            request.addHeader(CMISService.AUTHENTICATION_HEADER, cmisService.getTicket("admin", "admin"));
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
}