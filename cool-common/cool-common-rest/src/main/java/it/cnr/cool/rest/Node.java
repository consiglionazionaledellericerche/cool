package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.NodeService;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.mock.ISO8601DateFormatMethod;
import it.cnr.mock.JSONUtils;
import it.cnr.mock.RequestUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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
		ResponseBuilder rb;
		try {
			List<CmisObject> l = nodeService.manageRequest(request, true, false);
			String json = serializeJson(l);
			String html = "<textarea>" + json + "</textarea>";
			rb = Response.ok(html);
		} catch(MaxUploadSizeExceededException _ex) {		
			throw new ClientMessageException("Il file ( " + readableFileSize(request.getContentLength()) + ") supera la dimensione massima consentita (" + readableFileSize(_ex.getMaxUploadSize()) + ")");
		} catch (Exception e) {
			if (e instanceof ClientMessageException)
				throw e;
			else {
				LOGGER.error("error processing request", e);
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("message", e.getMessage());
				rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);				
			}
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
		} catch(MaxUploadSizeExceededException _ex) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", "Il file ( " + readableFileSize(request.getContentLength()) + ") supera la dimensione massima consentita (" + readableFileSize(_ex.getMaxUploadSize()) + ")");			
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);			
		} catch(CmisUnauthorizedException _ex) {
			rb = Response.status(Status.UNAUTHORIZED);			
		} catch (Exception e) {
			LOGGER.error("Exception: " + e.getMessage(), e);
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
		} catch(CmisUnauthorizedException _ex) {
			rb = Response.status(Status.UNAUTHORIZED);			
		} catch (Exception e) {
			LOGGER.error("Exception: " + e.getMessage(), e);
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
		Session cmisSession = cmisService.getCurrentCMISSession(req);
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
		} catch(CmisUnauthorizedException _ex) {
			builder = Response.status(Status.UNAUTHORIZED);
		} catch (Exception e) {
			model.put("message", e.getMessage());
			builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
					model);
		}
		return builder.build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("metadata")
	public Response metadata(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) {
		Map formParamz = new HashMap<>();
		formParamz.putAll(req.getParameterMap());
		if (formParams != null && !formParams.isEmpty())
			formParamz.putAll(RequestUtils.extractFormParams(formParams));
		ResponseBuilder builder = null;
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			Session session = cmisService.getCurrentCMISSession(req);
			CmisObject cmisObject = nodeMetedataService.updateObjectProperties(
					formParamz, session, req);

			model = buildModel(cmisObject);
			String content = Util.processTemplate(model, PATH_POST_FTL);
			LOGGER.debug(content);
			builder = Response.ok(content);
		} catch(CmisUnauthorizedException _ex) {
			builder = Response.status(Status.UNAUTHORIZED);
		} catch (Exception e) {
			if (!(e.getCause() instanceof CmisContentAlreadyExistsException))
				LOGGER.error("Exception: " + e.getMessage(), e);
			model.put("message", e.getMessage());
			builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
					model);
		}
		return builder.build();
	}

	private String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
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
