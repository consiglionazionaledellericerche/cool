package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.repository.ZoneRepository;
import it.cnr.cool.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("cache")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CacheRest {

	private static final int CACHE_CONTROL = 1800;

	private static final String FTL = "/surf/webscripts/js/cache.get.json.ftl";

	@Autowired
	private VersionService versionService;

	@Autowired
	protected FolderService folderService;

	@Autowired
	private ZoneRepository zoneRepository;

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
			model.put("isProduction", versionService.isProduction());
			model.put("dataDictionary", folderService.getDataDictionaryId());


			LOGGER.debug("adding zones to public caches");
			Pair<String, Object> zones = new Pair<String, Object>("zones", zoneRepository.get());
			List<Pair<String, Object>> publicCaches = Arrays.asList(zones);

			model.put("publicCaches", publicCaches);

			String json = Util.processTemplate(model, FTL);
			rb = Response.ok(json);
			rb.cacheControl(Util.getCache(CACHE_CONTROL));
		} catch (Exception e) {
			LOGGER.error("unable to process cache json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}

}
