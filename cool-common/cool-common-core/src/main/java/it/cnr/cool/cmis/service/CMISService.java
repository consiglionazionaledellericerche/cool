package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.service.impl.ObjectTypeCacheImpl;
import it.cnr.cool.dto.Credentials;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.impl.SessionImpl;
import org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpInvoker;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CMISService implements InitializingBean, CMISSessionManager {

    static final Locale DEFAULT_LOCALE = Locale.ITALY;

	private static final Logger LOGGER = LoggerFactory.getLogger(CMISService.class);

    public static final String AUTHENTICATION_HEADER = "X-alfresco-ticket";
    public static final String COOKIE_TICKET_NAME = "ticket";

    // service dependencies
    @Autowired
    private CMISConfig cmisConfig;

    private String baseURL;

    // OpenCMIS session factory
    private static SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

    @Autowired
    private OperationContext cmisDefaultOperationContext;

    @Autowired
    private CmisAuthRepository cmisAuthRepository;

    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

	/**
	 *
	 * Get a GUEST CMIS Session (or create a new one)
	 *
	 * @return
	 */
    private Session createGuestSession()
    {
        LOGGER.debug("asked for a guest session");
        return cmisAuthRepository.getGuestSession();
    }

    @Override
    public Session createAdminSession()
    {
        LOGGER.debug("asked for an admin session");
        return  cmisAuthRepository.getAdminSession();
    }


    /**
     * This method checks first whether a repository Id has been specified in the configuration,
     * and if not defaults to the first repository returned by the CMIS server
     * @param username
     * @param password
     *
     * @return
     */
    public Session getRepositorySession(String username, String password)
    {
        Map<String, String> parameters = cmisConfig.getServerParameters();
        if (parameters == null)
        {
            return null;
        }

        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.putAll(parameters);
        sessionParameters.put(SessionParameter.USER, username);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        sessionParameters.put(SessionParameter.TYPE_DEFINITION_CACHE_CLASS, ObjectTypeCacheImpl.class.getName());
        sessionParameters.put(SessionParameter.LOCALE_ISO3166_COUNTRY, DEFAULT_LOCALE.getCountry());
        sessionParameters.put(SessionParameter.LOCALE_ISO639_LANGUAGE, DEFAULT_LOCALE.getLanguage());
        sessionParameters.put(SessionParameter.LOCALE_VARIANT, DEFAULT_LOCALE.getVariant());

        Session session = sessionFactory.createSession(sessionParameters);
        session.setDefaultContext(cmisDefaultOperationContext);
        return session;
    }


    public BindingSession getAdminSession(){
        LOGGER.debug("requested an admin binding session");
        return cmisAuthRepository.getAdminBindingSession();
    }

    private BindingSession createBindingSession(){
        LOGGER.debug("requested a guest binding session");
        return cmisAuthRepository.getGuestBindingSession();
    }

    public BindingSession createBindingSession(String username, String password){
    	SessionImpl session = new SessionImpl();
        Map<String, String> parameters = cmisConfig.getServerParameters();
        if (parameters == null)
        {
            return null;
        }
        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.putAll(parameters);
        sessionParameters.put(SessionParameter.USER, username);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        if (!sessionParameters.containsKey(SessionParameter.AUTHENTICATION_PROVIDER_CLASS)) {
            sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CmisBindingFactory.STANDARD_AUTHENTICATION_PROVIDER);
        }
        sessionParameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        sessionParameters.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, "false");
        sessionParameters.put(SessionParameter.TYPE_DEFINITION_CACHE_CLASS, ObjectTypeCacheImpl.class.getName());
        for (Map.Entry<String, String> entry : sessionParameters.entrySet()) {
            session.put(entry.getKey(), entry.getValue());
        }
        // create authentication provider and add it session
        String authProvider = sessionParameters.get(SessionParameter.AUTHENTICATION_PROVIDER_CLASS);
        if (authProvider != null) {
            Object authProviderObj = null;

            try {
                authProviderObj = Class.forName(authProvider).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not load authentication provider: " + e, e);
            }

            if (!(authProviderObj instanceof AbstractAuthenticationProvider)) {
                throw new IllegalArgumentException(
                        "Authentication provider does not extend AbstractAuthenticationProvider!");
            }

            session.put(CmisBindingsHelper.AUTHENTICATION_PROVIDER_OBJECT,
                    (AbstractAuthenticationProvider) authProviderObj);
            ((AbstractAuthenticationProvider) authProviderObj).setSession(session);
        }
    	return session;
    }


	@Override
	public void afterPropertiesSet() throws Exception {
	}




	// utility methods

	/**
	 * Gets the HTTP Invoker object.
	 */
    public HttpInvoker getHttpInvoker(BindingSession session) {
		return CmisBindingsHelper.getHttpInvoker(session);
	}


	public String getBaseURL() {
        if (baseURL != null)
            return baseURL;
		else {
			throw new RuntimeException(
					"you must set a repository.base.url property");
		}
	}

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }




    public BindingSession getCurrentBindingSession(HttpServletRequest req) {

        String ticket = extractTicketFromRequest(req);

        BindingSession bindingSession;

        if (ticket == null) {
            bindingSession = createBindingSession();
            LOGGER.debug("empty ticket, returning guest binding session");
        } else {
            bindingSession = cmisAuthRepository.getBindingSession(ticket);
            LOGGER.debug("retrieved binding session: " + bindingSession);
        }



        return bindingSession;
    }


    public Session getCurrentCMISSession(HttpServletRequest req) {

        String ticket = extractTicketFromRequest(req);

        if (ticket == null) {
            LOGGER.debug("cmis session nulla, returning guest session");
            return createGuestSession();
        } else {
            LOGGER.debug("sessione: " + ticket);

            Session session = cmisAuthRepository.getSession(ticket);
            return session;
        }


    }


    public CMISUser getCMISUserFromSession(HttpServletRequest request) {
        String ticket = extractTicketFromRequest(request);

        BindingSession bindingSession = cmisAuthRepository.getBindingSession(ticket);

        CMISUser user;

        if (ticket != null) {
            user = cmisAuthRepository.getCachedCMISUser(ticket, bindingSession);
        } else {
            user = null;
        }


        if (user == null) {
            LOGGER.debug("user is null, assuming a guest");
            user = new CMISUser("guest");

            Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
            capabilities.put(CMISUser.CAPABILITY_GUEST, true);
            user.setCapabilities(capabilities);

        } else {
            LOGGER.info("retrieved user: {} with ticket: {} ", user.getId(), ticket);
        }
        return user;
    }

    public String extractTicketFromRequest(HttpServletRequest req) {

        String ticket;

        String authorization = req.getHeader("Authorization");

        if (authorization != null) {

            LOGGER.info("basic auth: " + authorization);

            Credentials credentials = extractCredentials(authorization);

            String username = credentials.getUsername();
            String password = credentials.getPassword();

            LOGGER.debug("basic auth user: " + username);

            ticket = cmisAuthenticatorFactory.authenticate(username, password);

        } else {
            ticket = req.getHeader(AUTHENTICATION_HEADER);
        }


        if (ticket != null) {
            LOGGER.info("extracted ticket: " + ticket);
        } else {
            Cookie[] cookies = req.getCookies();

            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(COOKIE_TICKET_NAME)) {
                        ticket = Optional.of(cookie.getValue()).filter(x -> x.length() > 0).orElse(null);
                        LOGGER.info("using ticket {} given by cookie", ticket);
                    }
                }
            }

        }

        return ticket;
    }



    private static Credentials extractCredentials(String authorization) {

        if (authorization == null || authorization.isEmpty()) {
            LOGGER.debug("no authorization header provided");
            return null;
        }

        String usernameAndPasswordBase64 = authorization.split(" ")[1];

        byte[] usernameAndPasswordByteArray = DatatypeConverter.parseBase64Binary(usernameAndPasswordBase64);

        String [] usernameAndPassword = new String(usernameAndPasswordByteArray).split(":");

        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        LOGGER.info("using BASIC auth for user: " + username);

        return new Credentials(username, password);

    }


}
