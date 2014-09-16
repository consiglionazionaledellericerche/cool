package it.cnr.cool.cmis.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAuthenticationProvider extends
		StandardAuthenticationProvider {
	private static final String HEADER_COOKIE = "Cookie";

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomAuthenticationProvider.class);

	public static String SESSION_ATTRIBUTE_KEY_USER_ID = "_alf_USER_ID";


	@Override
	public Map<String, List<String>> getHTTPHeaders(String url) {

		//TODO: trovare il modo di ottenere la request
		return getHTTPHeaders(url, null);
	}


	Map<String, List<String>> getHTTPHeaders(String url, HttpServletRequest request) {
		Map<String, List<String>> result = super.getHTTPHeaders(url);

		if (result == null) {
			result = new HashMap<String, List<String>>();
		}

		// add Cookie header if present
		if (request != null) {
			HttpSession session = request.getSession(false);

			if (session != null) {
				Object sessionId = session.getAttribute(CMISService.SESSION_ID);
				if (sessionId != null && sessionId instanceof String) {
					result.put(HEADER_COOKIE,
							Collections.singletonList((String) sessionId));
					LOGGER.debug("sessionId = " + sessionId);
				} else {
					LOGGER.debug("sessionId is null");
				}

				if (session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_ID) != null) {
					result.put("X-Username", Collections.singletonList(session
						.getAttribute(SESSION_ATTRIBUTE_KEY_USER_ID).toString()));
				}
			}

			result.put("X-Remote-Address", Collections.singletonList(request.getRemoteAddr()));

			result.put("X-Url",
					Collections.singletonList(request.getMethod() + " "
							+ request.getRequestURL() + "?"
							+ request.getQueryString()));

		} else {
			LOGGER.debug(HttpSession.class.getSimpleName() + " nullo, url: "
					+ url);
			BindingSession bindingSession = getSession();
			if (bindingSession != null) {
				Object sessionId = bindingSession.get(CMISService.SESSION_ID);
				if (sessionId != null && sessionId instanceof String) {
					result.put(HEADER_COOKIE,
							Collections.singletonList((String) sessionId));
				}
			} else {
				LOGGER.debug(BindingSession.class.getSimpleName() + " is null");
			}
		}

		//result.put("connection", Collections.singletonList("close"));
		return result;
	}
}
