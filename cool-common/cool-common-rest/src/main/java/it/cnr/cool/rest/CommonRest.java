package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.PageService;
import it.cnr.cool.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("common")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CommonRest {

	private static final String FTL = "/surf/webscripts/js/common.get.json.ftl";

	@Autowired
	private PageService pageService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonRest.class);

	@GET
	public Response foo(@Context HttpServletRequest req) {

		ResponseBuilder rb;
		try {

			Map<String, Object> model = new HashMap<String, Object>();

			model.put("artifact_version", versionService.getVersion());

			CMISUser user = cmisService.getCMISUserFromSession(req
					.getSession(false));

			BindingSession bindingSession = cmisService
					.getCurrentBindingSession(req);

			model.put("caches", cacheService.getCaches(user, bindingSession));

			model.put("cmisDateFormat", StringUtil.CMIS_DATEFORMAT);

			Map<String, Object> context = new HashMap<String, Object>();

			// context.put("user", user);
			LOGGER.error("FIXME: user is always guest");
			Map<String, Object> emptyUser = new HashMap<String, Object>();
			emptyUser.put("isGuest", true);
			context.put("user", emptyUser);

			model.put("context", context);

			String json = Util.processTemplate(model, FTL);
			LOGGER.debug(json);
			rb = Response.ok(json);
		} catch (Exception e) {
			LOGGER.error("unable to process common json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}


}
