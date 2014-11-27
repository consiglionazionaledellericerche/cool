package it.cnr.cool.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.test.InstanceTestClassListener;
import it.cnr.cool.test.SpringInstanceTestClassRunner;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

@RunWith(SpringInstanceTestClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityRestTest implements InstanceTestClassListener{

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRestTest.class);

	private final static String USERNAME = "test.selezioni";
	private final static String NEWUSERNAME = "pippo.paperino";

	private static final String URL = "url";

	@Autowired
	private SecurityRest security;	
	@Autowired
	private Proxy proxy;
	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

	@Override
	public void beforeClassSetup() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addPreferredLocale(Locale.ITALIAN);
		MultivaluedMap<String, String> form = new MultivaluedHashMap<String, String>();
		form.add("userName", NEWUSERNAME);
		form.add("password", NEWUSERNAME);
		form.add("firstName", "PIPPO");
		form.add("lastName", "PAPERINO");
		form.add("email", "pippo.paperino@pluto.it");
		form.add("codicefiscale", "SPSMRC73H02C495");
		
		Response outcome = security.doCreateUser(req, form, "it");
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), outcome.getStatus());
		
		form.remove("codicefiscale");
		form.add("codicefiscale", "SSSSSS73H02C495G");
		outcome = security.doCreateUser(req, form, "it");
		assertEquals(Status.OK.getStatusCode(), outcome.getStatus());		
	}

	@Test
	public void test1ConfirmAccountFail() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.confirmAccount(req, NEWUSERNAME, "INVALID_PIN");
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	public void test2LoginFailed() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.login(req, NEWUSERNAME, NEWUSERNAME, "/home", null);
		assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
		assertTrue(response.getHeaderString("Location").contains("failure=yes"));
	}
	
	@Test
	public void test3ConfirmAccount() throws Exception {
		CMISUser user = userService.loadUserForConfirm(NEWUSERNAME);		
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.confirmAccount(req, NEWUSERNAME, user.getPin());
		assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void test4Login() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.login(req, NEWUSERNAME, NEWUSERNAME, "/home", null);
		assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
		assertTrue(response.getHeaderString("Location").equals("/home"));
	}
	
	@Test
	public void test5ForgotPassword() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addPreferredLocale(Locale.ITALIAN);
		Response outcome = security.forgotPassword(req, NEWUSERNAME, Locale.getDefault().getLanguage());
		assertEquals(Status.OK.getStatusCode(), outcome.getStatus());

		String content = outcome.getEntity().toString();

		JSONObject json = new JSONObject(content);

		LOGGER.info(json.toString());

		assertEquals("pippo.paperino@pluto.it", json.getString("email"));
	}

	@Test
	public void test6ForgotPasswordFail() {
		HttpServletRequest req = new MockHttpServletRequest();
		Response outcome = security.forgotPassword(req, "doesNotExist", Locale.getDefault().getLanguage());
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), outcome.getStatus());

		String content = outcome.getEntity().toString();
		JSONObject json = new JSONObject(content);
		LOGGER.info(json.toString());

		assertTrue(json.has("error"));
	}


	@Test
	public void test7ChangePassword(){
		HttpServletRequest req = new MockHttpServletRequest();

		HttpSession session = req.getSession();
		session.setAttribute("_alf_USER_OBJECT", new CMISUser("someone"));

		Response response = security.changePassword(req, USERNAME, "", "");

		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

		String content = response.getEntity().toString();

		JSONObject json = new JSONObject(content);
		LOGGER.info(json.toString());

		assertTrue(json.has("error"));

	}

	@Test
	public void test8ChangePasswordPin() throws CoolUserFactoryException {
		HttpServletRequest req = new MockHttpServletRequest();

		HttpSession session = req.getSession();
		session.setAttribute("_alf_USER_OBJECT", new CMISUser("someone"));

		CMISUser user = userService.loadUserForConfirm(NEWUSERNAME);

		String pin = "123456";

		user.setPin(pin);
		userService.updateUser(user);

		LOGGER.debug("pin: " + pin);

		Response response = security.changePassword(req, NEWUSERNAME, pin, "AAA");

		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		String content = response.getEntity().toString();

		JSONObject json = new JSONObject(content);
		LOGGER.info(json.toString());

		assertTrue(json.has("fullName"));

	}

	@Override
	public void afterClassSetup() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setParameter(URL, "service/api/people/" + NEWUSERNAME);
		HttpSession session = req.getSession();
		session.setAttribute(CMISService.BINDING_SESSION, cmisService.getAdminSession());		
		MockHttpServletResponse res = new MockHttpServletResponse();
		try {
			proxy.delete(req, res);
		} catch (IOException e) {
		}
		assertEquals(HttpStatus.OK.value(), res.getStatus());
		
	}
}
