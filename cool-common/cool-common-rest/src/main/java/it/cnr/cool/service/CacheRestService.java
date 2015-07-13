package it.cnr.cool.service;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheRestService {

	private static final int CACHE_CONTROL = 1800;

	private static final String FTL = "/surf/webscripts/js/cache.get.json.ftl";

	@Autowired
	private VersionService versionService;

	@Autowired
	protected FolderService folderService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CacheRestService.class);

	public Response getResponse(Map<String, Object> model) {

		ResponseBuilder rb;
		try {
			String json = Util.processTemplate(model, FTL);
			rb = Response.ok(json);
			rb.cacheControl(Util.getCache(CACHE_CONTROL));
		} catch (Exception e) {
			LOGGER.error("unable to process cache json", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();
	}

	public Map<String, Object> getMap(String contextPath) {
		Map<String, Object> model = new HashMap<String, Object>();

		HashMap<String, Object> url = new HashMap<String, Object>();
		url.put("context", contextPath);
		model.put("url", url);
		model.put("isProduction", versionService.isProduction());
		model.put("dataDictionary", folderService.getDataDictionaryId());

		return model;
	}

}
