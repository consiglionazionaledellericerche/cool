package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.PageService;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("cache")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CacheRest {

	private static final String FTL = "/surf/webscripts/js/cache.get.json.ftl";

	@Autowired
	private CacheService cacheService;

	@Autowired
	private PageService pageService;

	@Autowired
	private VersionService versionService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CacheRest.class);

	@GET
	public Response foo(@Context HttpServletRequest req) {

		ResponseBuilder rb;
		try {
			// TODO: forse non serve tutto getModel...
			Map<String, Object> model = pageService.getModel(null,
					req.getContextPath(), req.getLocale());
			model.put("publicCaches", cacheService.getPublicCaches());
			model.put("isProduction", versionService.isProduction());

			String json = Util.processTemplate(model, FTL);
			rb = Response.ok(json);
		} catch (Exception e) {
			LOGGER.error("unable to process cache json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}


}
