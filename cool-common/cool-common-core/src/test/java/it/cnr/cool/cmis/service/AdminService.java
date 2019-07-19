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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.cool.exception.CoolException;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class AdminService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
	@Autowired
	private CMISService cmisService;
	
	public boolean isEVENTUALsolrQueryCmis(BindingSession cmisSession) {
		return solrQueryCmisQueryConsistency(cmisSession).equalsIgnoreCase("EVENTUAL");
	}
	
	
	public String solrQueryCmisQueryConsistency (BindingSession cmisSession) {
		InputStream stream = loadMBean(cmisSession, MBeansName.SOLR.value());
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readValue(stream, JsonNode.class);
			for (Iterator<JsonNode> iterator = rootNode.iterator(); iterator.hasNext();) {
				JsonNode jsonNode = iterator.next();
				return jsonNode.get(MBeansNameAttrubite.solrQueryCmisQueryConsistency.value).asText();
			}			
			return null;
		} catch (IOException e) {
			return null;
		} 		
	}
	
	private InputStream loadMBean(BindingSession cmisSession, String mBeansName) {
		String link = cmisService.getBaseURL().concat("service/cnr/utils/jmx");
        UrlBuilder url = new UrlBuilder(link);
        url.addParameter("mBeansName", mBeansName);
		Response resp = CmisBindingsHelper.getHttpInvoker(cmisSession).invokeGET(url, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			LOGGER.error("Cannot find mbean " + mBeansName);
			throw new CoolException("Cannot find mbean " + mBeansName + resp.getErrorContent());
		}
		return resp.getStream();
	}

	public enum MBeansName {
		SOLR("Alfresco:Type=Configuration,Category=Search,id1=managed,id2=solr");

		private final String value;

		MBeansName(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

	    public static MBeansName fromValue(String v) {
	        for (MBeansName c : MBeansName.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
	
	public enum MBeansNameAttrubite {
		solrQueryCmisQueryConsistency("solr.query.cmis.queryConsistency");

		private final String value;

		MBeansNameAttrubite(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

	    public static MBeansNameAttrubite fromValue(String v) {
	        for (MBeansNameAttrubite c : MBeansNameAttrubite.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
}
