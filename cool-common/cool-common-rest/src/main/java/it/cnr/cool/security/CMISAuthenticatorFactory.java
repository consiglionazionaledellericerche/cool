package it.cnr.cool.security;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CmisAuthRepository;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.UserFactoryException;

import javax.servlet.http.HttpServletRequest;


import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CMISAuthenticatorFactory {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CMISAuthenticatorFactory.class);

	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

    @Autowired
    private CmisAuthRepository cmisAuthRepository;

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

	public String authenticate(HttpServletRequest request, String username,
			String password) {
		try {
			String ticket = cmisService.getTicket(username, password);


            org.apache.chemistry.opencmis.client.bindings.impl.SessionImpl bindingSession;
            bindingSession = cmisAuthRepository.getBindingSession(ticket);

            CMISUser user = userService.loadUser(username, bindingSession);
            LOGGER.debug("loaded user: " + user.toString());

            cmisAuthRepository.getCMISUser(user, ticket);

            LOGGER.error("GESTIRE LA CREAZIONE DELL'AUTENTICAZIONE");

            return ticket;

		} catch (Exception e) {
			LOGGER.error("Can't retrieve info, assume not authorized", e);
		}
		return null;
	}

}
