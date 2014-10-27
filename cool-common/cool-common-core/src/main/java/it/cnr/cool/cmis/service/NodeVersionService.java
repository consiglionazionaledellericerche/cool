package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.util.MimeTypes;

import java.io.OutputStream;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

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
