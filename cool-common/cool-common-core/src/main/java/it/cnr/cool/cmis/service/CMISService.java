package it.cnr.cool.cmis.service;

import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CMISService implements InitializingBean, CMISSessionManager {
	private static final String SET_COOKIE = "Set-Cookie";

	static final Locale DEFAULT_LOCALE = Locale.ITALY;

	private static final int SESSION_DURATION_MINUTES = 5; // TODO: gestire la scadenza di un ticket in seguito a riavvio Alfresco

	private static final Logger LOGGER = LoggerFactory.getLogger(CMISService.class);

    public static final String DEFAULT_SERVER = "cmis.default";
    public static final String BINDING_SESSION = "cmis.binding.session";
    public static final String SIPER_BINDING_SESSION = "siper.binding.session";
    public static final String ALFRESCO_TICKET = "alfresco.user.ticket";
    public static final String QUERY_RESULT = "cmis.query.result";
    public static final String TOTAL_NUM_ITEMS = "cmis.query.total.num.items";
	public static final String SESSION_ID = "session.id";
	public static final String EXPIRY = "session.expiration";

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
			try {
				String ticket = getTicket(username, password);
				username = "";
				password = ticket;
			} catch (Exception e) {
				LOGGER.error("unable to get ticket for user: " + username);
			}

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
			try {
				String ticket = getTicket(username, password);
				username = "";
				password = ticket;
			} catch (Exception e) {
				LOGGER.error("unable to get ticket for user: " + username);
			}

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
        sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CustomAuthenticationProvider.class.getName());
        sessionParameters.put(SessionParameter.CACHE_SIZE_TYPES, "1000");
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

		if (adminSession == null
				|| isPast((Long) adminSession.get(CMISService.EXPIRY))) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.ADMIN_USERNAME);
			String password = params.get(CMISConfig.ADMIN_PASSWORD);

			SessionImpl session;
			try {
				String ticket = getTicket(username, password);
				session = createBindingSession("", ticket);
				session.put(CMISService.ALFRESCO_TICKET, ticket);
				// TODO: per creare meno sessioni l'admin dovrebbe andare sempre
				// sullo stesso nodo
				session.put(CMISService.SESSION_ID, getSessionId(ticket));
				session.put(CMISService.EXPIRY, getExpiration());
			} catch (Exception e) {
				LOGGER.warn("cannot set ticket and sessionid on admin session, fallback to standard username/password auth");
				session = createBindingSession(username, password);
			}

			adminSession = session;

		}

		return adminSession;
    }

    public SessionImpl createBindingSession(){

		if (cmisGuestBindingSession == null
				|| isPast((Long) cmisGuestBindingSession
						.get(CMISService.EXPIRY))) {

			Map<String, String> params = cmisConfig.getServerParameters();
			String username = params.get(CMISConfig.GUEST_USERNAME);
			String password = params.get(CMISConfig.GUEST_PASSWORD);

			SessionImpl session;
			try {
				String ticket = getTicket(username, password);
				session = createBindingSession("", ticket);
				session.put(CMISService.ALFRESCO_TICKET, ticket);
				// TODO: per creare meno sessioni il guest dovrebbe andare
				// sempre sullo stesso nodo
				session.put(CMISService.SESSION_ID, getSessionId(ticket));
				session.put(CMISService.EXPIRY, getExpiration());
			} catch (Exception e) {
				LOGGER.warn("cannot set ticket and sessionid on guest session, fallback to standard username/password auth");
				session = createBindingSession(username, password);
			}

			cmisGuestBindingSession = session;

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
        sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CustomAuthenticationProvider.class.getName());
        if (!sessionParameters.containsKey(SessionParameter.AUTHENTICATION_PROVIDER_CLASS)) {
            sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CmisBindingFactory.STANDARD_AUTHENTICATION_PROVIDER);
        }
        sessionParameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        sessionParameters.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, "false");
        sessionParameters.put(SessionParameter.CACHE_SIZE_TYPES, "1000");
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

	public String getTicket(String username, String password)
			throws LoginException {

		String ticketURL = getBaseURL() + "service/api/login.json";

		PostMethod method = new PostMethod(ticketURL);

		JSONObject body = new JSONObject();
		try {
			body.put("username", username);
			body.put("password", password);

			RequestEntity requestEntity = new StringRequestEntity(
					body.toString(), "text/plain", "UTF-8");
			method.setRequestEntity(requestEntity);

			if (new HttpClient().executeMethod(method) != HttpStatus.SC_OK) {
				throw new LoginException("Login failed for user " + username
						+ " with HTTP status code: " + method.getStatusLine());
			} else {
				String json = new String(method.getResponseBody());
				JsonObject response = new JsonParser().parse(json)
						.getAsJsonObject();

				return response.getAsJsonObject().get("data")
						.getAsJsonObject().get("ticket").getAsString();
			}

		} catch (Exception e) {
			throw new LoginException("unable to create ticket for user "
					+ username, e);
		}

	}

	public boolean validateTicket(String ticket, BindingSession bindingSession) {

		boolean valid = false;

		String ticketURL = getBaseURL() + "service/api/login/ticket/" + ticket;

		LOGGER.debug(ticketURL);

		try {
			UrlBuilder url = new UrlBuilder(ticketURL);

			Response res = getHttpInvoker(bindingSession).invokeGET(url,
					bindingSession);
			int status = res.getResponseCode();
			valid = (status == HttpStatus.SC_OK);

			String template = "ticket %s is %s, status %d";

			String message = String.format(template, ticket, (valid ? "valid"
					: "invalid"), status);

			LOGGER.debug(message);
		} catch (Exception e) {
			LOGGER.warn("unable to validate ticket " + ticket, e);
		}

		return valid;

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



	public String getSessionId(String ticket) {

		GetMethod method = new GetMethod(atompubURL);
		String encoding = new String(Base64.encodeBase64((":" + ticket)
				.getBytes()));

		HttpClient client = new HttpClient();

		method.addRequestHeader("Authorization", "Basic " + encoding);

		try {
			int r = client.executeMethod(method);

			Pattern pattern = Pattern
					.compile("(JSESSIONID=[0-9A-F]+\\.[a-zA-Z0-9]+).*;");


			if (r == HttpStatus.SC_OK && method.getResponseHeader(SET_COOKIE) != null) {
				
				String setCookieHeader = method.getResponseHeader(SET_COOKIE)
						.getValue();
				Matcher matcher = pattern.matcher(setCookieHeader);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}

		} catch (HttpException e) {
			LOGGER.error("unable to get session id", e);
		} catch (IOException e) {
			LOGGER.error("unable to get session id", e);
		}

		return null;
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
			LOGGER.info("session is null, return CMISUser null");
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
		return atompubURL.replace("cmisatom", "").replace("service/cmis", "");
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
