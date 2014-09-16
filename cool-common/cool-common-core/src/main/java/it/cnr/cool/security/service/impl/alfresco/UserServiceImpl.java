package it.cnr.cool.security.service.impl.alfresco;


import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.util.format.GsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLEncoder;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.connector.User;

public class UserServiceImpl implements UserService{
	@Autowired
	private GsonParser gsonParser;
	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public User loadUserForConfirm(String userId) throws CoolUserFactoryException {
		return loadUser(userId, cmisService.getAdminSession());
	}
	
	@Override
	public User loadUser(String userId, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/cnr/person/").concat(encode(userId));
        UrlBuilder url = new UrlBuilder(link);
        url.addParameter("groups", true);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new CoolUserFactoryException("User not found "+userId+" Exception: "+resp.getErrorContent());
		}
		if (status == HttpStatus.SC_UNAUTHORIZED){
			//In questo caso sono l'utente guest
			return new CMISUser(userId);
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.configure(
				    DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.readValue(new InputStreamReader(resp.getStream()), CMISUser.class);
		} catch (JsonParseException e) {
			throw new CoolUserFactoryException("Exception for user "+userId, e);
		} catch (JsonMappingException e) {
			throw new CoolUserFactoryException("Exception for user "+userId, e);
		} catch (IOException e) {
			throw new CoolUserFactoryException("Exception for user "+userId, e);
		}
	}
	
	@Override
	public InputStream findUser(String term, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/api/people").concat("?filter=*"+term+"*");
        UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new CoolUserFactoryException("User not found "+term+" Exception: "+resp.getErrorContent());
		}
		return resp.getStream();
	}

	@Override
	public User findUserByEmail(String email, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/cnr/people").concat("?filter=email:"+email);
        UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new CoolUserFactoryException("User not found "+email+" Exception: "+resp.getErrorContent());
		}
		try {
			JSONObject jsonObject = new JSONObject(StringUtil.convertStreamToString(resp.getStream()));
			JSONArray jsonArray = jsonObject.getJSONArray("people");
			if (jsonArray.length() == 0)
				return null;
			else
				return gsonParser.fromJson( new StringReader(jsonArray.getJSONObject(0).toString()), CMISUser.class);
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	public User findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/cnr/people").concat("?filter=codicefiscale:"+codicefiscale);
        UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new CoolUserFactoryException("User not found "+codicefiscale+" Exception: "+resp.getErrorContent());
		}
		try {
			JSONObject jsonObject = new JSONObject(StringUtil.convertStreamToString(resp.getStream()));
			JSONArray jsonArray = jsonObject.getJSONArray("people");
			if (jsonArray.length() == 0)
				return null;
			else if (jsonArray.length() == 1) {
				return gsonParser.fromJson( new StringReader(jsonArray.getJSONObject(0).toString()), CMISUser.class);
			}else {
				throw new CoolUserFactoryException("For this tax code "+codicefiscale+" found user: "+ jsonArray.length());				
			}
		} catch (JSONException e) {
			return null;
		}
	}
	
	@Override
	public User createUser(final User user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/cnr/person");
		((CMISUser)user).setDisableAccount(true);
        UrlBuilder url = new UrlBuilder(link);
        BindingSession cmisSession = cmisService.getAdminSession();        
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePOST(url, MimeTypes.JSON.mimetype(), 
				new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
            			out.write(gsonParser.toJson(user).getBytes());
            		}
        		}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolUserFactoryException("Create user error. Exception: "+resp.getErrorContent());
		if (status == HttpStatus.SC_CONFLICT)
			throw new CoolUserFactoryException("User name already exists: "+user.getId());
		
		return loadUser(user.getId(), cmisSession);
	}

	@Override
	public User updateUser(final User user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/cnr/person/").concat(encode(user.getId()));
        UrlBuilder url = new UrlBuilder(link);
        BindingSession cmisSession = cmisService.getAdminSession();
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePUT(url, MimeTypes.JSON.mimetype(), null,
				new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
            			out.write(gsonParser.toJson(user).getBytes());
            		}
        		}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolUserFactoryException("Update user error. Exception: "+resp.getErrorContent());

		return loadUser(user.getId(), cmisSession);
	}

	@Override
	public User changeUserPassword(final User user, final String newPassword) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/api/person/changepassword/").concat(encode(user.getId()));
        UrlBuilder url = new UrlBuilder(link);
        BindingSession cmisSession = cmisService.getAdminSession();
        final String respJson = "{\"newpw\":\""+newPassword+"\"}";
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePOST(url, MimeTypes.JSON.mimetype(),
				new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
            			out.write(respJson.getBytes());
            		}
        		}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolUserFactoryException("Update user error. Exception: "+resp.getErrorContent());

		return user;
	}

	@Override
	public void disableAccount(String userName) throws CoolUserFactoryException {

		String link = cmisService.getBaseURL().concat(
				"service/cnr/disableAccount");
		UrlBuilder url = new UrlBuilder(link);

		BindingSession cmisSession = cmisService.getAdminSession();

		final String respJson = "{\"userName\":\"" + userName
				+ "\", \"disableUser\": true}";

		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession)
				.invokePOST(url, MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						out.write(respJson.getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolUserFactoryException("Update user error. Exception: "
					+ resp.getErrorContent());

		try {
			String content = IOUtils.toString(resp.getStream());
			LOGGER.info(content);
		} catch (IOException e) {
			LOGGER.error("unable to get response content, user: " + userName, e);
		}


	}

	@SuppressWarnings("deprecation")
	private static String encode(String s) {
		String encoded;
		try {
			encoded = URIUtil.encodePath(s);
		} catch (URIException e) {
			LOGGER.warn("unable to encode string " + s, e);
			encoded = URLEncoder.encode(s);
		}

		return encoded;
	}

}
