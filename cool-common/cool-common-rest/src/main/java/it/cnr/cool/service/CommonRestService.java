package it.cnr.cool.service;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommonRestService {

	private static final String FTL = "/surf/webscripts/js/common.get.json.ftl";

	@Autowired
	private PageService pageService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonRestService.class);

	public Response getResponse(Map<String, Object> model) {

		ResponseBuilder rb;
		try {

			String json = Util.processTemplate(model, FTL);
			LOGGER.debug(json);
			rb = Response.ok(json);
            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoCache(true);
            rb.cacheControl(cacheControl);
		} catch (Exception e) {
			LOGGER.error("unable to process common json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();
	}

	public Map<String, Object> getStringObjectMap(CMISUser user) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("artifact_version", versionService.getVersion());

		model.put("caches", Arrays.asList()); //TODO: serve solo a JCONON

		model.put("cmisDateFormat", StringUtil.CMIS_DATEFORMAT);

		Map<String, Object> context = pageService.getContext(user);

		model.put("context", context);
		return model;
	}

}
