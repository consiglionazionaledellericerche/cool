package it.cnr.cool.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.UnauthorizedException;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.QueryService;
import it.cnr.cool.util.CalendarUtil;
import it.cnr.mock.ISO8601DateFormatMethod;
import it.cnr.mock.JSONUtils;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Map;

@Path("search")
@Component
public class Search {

	private static final String BOM_EXCEL_UTF_8 = "\ufeff";
	private static final String ENCODING_UTF_8 = "UTF-8";
	private static final String FTL_JSON_PATH = "/surf/webscripts/search/query.lib.ftl";
	private static final String FTL_XLS_PATH = "/surf/webscripts/search/query.get.xls.ftl";


	private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);

    @Autowired
	private QueryService queryService;

    @Autowired
    private CMISService cmisService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@Context HttpServletRequest request) {

		ResponseBuilder rb;

        Map<String, Object> model = null;

        try {
            Session session = cmisService.getCurrentCMISSession(request);
            model = queryService.query(request, session);
        } catch(CmisUnauthorizedException e) {
            throw new UnauthorizedException("unauthorized search", e);
        }
		try {
			String json = getJson(model);
			rb = Response.ok(json);
			CacheControl cacheControl = new CacheControl();
			cacheControl.setNoCache(true);
			rb.cacheControl(cacheControl);
		} catch (TemplateException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("document/version")
	public Response documentVersion(@Context HttpServletRequest request, @QueryParam("nodeRef") String nodeRef) {

		ResponseBuilder rb;
        Session session = cmisService.getCurrentCMISSession(request);

		Map<String, Object> model = queryService.documentVersion(session, nodeRef);
		try {
			String json = getJson(model);
			rb = Response.ok(json);
			CacheControl cacheControl = new CacheControl();
			cacheControl.setNoCache(true);
			rb.cacheControl(cacheControl);
		} catch (TemplateException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}

		return rb.build();

	}

	@GET
	@Path("query.xls")
	@Produces("application/vnd.ms-excel")
	public Response queryExcel(@Context HttpServletRequest request) {

		ResponseBuilder rb;

        Session session = cmisService.getCurrentCMISSession(request);

		Map<String, Object> model = queryService.query(request, session);
		try {
			String xls = processTemplate(model, FTL_XLS_PATH);
			rb = Response.ok((BOM_EXCEL_UTF_8 + xls).getBytes(ENCODING_UTF_8));
			String fileName = "query";
			if(model.containsKey("nameBando")) {
				fileName = ((String) model.get("nameBando"));
				fileName = refactoringFileName(fileName, "");
			}

			rb.header("Content-Disposition",
					"attachment; filename=\"" + fileName.concat(".xls\""));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}
		return rb.build();
	}

	//replace caratteri che non possono comparire nel nome del file in windows
	public static String refactoringFileName(String fileName, String newString) {
		return fileName.replaceAll("[“”\"\\/:*<>| ’']", newString).replace("\\", newString);
	}

	public static String getJson(Map<String, Object> model) throws TemplateException, IOException {
		return processTemplate(model, FTL_JSON_PATH);
	}


	private static String processTemplate(Map<String, Object> model, String path) throws TemplateException,
			IOException {

		model.put("xmldate", new ISO8601DateFormatMethod());
		model.put("jsonUtils", new JSONUtils());
		model.put("calendarUtil", new CalendarUtil());

		String json = Util.processTemplate(model, path);
		LOGGER.debug(json);
		return json;

	}

	public void setQueryService(QueryService queryService) {
		this.queryService = queryService;
	}

}
