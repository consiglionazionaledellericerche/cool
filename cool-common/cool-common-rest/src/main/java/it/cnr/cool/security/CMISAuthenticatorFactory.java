package it.cnr.cool.security;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.AbstractUserFactory;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

public class CMISAuthenticatorFactory extends AbstractUserFactory implements ServletAuthenticatorFactory{
    private static final Logger LOGGER = LoggerFactory.getLogger(CMISAuthenticatorFactory.class);

	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

	@Override
	public User loadUser(RequestContext context, String userId)
			throws UserFactoryException {
		return loadUser(context, userId, null);
	}

	@Override
	public User loadUser(RequestContext context, String userId,
			String endpointId) throws UserFactoryException {
		try {
			BindingSession bindingSession = cmisService.getCurrentBindingSession(ServletUtil.getRequest());
			return userService.loadUser(userId, bindingSession);
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException("Error loading user: " + userId, e);
		}
	}

	@Override
	public boolean authenticate(HttpServletRequest request, String username,
			String password) {
        boolean authorized = false;
		try{
			String ticket = cmisService.getTicket(username, password);
            Session cmisSession = cmisService.createSession("", ticket);
            HttpSession session = request.getSession(true);
			session.setAttribute(CMISService.ALFRESCO_TICKET, ticket);
			session.setAttribute(CMISService.SESSION_ID, cmisService.getSessionId(ticket));
            session.setAttribute(CMISService.DEFAULT_SERVER, cmisSession);
            session.setAttribute(CMISService.BINDING_SESSION,cmisService.createBindingSession("", ticket));
            session.setAttribute(CMISService.SIPER_BINDING_SESSION,cmisService.createBindingSession(username, password));

            authorized = true;
        }
        catch(Exception e)
        {
			LOGGER.debug("Can't retrieve info, assume not authorized", e);
        }
        return authorized;
	}

	@Override
	public Authenticator create(WebScriptServletRequest req,
			WebScriptServletResponse res) {
		return new BasicHttpAuthenticator(req, res);
	}
    /**
     * HTTP Basic Authentication
     */
    public class BasicHttpAuthenticator implements Authenticator
    {
        // dependencies
        private final WebScriptServletRequest servletReq;
        private final WebScriptServletResponse servletRes;

        private final String authorization;
        private final Object userId;

        /**
         * Construct
         *
         * @param authenticationService
         * @param req
         * @param res
         */
        public BasicHttpAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res)
        {
            this.servletReq = req;
            this.servletRes = res;

            HttpServletRequest httpReq = servletReq.getHttpServletRequest();
            this.authorization = httpReq.getHeader("Authorization");
			HttpSession session = httpReq.getSession(false);
			this.userId = session != null ? session
					.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID)
					: null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#authenticate(org.alfresco.web.scripts.Description.RequiredAuthentication, boolean)
         */
		@Override
		@SuppressWarnings("PMD.UselessParentheses")
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            boolean authorized = false;

            String runas = servletReq.getServiceMatch().getWebScript().getDescription().getRunAs();
            // validate credentials
            HttpServletRequest req = servletReq.getHttpServletRequest();
            HttpServletResponse res = servletRes.getHttpServletResponse();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("HTTP Authorization provided: " + (authorization != null && authorization.length() != 0));
			if (userId != null
					|| (required.equals(RequiredAuthentication.guest) &&
						req.getParameter("guest") != null &&
						Boolean.valueOf(req.getParameter("guest")))
				)
            	return true;
            // authenticate as specified by HTTP Basic Authentication
            if (authorization != null && authorization.length() != 0)
            {
                String[] authorizationParts = authorization.split(" ");
                if (!authorizationParts[0].equalsIgnoreCase("basic"))
                {
                    throw new WebScriptException("Authorization '" + authorizationParts[0] + "' not supported.");
                }
                String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
                String[] parts = decodedAuthorisation.split(":");

                if (parts.length == 2)
                {
                    // assume username and password passed as the parts
                    String username = parts[0];
                    String password = parts[1];
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Authenticating (BASIC HTTP) user " + parts[0]);
                    authorized = CMISAuthenticatorFactory.this.authenticate(req, username, password);
                    if (authorized){
						try {
							BindingSession bindingSession = cmisService.getCurrentBindingSession(ServletUtil.getRequest());

							User user = userService.loadUser(username, bindingSession);
	                    	if ((required.equals(RequiredAuthentication.user) && user.isGuest() && runas == null)||
	                    		(required.equals(RequiredAuthentication.admin) && !user.isAdmin() && runas == null)){
	                    			authorized = false;
	                    	}else if (runas != null){
								authorized = isAutenticated(user);
	                    	}
						} catch (CoolUserFactoryException e) {
							throw new WebScriptException("Error loading user: " + username, e);
						}
                    }
                }
            }

            // request credentials if not authorized
            if (!authorized)
            {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Requesting authorization credentials");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setHeader("WWW-Authenticate", "Basic realm=\"Cool\"");
            }

            return authorized;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
         */
		@Override
		public boolean emptyCredentials()
        {
            return authorization == null || authorization.length() == 0;
        }
    }

	private boolean isAutenticated(User user) {
		if (user.isGuest())
			return false;
		boolean login = false;
        if (user.isAdmin())
        	login = true;
		return login;
	}
}
