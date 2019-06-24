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

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.util.MimeTypes;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Service
public class NodeVersionService {

	@Autowired
	private CMISService cmisService;

	public void addAutoVersion(Document doc) {
		addAutoVersion(doc, true);
	}

	public void addAutoVersion(Document doc,
			final boolean autoVersionOnUpdateProps) {
		String link = cmisService.getBaseURL().concat(
				"service/api/metadata/node/");
		link = link.concat(doc.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString().replace(":/", ""));
		UrlBuilder url = new UrlBuilder(link);
		BindingSession cmisSession = cmisService.getAdminSession();
		Response resp = cmisService.getHttpInvoker(cmisSession).invokePOST(url,
				MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						JSONObject jsonObject = new JSONObject();
						JSONObject jsonObjectProp = new JSONObject();
						jsonObjectProp.put("cm:autoVersion", true);
						jsonObjectProp.put("cm:autoVersionOnUpdateProps",
								autoVersionOnUpdateProps);
						jsonObject.put("properties", jsonObjectProp);
						out.write(jsonObject.toString().getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Add Auto Version. Exception: "
					+ resp.getErrorContent());
	}



}
