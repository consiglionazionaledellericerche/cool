package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.NodeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.ISO8601DateFormatMethod;
import org.springframework.extensions.webscripts.json.JSONUtils;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;

@Path("node")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class Node {

	private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
	private static final String PATH_FTL = "/surf/webscripts/search/cmisObject.get.json.ftl";
	private static final String PATH_POST_FTL = "/surf/webscripts/node/cmisObject.post.json.ftl";
	private static final int MAX_AGE = 60 * 60 * 1; // un'ora

	@Autowired
	private CMISService cmisService;

	@Autowired
	@Qualifier("cmisDefaultOperationContext")
	private OperationContext cmisDefaultOperationContext;

	@Autowired
	private NodeService nodeService;
	@Autowired
	private NodeMetadataService nodeMetedataService;

	@POST
	@Produces(MediaType.TEXT_HTML)
	public Response postHTML(@Context HttpServletRequest request) {

		List<CmisObject> l = nodeService.manageRequest(request, true, false);
		ResponseBuilder rb;

		try {
			String json = serializeJson(l);
			String html = "<textarea>" + json + "</textarea>";
			rb = Response.ok(html);
		} catch (Exception e) {
			LOGGER.error("error processing request", e);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		}
		return rb.build();
	}

	@POST
	public Response post(@Context HttpServletRequest request) {

		ResponseBuilder rb;
		try {
			List<CmisObject> l = nodeService
					.manageRequest(request, true, false);

			String json = serializeJson(l);
			rb = Response.ok(json);
		} catch (Exception e) {
			LOGGER.error("Exception: ", e.getMessage());
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		}
		return rb.build();
	}

	@DELETE
	public Response delete(@Context HttpServletRequest request) {

		List<CmisObject> l = nodeService.manageRequest(request, false, true);
		ResponseBuilder rb;

		try {
			String json = serializeJson(l);
			rb = Response.ok(json);
		} catch (Exception e) {
			LOGGER.error("Exception: ", e.getMessage());
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		}
		return rb.build();
	}


	@GET
	public Response get(@Context HttpServletRequest req) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("get CmisObject");
		}
		ResponseBuilder builder = null;
		Map<String, Object> model = new HashMap<String, Object>();

		String nodeRef = req.getParameter("nodeRef");
		LOGGER.debug("nodeRef: " + nodeRef);

		String fields = req.getParameter("fields");
		LOGGER.debug("fields: " + fields);

		OperationContext operationContext = new OperationContextImpl(
				cmisDefaultOperationContext);
		if (fields != null && fields.length() > 0) {
			operationContext.setFilterString(fields);
		}
		Session cmisSession = cmisService.getCurrentCMISSession(req
				.getSession(false));
		try {
			CmisObject cmisObject = cmisSession.getObject(nodeRef, operationContext);
			model = buildModel(cmisObject);
			String content = Util.processTemplate(model, PATH_FTL);
			LOGGER.debug(content);
			builder = Response.ok(content);
			Boolean cachable = Boolean.valueOf(req.getParameter("cachable"));
			if (cachable) {
				CacheControl cache = Util.getCache(MAX_AGE);
				builder.cacheControl(cache);
			}

		} catch (Exception e) {
			model.put("message", e.getMessage());
			builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
					model);
		}
		return builder.build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("metadata")
	public Response metadata(@Context HttpServletRequest req,
			MultivaluedMap<String, String> formParams) {
		ResponseBuilder builder = null;
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			Session session = cmisService.getCurrentCMISSession(req
					.getSession(false));

			CmisObject cmisObject = nodeMetedataService.updateObjectProperties(
					extractFormParams(formParams), session, req);

			model = buildModel(cmisObject);
			String content = Util.processTemplate(model, PATH_POST_FTL);
			LOGGER.debug(content);
			builder = Response.ok(content);
		} catch (Exception e) {
			LOGGER.error("Exception: ", e);
			model.put("message", e.getMessage());
			builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
					model);
		}
		return builder.build();
	}

	private Map<String, Object> extractFormParams(
			MultivaluedMap<String, String> formParams) {
		Map<String, Object> properties = new HashMap<String, Object>();
		for (Entry<String, List<String>> appo : formParams.entrySet()) {
			List<String> value = appo.getValue();
			String key = appo.getKey();
			if (value.size() == 1) {
				properties.put(key, value.get(0));
			} else {
				properties.put(key, value.toArray(new String[value.size()]));
			}
		}
		return properties;
	}


	private String serializeJson(List<CmisObject> l) {

		Map<String, String> hml = new HashMap<String, String>();

		for (CmisObject o : l) {
			hml.put(o.getId(), null);
		}

		LOGGER.debug(hml.toString());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("attachments", hml);

		String json = new GsonBuilder().serializeNulls().create().toJson(model);
		LOGGER.debug(json);
		return json;
	}

	private Map<String, Object> buildModel(CmisObject object) {

		String nodeRef = object.getId();
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("gest", true);
		args.put("nodeRef", nodeRef);
		args.put("ajax", true);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("cmisObject", object);
		model.put("args", args);
		model.put("jsonUtils", new JSONUtils());
		model.put("xmldate", new ISO8601DateFormatMethod());

		return model;
	}

}
