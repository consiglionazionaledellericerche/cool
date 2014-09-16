package it.cnr.cool.cmis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.bindings.impl.SessionImpl;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

public class CustomAuthenticationProviderTest {

	private static final String SESSION_ID = "1234";
	private static final String USERNAME = "spaclient";
	private static final String URL = "http://www.google.it";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomAuthenticationProvider.class);

	@Test
	public void testGetHTTPHeadersStringRequest() {

		CustomAuthenticationProvider provider = new CustomAuthenticationProvider();

		HttpServletRequest request = new MockHttpServletRequest();

		Map<String, List<String>> headers = provider
				.getHTTPHeaders(URL, request);
		LOGGER.info(headers.toString());
		assertTrue(headers.containsKey("X-Remote-Address"));
		assertTrue(headers.containsKey("X-Url"));

	}

	@Test
	public void testGetHTTPHeadersStringSession() {

		CustomAuthenticationProvider provider = new CustomAuthenticationProvider();


		HttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession(true);
		session.setAttribute(CMISService.SESSION_ID, SESSION_ID);
		session.setAttribute(CustomAuthenticationProvider.SESSION_ATTRIBUTE_KEY_USER_ID, USERNAME);

		Map<String, List<String>> headers = provider
				.getHTTPHeaders(URL, request);
		LOGGER.info(headers.toString());
		assertEquals(USERNAME, headers.get("X-Username").get(0));
		assertEquals(SESSION_ID, headers.get("Cookie").get(0));

	}

	@Test
	public void testGetHTTPHeadersString() {
		CustomAuthenticationProvider provider = new CustomAuthenticationProvider();
		Map<String, List<String>> headers = provider.getHTTPHeaders(URL);
		LOGGER.info(headers.toString());
		assertTrue(headers.isEmpty());
	}

	@Test
	public void testGetHTTPHeadersBindingSession() {
		CustomAuthenticationProvider provider = new CustomAuthenticationProvider();
		BindingSession session = new SessionImpl();

		session.put(CMISService.SESSION_ID, SESSION_ID);
		provider.setSession(session);
		Map<String, List<String>> headers = provider.getHTTPHeaders(URL);
		LOGGER.info(headers.toString());
		assertEquals(SESSION_ID, headers.get("Cookie").get(0));
	}

}
