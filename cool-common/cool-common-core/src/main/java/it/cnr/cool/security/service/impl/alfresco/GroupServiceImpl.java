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

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.GroupService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.UriUtils;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService{
	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

	@Override
	public CMISGroup createGroup(String group_name, String display_name, BindingSession cmisSession)
			throws CoolUserFactoryException {
		try {
			String link = cmisService.getBaseURL().concat("service/cnr/groups/group");
	        UrlBuilder url = new UrlBuilder(link);
	        CMISGroup group = new CMISGroup(group_name, display_name);
			ObjectMapper mapper = new ObjectMapper();
	        final byte[] groupJson  = mapper.writeValueAsBytes(group);
			Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokePOST(url, MimeTypes.JSON.mimetype(),
					new Output() {
						@Override
						public void write(OutputStream out) throws Exception {
	            			out.write(groupJson);
	            		}
	        		}, cmisSession);
			int status = resp.getResponseCode();
			if (status == HttpStatus.SC_NOT_FOUND|| status == HttpStatus.SC_BAD_REQUEST|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
				throw new CoolUserFactoryException("Create group error. Exception: "+resp.getErrorContent(), status);
			if (status == HttpStatus.SC_CONFLICT)
				throw new CoolUserFactoryException("Group name already exists: "+group_name, status);
	
			return loadGroup(group_name, cmisSession);
		} catch (JsonGenerationException e) {
			throw new CoolUserFactoryException("Create group error.", e);
		} catch (JsonMappingException e) {
			throw new CoolUserFactoryException("Create group error.", e);
		} catch (IOException e) {
			throw new CoolUserFactoryException("Create group error.", e);
		}
	}
	
	@Override
	public CMISGroup loadGroup(String group_name, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/api/groups/").concat(UriUtils.encode(group_name));
        UrlBuilder url = new UrlBuilder(link);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			LOGGER.error("Group not found "+group_name+" Exception: "+resp.getErrorContent());
			throw new CoolUserFactoryException("Group not found "+group_name+" Exception: "+resp.getErrorContent(), status);
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode json = mapper.readTree(resp.getStream());
			return new CMISGroup(json.get("data").get("shortName").asText(), json.get("data").get("displayName").asText());		
		} catch (JsonProcessingException e) {
			throw new CoolUserFactoryException("Exception for group " + group_name, e);
		} catch (IOException e) {
			throw new CoolUserFactoryException("Exception for group " + group_name, e);
		}
	}

	@Override
	public List<CMISAuthority> children(String group_name, BindingSession cmisSession) throws CoolUserFactoryException {
		String link = cmisService.getBaseURL().concat("service/api/groups/").concat(UriUtils.encode(group_name)).concat("/children");
        UrlBuilder url = new UrlBuilder(link);
        url.addParameter("fullName", UriUtils.encode(group_name));
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			LOGGER.error("Group not found "+group_name+" Exception: "+resp.getErrorContent());
			throw new CoolUserFactoryException("Group not found "+group_name+" Exception: "+resp.getErrorContent(), status);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			 JsonNode json = mapper.readTree(resp.getStream());
			 return mapper.readValue(json.get("data"), mapper.getTypeFactory().constructCollectionType(List.class, CMISAuthority.class));
		} catch (JsonProcessingException e) {
			throw new CoolUserFactoryException("Exception for group " + group_name, e);
		} catch (IOException e) {
			throw new CoolUserFactoryException("Exception for group " + group_name, e);
		}
	}

}
