/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.rest;

import com.google.gson.GsonBuilder;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.NodeService;
import it.cnr.cool.util.CMISUtil;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.mock.ISO8601DateFormatMethod;
import it.cnr.mock.JSONUtils;
import it.cnr.mock.RequestUtils;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node resource
 */
@Path("node")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class Node {

	private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
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
			LOGGER.error("max size exceeded", _ex);
			String readableFileSize = readableFileSize(request.getContentLength());
			String maxFileSize = readableFileSize(_ex.getMaxUploadSize());
			String message = "Il file ( " + readableFileSize + ") supera la dimensione massima consentita (" + maxFileSize + ")";
			throw new ClientMessageException(message);
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
			LOGGER.error("Max Upload Size Exceeded", _ex);
			Map<String, Object> model = new HashMap<String, Object>();
			String readableFileSize = readableFileSize(request.getContentLength());
			String message = "Il file ( " + readableFileSize + ") supera la dimensione massima consentita (" + readableFileSize(_ex.getMaxUploadSize()) + ")";
			model.put("message", message);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);			
		} catch(CmisUnauthorizedException| CmisPermissionDeniedException _ex) {
			LOGGER.error("unauthorized", _ex);
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
		} catch(CmisUnauthorizedException|CmisPermissionDeniedException _ex) {
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
			builder = Response.ok(CMISUtil.convertToProperties(cmisObject));
			Boolean cachable = Boolean.valueOf(req.getParameter("cachable"));
			if (cachable) {
				CacheControl cache = Util.getCache(MAX_AGE);
				builder.cacheControl(cache);
			}
		} catch(CmisUnauthorizedException|CmisPermissionDeniedException _ex) {
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
			builder = Response.ok(CMISUtil.convertToProperties(cmisObject));
		} catch(CmisUnauthorizedException|CmisPermissionDeniedException _ex) {
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
		double number = size / Math.pow(1000, digitGroups);
		return new DecimalFormat("#,##0.#").format(number) + " " + units[digitGroups];
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

}
