package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;

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
	private VersionService versionService;

	@Autowired
	protected FolderService folderService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CacheRest.class);

	@GET
	public Response foo(@Context HttpServletRequest req) {

		ResponseBuilder rb;
		try {
			Map<String, Object> model = new HashMap<String, Object>();

			HashMap<String, Object> url = new HashMap<String, Object>();
			url.put("context", req.getContextPath());
			model.put("url", url);
			model.put("publicCaches", cacheService.getPublicCaches());
			model.put("isProduction", versionService.isProduction());
			model.put("dataDictionary", folderService.getDataDictionaryId());

			String json = Util.processTemplate(model, FTL);
			rb = Response.ok(json);
		} catch (Exception e) {
			LOGGER.error("unable to process cache json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}

}
