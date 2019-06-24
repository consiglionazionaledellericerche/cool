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

package it.cnr.cool.cmis.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.util.MimeTypes;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ACLService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACLService.class);
	public static String PARAM_INHERITED_PERMISSION = "inheritedPermission";
	@Autowired
	private CMISService cmisService;

	public void removeAcl(BindingSession cmisSession, String nodeRef,
			Map<String, ACLType> permission) {
		managePermission(cmisSession, nodeRef, permission, true);
	}

	public void addAcl(BindingSession cmisSession, String nodeRef,
			Map<String, ACLType> permission) {
		managePermission(cmisSession, nodeRef, permission, false);
	}

	private void managePermission(BindingSession cmisSession, String objectId,
			final Map<String, ACLType> permission, final boolean remove) {
		String link = cmisService.getBaseURL()
				.concat("service/cnr/nodes/permissions/")
				.concat(objectId.replace(":/", ""));
		UrlBuilder url = new UrlBuilder(link);
		Response resp = cmisService.getHttpInvoker(cmisSession).invokePOST(url,
				MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						JSONObject jsonObject = new JSONObject();
						JSONArray jsonArray = new JSONArray();
						for (String authority : permission.keySet()) {
							JSONObject jsonAutority = new JSONObject();
							jsonAutority.put("authority", authority);
							jsonAutority.put("role", permission.get(authority));
							if (remove)
								jsonAutority.put("remove", remove);
							jsonArray.put(jsonAutority);
						}
						jsonObject.put("permissions", jsonArray);
						out.write(jsonObject.toString().getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();

		LOGGER.info((remove ? "remove" : "add") + " permission " + permission + " on item "
				+ objectId + ", status = " + status);

		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Create user error. Exception: "
					+ resp.getErrorContent());
	}

	public void changeOwnership(BindingSession cmisSession,
			final String nodeRef, final String userId, final boolean children,
			final List<String> excludedTypes) {
		String link = cmisService.getBaseURL().concat(
				"service/cnr/nodes/owner");
		UrlBuilder url = new UrlBuilder(link);
		Response resp = cmisService.getHttpInvoker(cmisSession).invokePOST(url,
				MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("nodeRef", nodeRef);
						jsonObject.put("userid", userId);
						jsonObject.put("children", children);
						jsonObject.put("excludedTypes", excludedTypes);
						out.write(jsonObject.toString().getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Create user error. Exception: "
					+ resp.getErrorContent());
	}

	public void setInheritedPermission(BindingSession cmisSession,
			String objectId, final Boolean inheritedPermission) {
		String link = cmisService.getBaseURL()
				.concat("service/cnr/nodes/permissions/")
				.concat(objectId.replace(":/", ""));
		UrlBuilder url = new UrlBuilder(link);
		Response resp = cmisService.getHttpInvoker(cmisSession).invokePOST(url,
				MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						JSONObject jsonObject = new JSONObject();
						JSONArray jsonArray = new JSONArray();
						jsonObject.put("permissions", jsonArray);
						jsonObject.put("isInherited", inheritedPermission);
						out.write(jsonObject.toString().getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Create user error. Exception: "
					+ resp.getErrorContent());
	}

	public JsonObject getPermission(BindingSession cmisSession, String objectId) {
		String link = cmisService.getBaseURL()
				.concat("service/cnr/nodes/permissions/")
				.concat(objectId.replace(":/", ""));
		UrlBuilder url = new UrlBuilder(link);
		Response resp = cmisService.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Find permission error. Exception: "
					+ resp.getErrorContent());
		InputStreamReader responseReader = new InputStreamReader(resp.getStream());
		return new JsonParser().parse(responseReader)
				.getAsJsonObject();
	}
}
