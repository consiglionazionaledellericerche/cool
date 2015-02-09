package it.cnr.cool.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CmisAuthRepository;
import it.cnr.cool.cmis.service.LoginException;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.UserFactoryException;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

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
			String ticket = getTicket(username, password);


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


    public String getTicket(String username, String password)
            throws LoginException {

        String ticketURL = cmisService.getBaseURL() + "service/api/login.json";

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

                return response.getAsJsonObject().get("data").getAsJsonObject()
                        .get("ticket").getAsString();
            }

        } catch (Exception e) {
            throw new LoginException("unable to create ticket for user "
                    + username, e);
        }

    }


}
