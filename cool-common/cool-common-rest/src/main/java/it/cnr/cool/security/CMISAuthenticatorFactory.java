package it.cnr.cool.security;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.UserFactoryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CMISAuthenticatorFactory {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CMISAuthenticatorFactory.class);

	public static final String SESSION_ATTRIBUTE_KEY_USER_ID = "_alf_USER_ID";

	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

	public CMISUser loadUser(HttpServletRequest request, String userId)
			throws UserFactoryException {
		return loadUser(request, userId, null);
	}

	public CMISUser loadUser(HttpServletRequest request, String userId,
			String endpointId) throws UserFactoryException {
		try {
			BindingSession bindingSession = cmisService
					.getCurrentBindingSession(request);
			return userService.loadUser(userId, bindingSession);
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException("Error loading user: " + userId, e);
		}
	}

	public boolean authenticate(HttpServletRequest request, String username,
			String password) {
		boolean authorized = false;
		try {
			String ticket = cmisService.getTicket(username, password);

			org.apache.chemistry.opencmis.client.bindings.impl.SessionImpl bindingSession = cmisService
					.createBindingSession("", ticket);

			Session cmisSession = cmisService.createSession("", ticket);
			HttpSession session = request.getSession(true);
			session.setAttribute(CMISService.DEFAULT_SERVER, cmisSession);
			session.setAttribute(CMISService.BINDING_SESSION,
					cmisService.createBindingSession("", ticket));
			session.setAttribute(CMISService.SIPER_BINDING_SESSION,
					cmisService.createBindingSession(username, password));
			session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_ID, username);

			CMISUser user = userService.loadUser(username, bindingSession);
			LOGGER.debug("loaded user: " + user.toString());
			session.setAttribute(CMISUser.SESSION_ATTRIBUTE_KEY_USER_OBJECT,
					user);


			authorized = true;
		} catch (Exception e) {
			LOGGER.debug("Can't retrieve info, assume not authorized", e);
		}
		return authorized;
	}

}
