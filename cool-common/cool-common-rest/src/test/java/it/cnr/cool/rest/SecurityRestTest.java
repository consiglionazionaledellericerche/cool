package it.cnr.cool.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)

public class SecurityRestTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRestTest.class);

	private final static String USERNAME = "Francesco-uliana";

	private static final String PIN = "abcde";

	@Autowired
	private SecurityRest security;

	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

	@Test
	public void testForgotPassword() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addPreferredLocale(Locale.ITALIAN);
		Response outcome = security.forgotPassword(req, USERNAME);
		assertEquals(Status.OK.getStatusCode(), outcome.getStatus());

		String content = outcome.getEntity().toString();

		JSONObject json = new JSONObject(content);

		LOGGER.info(json.toString());

		assertEquals("francesco@uliana.it", json.getString("email"));
	}

	@Test
	public void testForgotPasswordFail() {
		HttpServletRequest req = new MockHttpServletRequest();
		Response outcome = security.forgotPassword(req, "doesNotExist");
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), outcome.getStatus());

		String content = outcome.getEntity().toString();
		JSONObject json = new JSONObject(content);
		LOGGER.info(json.toString());

		assertTrue(json.has("error"));
	}


	@Test
	public void testChangePassword(){
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
	public void testChangePasswordPin() throws CoolUserFactoryException {
		HttpServletRequest req = new MockHttpServletRequest();

		HttpSession session = req.getSession();
		session.setAttribute("_alf_USER_OBJECT", new CMISUser("someone"));

		User user = userService.loadUserForConfirm(USERNAME);

		String pin = "123456";

		((CMISUser)user).setPin(pin);
		userService.updateUser(user);

		LOGGER.debug("pin: " + pin);

		Response response = security.changePassword(req, USERNAME, pin, "AAA");

		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		String content = response.getEntity().toString();

		JSONObject json = new JSONObject(content);
		LOGGER.info(json.toString());

		assertTrue(json.has("fullName"));

	}

	@Test
	public void testConfirmAccountFail() throws Exception {
		setDisableAccount(true);
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.confirmAccount(req, USERNAME, "INVALID_PIN");
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
		setDisableAccount(false);
	}

	@Test
	public void testConfirmAccount() throws Exception {
		setDisableAccount(true);
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.confirmAccount(req, USERNAME, PIN);
		assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
		setDisableAccount(false);
	}


	@Test
	public void testConfirmAccountFailBis() throws Exception {
		setDisableAccount(false);
		MockHttpServletRequest req = new MockHttpServletRequest();
		Response response = security.confirmAccount(req, USERNAME, PIN);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	private void setDisableAccount(boolean disable) throws CoolUserFactoryException {
		CMISUser user = (CMISUser) userService.loadUserForConfirm(USERNAME);
		
		if (disable) {
			userService.disableAccount(USERNAME);
			user.setPin(PIN);
		}
		userService.updateUser(user);
	}

}
