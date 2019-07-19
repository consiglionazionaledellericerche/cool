/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.security.service.impl.alfresco;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.listener.LogoutListener;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.util.UriUtils;
import it.cnr.cool.util.format.GsonParser;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
@Service
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
	
	private List<LogoutListener> logutListener = new ArrayList<LogoutListener>();

	private	ObjectMapper mapper = new ObjectMapper();
 
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public CMISUser loadUserForConfirm(String userId)
			throws CoolUserFactoryException {
		return loadUser(userId, cmisService.getAdminSession());
	}

	public boolean isUserExists(String userId) {
		final BindingSession adminSession = cmisService.getAdminSession();
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON + "/").concat(UriUtils.encode(userId));
		UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(adminSession).invokeGET(url, adminSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND)
			return false;
		return true;
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
		try {
	        user.clearForPersist();
			byte[] userJson = mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsBytes(user);
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
		} catch (IOException e) {
			throw new CoolUserFactoryException("Create user error.", e);
		}
		return loadUser(user.getId(), cmisSession);
	}

	@Override
	public CMISUser updateUser(final CMISUser user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON + "/").concat(UriUtils.encode(user.getId()));
        UrlBuilder url = new UrlBuilder(link);
        BindingSession cmisSession = cmisService.getAdminSession();
		try {
	        user.clearForPersist();
	        byte[] userJson = mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsBytes(user);
	        Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePUT(url, MimeTypes.JSON.mimetype(), null,
					new Output() {
						@Override
						public void write(OutputStream out) throws Exception {
	            			out.write(userJson);
	            		}
	        		}, cmisSession);
			int status = resp.getResponseCode();
			if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
				throw new CoolUserFactoryException("Update user error. Exception: "+resp.getErrorContent());
		} catch (IOException e) {
			throw new CoolUserFactoryException("Create user error.", e);
		}

		return loadUser(user.getId(), cmisSession);
	}

	@Override
	public void deleteUser(final CMISUser user) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat(SERVICE_CNR_PERSON_PERSON + "/").concat(UriUtils.encode(user.getId()));
		UrlBuilder url = new UrlBuilder(link);
		BindingSession cmisSession = cmisService.getAdminSession();
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeDELETE(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolUserFactoryException("Update user error. Exception: "+resp.getErrorContent());
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
	public boolean addLogoutListener(LogoutListener logoutListener) {
		return logutListener.add(logoutListener);
	}
	
	public void logout(String userId) {
		logutListener.stream().forEach(listener -> listener.logout(userId));
	}

	@Override
	public URI getRedirect(CMISUser cmisUser, URI uri) {
		return uri;
	}
}
