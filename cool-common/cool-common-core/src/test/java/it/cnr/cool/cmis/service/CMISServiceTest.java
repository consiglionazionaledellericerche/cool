package it.cnr.cool.cmis.service;

import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CMISServiceTest {

	private static final String TICKET = "org.apache.chemistry.opencmis.password";

	private static final String USER_ADMIN_USERNAME = "user.admin.username";

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";


	@Autowired
	private CMISService cmisService;

	@Autowired
	private CmisAuthRepository cmisAuthRepository;

	@Autowired
	private CMISAuthenticatorFactory cmisAuthenticatorFactory;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CMISServiceTest.class);

	@Test
	public void testCMISService() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetRepositoriesStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetRepositoriesStringStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetRepositoriesCMISConfigStringStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetRepositoriesMapOfStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSession() {
        HttpServletRequest req = new MockHttpServletRequest();
        Session session = cmisService.getCurrentCMISSession(req);
		RepositoryInfo info = session.getRepositoryInfo();
		LOGGER.info(info.getCmisVersion().toString());
		assertEquals(CmisVersion.CMIS_1_1, info.getCmisVersion());
	}

	@Test
	public void testCreateAdminSession() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSessionStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSessionStringStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSessionStringStringStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSessionCMISConfigStringStringStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testCreateSessionMapOfStringString() {
		LOGGER.warn("Not yet implemented");
	}


	@Test
	public void testGetAdminSession() {

		BindingSession session = cmisService.getAdminSession();
		String userId = session.get(USER_ADMIN_USERNAME).toString();
		LOGGER.info(userId);
		assertEquals(ADMIN_USERNAME, userId);
	}

	@Test
	public void testCreateBindingSession() {
        HttpServletRequest req = new MockHttpServletRequest();
        BindingSession session = cmisService.getCurrentBindingSession(req);
		String userId = session.get(USER_ADMIN_USERNAME).toString();
		LOGGER.info(userId);
		assertEquals(ADMIN_USERNAME, userId);
	}

	@Test
	public void testCreateBindingSessionStringString() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetTicket() {
		LOGGER.warn("Not yet implemented");
	}


	@Test
	public void testSetAtompubURL() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetQueryResultSessionCmisObject() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetQueryResultSessionCriteria() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testFindDocumentChild() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetCurrentCMISSession() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        Session session = cmisService.getCurrentCMISSession(req);
		String id = session.getRepositoryInfo().getId();
		LOGGER.info(id);
		Session session2 = cmisService.getCurrentCMISSession(req);
		String id2 = session2.getRepositoryInfo().getId();
		assertEquals(id, id2);
	}


	@Test
	public void testGetCurrentBindingSession() {
		HttpServletRequest req = new MockHttpServletRequest();
		req.getSession();
		BindingSession session = cmisService.getCurrentBindingSession(req);
		BindingSession session2 = cmisService.getCurrentBindingSession(req);
		assertEquals(session.getSessionId(), session2.getSessionId());
	}

	@Test
	public void testAfterPropertiesSet() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetSessionId() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetCMISUserFromSession() {

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CMISService.AUTHENTICATION_HEADER, "foobar");
        CMISUser user = cmisService.getCMISUserFromSession(req);
		assertTrue(user.isGuest());
	}



    @Test
	public void testGetHttpInvoker() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testGetBaseURL() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testUpdateObjectProperties() {
		LOGGER.warn("Not yet implemented");
	}

	@Test
	public void testExtractTicketFromRequestAuthorization () {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String usernameAndPassword = ADMIN_USERNAME + ":" + ADMIN_PASSWORD;

		String base64Auth = DatatypeConverter.printBase64Binary(usernameAndPassword.getBytes());
		request.addHeader("Authorization", "Basic " + base64Auth);

		assertEquals(ADMIN_USERNAME, usernameOf(request));
	}

	@Test
	public void testExtractTicketFromRequestXAlfrescoTicket() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String ticket = cmisAuthenticatorFactory.authenticate(ADMIN_USERNAME, ADMIN_PASSWORD);
		request.addHeader(CMISService.AUTHENTICATION_HEADER, ticket);

		assertEquals(ADMIN_USERNAME, usernameOf(request));
	}

	@Test
	public void testExtractTicketFromRequestCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String ticket = cmisAuthenticatorFactory.authenticate(ADMIN_USERNAME, ADMIN_PASSWORD);
		Cookie cookie = new Cookie(CMISService.COOKIE_TICKET_NAME, ticket);
		request.setCookies(cookie);

		assertEquals(ADMIN_USERNAME, usernameOf(request));
	}


	private String usernameOf(HttpServletRequest request) {
		String ticket = cmisService.extractTicketFromRequest(request);
		LOGGER.debug(ticket);
		assertNotNull(ticket);
		BindingSession bindingSession = cmisAuthRepository.getBindingSession(ticket);
		CMISUser user = cmisAuthRepository.getCachedCMISUser(ticket, bindingSession);
		LOGGER.debug(user.toString());
		String id = user.getId();
		LOGGER.debug(id);
		return id;
	}

}
