package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.service.impl.ObjectTypeCacheImpl;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class CMISService implements InitializingBean, CMISSessionManager {

	static final Locale DEFAULT_LOCALE = Locale.ITALY;

	private static final int SESSION_DURATION_MINUTES = 5; // TODO: gestire la scadenza di un ticket in seguito a riavvio Alfresco

	private static final Logger LOGGER = LoggerFactory.getLogger(CMISService.class);

    public static final String DEFAULT_SERVER = "cmis.default";
    public static final String BINDING_SESSION = "cmis.binding.session";
    public static final String SIPER_BINDING_SESSION = "siper.binding.session";
    public static final String QUERY_RESULT = "cmis.query.result";
    public static final String TOTAL_NUM_ITEMS = "cmis.query.total.num.items";

    // service dependencies
    @Autowired
    private CMISConfig cmisConfig;

    private SessionImpl adminSession;
    private Session cmisAdminSession;
    private Session cmisGuestSession;
	private long cmisGuestSessionExpiration;
	private long cmisAdminSessionExpiration;

	private String atompubURL;
    private String baseURL;

	private SessionImpl cmisGuestBindingSession;

    // OpenCMIS session factory
    private static SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
    @Autowired
    private OperationContext cmisDefaultOperationContext;


	private String repositoryId;
    /**
     * Construct
     */
    public CMISService()
    {
    }

    public List<Repository> getRepositories(String username, String password)
    {
        return getRepositories(DEFAULT_SERVER, username, password);
    }

    public List<Repository> getRepositories(String server, String username, String password)
    {
    	return getRepositories(cmisConfig, server, username, password);
    }

    public List<Repository> getRepositories(CMISConfig config, String server, String username, String password)
    {
        Map<String, String> parameters = config.getServerParameters();
        if (parameters == null)
        {
            return null;
        }
        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.putAll(parameters);
        sessionParameters.put(SessionParameter.USER, username);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        return getRepositories(sessionParameters);
    }

    public List<Repository> getRepositories(Map<String, String> parameters)
    {
        return sessionFactory.getRepositories(parameters);
    }

	/**
	 *
	 * Get a GUEST CMIS Session (or create a new one)
	 *
	 * @return
	 */
    public Session createSession()
    {
		if (cmisGuestSession == null || isPast(cmisGuestSessionExpiration)) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.GUEST_USERNAME);
			String password = params.get(CMISConfig.GUEST_PASSWORD);

			cmisGuestSession = createSession(DEFAULT_SERVER, username, password);
			cmisGuestSessionExpiration = getExpiration();

		}
    	return cmisGuestSession;
    }

    @Override
	public Session createAdminSession()
    {
		// TODO: copiaincollato da
		// it.cnr.cool.cmis.service.CMISService.createSession()

		if (cmisAdminSession == null || isPast(cmisAdminSessionExpiration)) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.ADMIN_USERNAME);
			String password = params.get(CMISConfig.ADMIN_PASSWORD);

			cmisAdminSession = createSession(DEFAULT_SERVER, username, password);
			cmisAdminSessionExpiration = getExpiration();

		}
    	return cmisAdminSession;
    }

    public Session createSession(String username, String password)
    {
        return createSession(DEFAULT_SERVER, username, password);
    }

    public Session createSession(String server, String username, String password)
    {
        return getRepositorySession(server,username,password);
    }

    /**
     * This method checks first whether a repository Id has been specified in the configuration,
     * and if not defaults to the first repository returned by the CMIS server
     *
     * @param server
     * @param username
     * @param password
     * @return
     */
    private Session getRepositorySession(String server, String username, String password)
    {
        Map<String, String> parameters = cmisConfig.getServerParameters();
        if (parameters == null)
        {
            return null;
        }
        if (repositoryId == null) {
            if(parameters.containsKey(SessionParameter.REPOSITORY_ID)) {
            	repositoryId = parameters.get(SessionParameter.REPOSITORY_ID);
            } else {
            	repositoryId = getRepositories(server, username, password).get(0).getId();
            }
        }
        return createSession(server, repositoryId, username, password);
    }

    public Session createSession(String server, String repositoryId, String username, String password)
    {
    	return createSession(cmisConfig, server, repositoryId, username, password);
    }

    public Session createSession(CMISConfig config, String server, String repositoryId, String username, String password)
    {
        Map<String, String> parameters = config.getServerParameters();
        if (parameters == null)
        {
            return null;
        }
        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.putAll(parameters);
        sessionParameters.put(SessionParameter.USER, username);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        sessionParameters.put(SessionParameter.REPOSITORY_ID, repositoryId);
        sessionParameters.put(SessionParameter.TYPE_DEFINITION_CACHE_CLASS, ObjectTypeCacheImpl.class.getName());
        sessionParameters.put(SessionParameter.LOCALE_ISO3166_COUNTRY, DEFAULT_LOCALE.getCountry());
        sessionParameters.put(SessionParameter.LOCALE_ISO639_LANGUAGE, DEFAULT_LOCALE.getLanguage());
        sessionParameters.put(SessionParameter.LOCALE_VARIANT, DEFAULT_LOCALE.getVariant());
        return createSession(sessionParameters);
    }

    public Session createSession(Map<String, String> parameters)
    {
    	Session session = sessionFactory.createSession(parameters);
    	session.setDefaultContext(cmisDefaultOperationContext);
        return session;
    }

    public String getAdminUserId() {
    	return cmisConfig.getServerParameters().get(CMISConfig.ADMIN_USERNAME);
    }

    public SessionImpl getAdminSession(){

		// TODO: vedi
		// it.cnr.cool.cmis.service.CMISService.createBindingSession()

		if (adminSession == null) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.ADMIN_USERNAME);
			String password = params.get(CMISConfig.ADMIN_PASSWORD);

			adminSession = createBindingSession(username, password);

		}

		return adminSession;
    }

    public SessionImpl createBindingSession(){

		if (cmisGuestBindingSession == null) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.GUEST_USERNAME);
			String password = params.get(CMISConfig.GUEST_PASSWORD);

			cmisGuestBindingSession = createBindingSession(username, password);

		}

		return cmisGuestBindingSession;
    }

    public SessionImpl createBindingSession(String username, String password){
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


	public void setAtompubURL(String atompubURL) {
		this.atompubURL = atompubURL;
	}


	@Override
	public Session getCurrentCMISSession(HttpSession se) {
		if (se == null) {
			return createSession();
		} else {
			if (se.getAttribute(CMISService.DEFAULT_SERVER) == null)
				se.setAttribute(CMISService.DEFAULT_SERVER, createSession());
			return (Session) se.getAttribute(CMISService.DEFAULT_SERVER);
		}
	}

	public BindingSession getSiperCurrentBindingSession(HttpSession se) {

        if (se == null) {
            return createBindingSession();
        } else {
    		if (se.getAttribute(CMISService.SIPER_BINDING_SESSION) == null)
    			se.setAttribute(CMISService.SIPER_BINDING_SESSION,
    					createBindingSession(cmisConfig.getServerParameters().get(CMISConfig.ADMIN_USERNAME),
    							cmisConfig.getServerParameters().get(CMISConfig.ADMIN_PASSWORD)));
        }
		return (BindingSession)se.getAttribute(CMISService.SIPER_BINDING_SESSION);
	}

	public BindingSession getCurrentBindingSession(HttpServletRequest req) {
		HttpSession se = req.getSession(false);
        if (se == null) {
            return createBindingSession();
        } else {
    		if (se.getAttribute(CMISService.BINDING_SESSION) == null)
    			se.setAttribute(CMISService.BINDING_SESSION,createBindingSession(cmisConfig.getServerParameters().get(CMISConfig.ADMIN_USERNAME),
            		cmisConfig.getServerParameters().get(CMISConfig.ADMIN_PASSWORD)));
        }
		return (BindingSession)se.getAttribute(CMISService.BINDING_SESSION);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}




	// utility methods

	/**
	 *
	 * Check if a time parameter is past
	 *
	 * @param time
	 *            in millisecond since 1970
	 * @return true if the parameter is before now
	 */
	private boolean isPast(Long time) {
		return time == null || time < new Date().getTime();
	}

	/**
	 *
	 *
	 * @return expiration as milliseconds since 1970
	 */
	private long getExpiration() {
		return new Date().getTime() + (1000 * 60 * SESSION_DURATION_MINUTES);
	}

	public CMISUser getCMISUserFromSession(HttpSession session) {

		CMISUser user = null;

		if (session != null) {
			user = ((CMISUser) session
					.getAttribute(CMISUser.SESSION_ATTRIBUTE_KEY_USER_OBJECT));
			LOGGER.debug("Retrieved from session user "
					+ (user == null ? "guest" : user.getId()));
		} else {
			LOGGER.debug("session is null, return CMISUser null");
		}

		return user;
	}

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


	public void updateDocument(Session session, String path, String content) {

		Document document = getDocument(session, path);
		String name = document.getName();
		String mimeType = document.getContentStreamMimeType();

		ContentStreamImpl cs = new ContentStreamImpl(name, mimeType, content);

		document.setContentStream(cs, true, true);
	}

	public InputStream getDocumentInputStream(Session session, String path) {
		return getDocument(session, path).getContentStream().getStream();
	}

	public Document getDocument(Session session, String path) {
		return (Document) session.getObjectByPath(path);
	}


}
