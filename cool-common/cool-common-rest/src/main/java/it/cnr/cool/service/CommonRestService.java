package it.cnr.cool.service;

import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonRestService {

	@Autowired
	private PageService pageService;

	@Autowired
	private VersionService versionService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonRestService.class);

	public Response getResponse(Map<String, Object> model) {
		ResponseBuilder rb;
		try {
			rb = Response.ok(model);
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
		model.put("now",  StringUtil.CMIS_DATEFORMAT.format(new Date()));
		if (user == null) {
			user = new CMISUser("guest");
			Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
			capabilities.put(CMISUser.CAPABILITY_GUEST, true);
			user.setCapabilities(capabilities);
		}
		model.put("User", user);
		model.put("isAdmin", user.isAdmin());
		model.put("isGuest", user.isGuest());		
		return model;
	}

}
