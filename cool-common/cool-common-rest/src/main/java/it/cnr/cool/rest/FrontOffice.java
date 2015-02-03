package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.service.frontOffice.FrontOfficeService;
import it.cnr.cool.service.frontOffice.TypeDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Path("frontOffice")
@Component
@Produces(MediaType.APPLICATION_JSON)
@SecurityChecked
public class FrontOffice {

	private final static String USER_AGENT = "user-agent";
	@Autowired
	private FrontOfficeService frontOfficeService;
	@Autowired
	private CMISService cmisService;

	@GET
	@Path("faq")
	public Response getFaq(@Context HttpServletRequest req,
			@QueryParam("editor") @DefaultValue("false") boolean editor,
			@QueryParam("typeBando") String typeBando,
			@QueryParam("after") String after,
			@QueryParam("before") String before,
			@QueryParam("filterAnswer") String filterAnswer) {
		ResponseBuilder builder = Response.ok(frontOfficeService.getFaq(
				cmisService.getCurrentCMISSession(req.getSession(false)),
				after, before, typeBando, editor, filterAnswer));
		// setto Faq e Notice cachabili per un'ora, le altre risorse devono
		// avere un timestamp nella URL
		builder.cacheControl(Util.getCache(3600));
		return builder.build();
	}

	@GET
	@Path("notice")
	public Response getNotice(@Context HttpServletRequest req,
			@QueryParam("editor") @DefaultValue("false") boolean editor,
			@QueryParam("typeBando") String typeBando,
			@QueryParam("after") String after,
			@QueryParam("before") String before) {
		ResponseBuilder builder = Response.ok(frontOfficeService.getNotice(
				cmisService.getCurrentCMISSession(req.getSession(false)),
				cmisService.createAdminSession(),after, before, editor, typeBando));
		builder.cacheControl(Util.getCache(3600));
		return builder.build();
	}

	@GET
	@Path("log")
	public Response getLog(@Context HttpServletRequest req,
			@QueryParam("after") String after,
			@QueryParam("before") String before,
			@QueryParam("typeLog") String typeLog,
			@QueryParam("application") String application,
			@QueryParam("userLog") String userLog) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("docs", frontOfficeService.getLog(
				cmisService.getCurrentCMISSession(req.getSession(false)),
				after, before, application, typeLog, userLog));
		return Response.ok(model).build();
	}


	@POST
	@Path("log")
	public Map<String, Object> postLog(@Context HttpServletRequest req,
			@FormParam("stackTrace") String stackTrace,
			@HeaderParam(USER_AGENT) String userAgent) {
		HashMap<String, String> requestHeader = new HashMap<String, String>();
		Enumeration<String> iteratorHeader = req.getHeaderNames();
		while (iteratorHeader.hasMoreElements()) {
			String key = iteratorHeader.nextElement();
			requestHeader.put(key, req.getHeader(key));
		}
		HashMap<String, String> requestParameter = new HashMap<String, String>();
        for (String key : (Iterable<String>) req.getParameterMap().keySet()) {
            requestParameter.put(key, req.getParameter(key));
        }
		return frontOfficeService.post(req.getRemoteAddr(),
				userAgent, requestHeader, requestParameter,
				TypeDocument.fromValue("log"), stackTrace);
	}

	@POST
	public Map<String, Object> post(@Context HttpServletRequest req,
			@FormParam("stackTrace") String stackTrace,
			@FormParam("type_document") String type_document) {
		return frontOfficeService.post(req.getRemoteAddr(),
				null, null, null, TypeDocument.fromValue(type_document),
				stackTrace);
	}

	@DELETE
	@Path("{store_type}/{store_id}/{id}")
	public Map<String, Object> deleteSingleNode(
			@Context HttpServletRequest req,
			@PathParam("store_type") String store_type,
			@PathParam("store_id") String store_id, @PathParam("id") String id) {
		Map<String, Object> model = new HashMap<String, Object>();
		frontOfficeService.deleteSingleNode(
				cmisService.getCurrentCMISSession(req.getSession(false)),
				store_type + "://" + store_id + "/" + id);
		model.put("totalNumItems", 1);
		return model;
	}
}