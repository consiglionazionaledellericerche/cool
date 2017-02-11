package it.cnr.cool.security.service.impl.alfresco;


import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.util.UriUtils;
import it.cnr.cool.util.format.GsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
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

public class UserServiceImpl implements UserService{
	private static final String SERVICE_CNR_PERSON_DISABLE_ACCOUNT = "service/cnr/person/disable-account",
			SERVICE_API_PERSON_CHANGEPASSWORD = "service/api/person/changepassword/",
			SERVICE_CNR_GROUPS = "service/cnr/groups/",
			SERVICE_CNR_PERSON_PERSON = "service/cnr/person/person",
			SERVICE_CNR_PERSON_PEOPLE = "service/cnr/person/people";
	@Autowired
	private GsonParser gsonParser;
	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public CMISUser loadUserForConfirm(String userId)
			throws CoolUserFactoryException {
		return loadUser(userId, cmisService.getAdminSession());
	}

	@Override
	public CMISUser loadUser(String userId, BindingSession cmisSession)
			throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON + "/").concat(UriUtils.encode(userId));
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
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PEOPLE).concat("?filter=*"+term+"*");
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
	public List<String> findMembers(String groupName, BindingSession cmisSession) throws CoolUserFactoryException{
		List<String> result = new ArrayList<String>();
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_GROUPS).concat(UriUtils.encode(groupName)).concat("/members");
        UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			throw new CoolUserFactoryException("Members not found "+groupName+" Exception: "+resp.getErrorContent());
		}
		try {
			JSONObject jsonObject = new JSONObject(StringUtil.convertStreamToString(resp.getStream()));
			JSONArray jsonArray = jsonObject.getJSONArray("people");
			for (int i = 0; i < jsonArray.length(); i++) {
				result.add(jsonArray.getString(i));
			}
		} catch (JSONException e) {
			LOGGER.error("json exception", e);
			return result;
		}
		return result;
	}
	
	@Override
	public CMISUser findUserByEmail(String email, BindingSession cmisSession)
			throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PEOPLE).concat("?filter=email:"+email);
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
			LOGGER.error("json exception", e);
			return null;
		}
	}

	@Override
	public CMISUser findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PEOPLE).concat("?filter=codicefiscale:"+codicefiscale);
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
			LOGGER.error("json exception", e);
			return null;
		}
	}

	@Override
	public CMISUser createUser(final CMISUser user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON);
		user.setDisableAccount(true);
        UrlBuilder url = new UrlBuilder(link);
        BindingSession cmisSession = cmisService.getAdminSession();
        final byte[] userJson = gsonParser.toJson(user).getBytes();
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePOST(url, MimeTypes.JSON.mimetype(),
				new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
            			out.write(userJson);
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
	public CMISUser updateUser(final CMISUser user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON + "/").concat(UriUtils.encode(user.getId()));
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
	public CMISUser changeUserPassword(final CMISUser user, final String newPassword) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_API_PERSON_CHANGEPASSWORD).concat(UriUtils.encode(user.getId()));
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
	public void enableAccount(String userName) throws CoolUserFactoryException {
		manageAccount(userName, false);
	}	
	@Override
	public void disableAccount(String userName) throws CoolUserFactoryException {
		manageAccount(userName, true);
	}

	private void manageAccount(String userName, boolean disable) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_DISABLE_ACCOUNT);
		UrlBuilder url = new UrlBuilder(link);
		BindingSession cmisSession = cmisService.getAdminSession();
		final String respJson = "{\"userName\":\"" + userName
				+ "\", \"disableUser\": " + disable + "}";
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
}
