package it.cnr.cool.rest;

import it.cnr.bulkinfo.cool.BulkInfoCool;
import it.cnr.bulkinfo.exception.BulkInfoException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.BulkInfoCoolSerializer;
import it.cnr.cool.service.BulkInfoCoolService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Map;

@Path("bulkInfo")
@Component
@Produces(MediaType.APPLICATION_JSON)

public class BulkInfoRest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoRest.class);

	private static final String DEFAULT_ERROR = "errore generico";

	@Autowired
	private BulkInfoCoolService bulkInfoCoolService;
	@Autowired
	private BulkInfoCoolSerializer bulkInfoCoolSerializer;
	@Autowired
	private CMISService cmisService;
	@Autowired
	private VersionService versionService;

	@GET
	@Path("view/{type}/{kind}/{name}")
	public Response getView(@Context HttpServletRequest req,
			@PathParam("type") String type,
			@PathParam("kind") String kind,
			@PathParam("name") String name,
			@QueryParam("cmis:objectId") String objectId) {

		ResponseBuilder builder;
		Map<String, Object> model;

		Session session = cmisService.getCurrentCMISSession(req);

		try {
			model = bulkInfoCoolService
					.getView(session, type, kind, name, objectId);

			// String json = Util.processTemplate(model, TEMPLATE_PATH_VIEW);
			String json = bulkInfoCoolSerializer.serialize(model).toString();

			builder = Response.ok(json);

			if(objectId == null || objectId.isEmpty()) {
			  builder.cacheControl(Util.getCache(1800));
			}

		} catch (BulkInfoException e) {
			LOGGER.error("", e);
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		} catch (Exception e) {
			LOGGER.error("", e);
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		}

		return builder.build();
	}

	@GET
	@Path("structure/{type}")
	public Response getBulkinfoStructure(@Context HttpServletRequest req,
			@PathParam("type") String type,
			@QueryParam("prefix") String prefix) {

		ResponseBuilder builder;
		String json = "";

		try {
			BulkInfoCool bi = bulkInfoCoolService.find(type);

			json = "[";
			boolean coma = false;
			for(String form : bi.getForms().keySet()) {
				if(form.contains(prefix)) {
					json += (coma ? "," : "") + "\""+ form +"\"";
					coma = true;
				}
			}
			json += "]";

			builder = Response.ok(json);
	    builder.cacheControl(Util.getCache(1800));

		} catch (Exception e) {
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		}

		return builder.build();
	}

}

