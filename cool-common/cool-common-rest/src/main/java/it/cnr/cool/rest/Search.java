package it.cnr.cool.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.QueryService;
import it.cnr.cool.util.CalendarUtil;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.ISO8601DateFormatMethod;
import org.springframework.extensions.webscripts.json.JSONUtils;
import org.springframework.stereotype.Component;

@Path("search")
@Component
public class Search {

	private static final String BOM_EXCEL_UTF_8 = "\ufeff";
	private static final String ENCODING_UTF_8 = "UTF-8";
	private static final String FTL_JSON_PATH = "/surf/webscripts/search/query.lib.ftl";
	private static final String FTL_XLS_PATH = "/surf/webscripts/search/query.get.xls.ftl";


	private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);

	private QueryService queryService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@Context HttpServletRequest request) {

		ResponseBuilder rb;

		Map<String, Object> model = queryService.query(request);
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
		Map<String, Object> model = queryService.query(request);
		try {
			String xls = processTemplate(model, FTL_XLS_PATH);
			rb = Response.ok((BOM_EXCEL_UTF_8 + xls).getBytes(ENCODING_UTF_8));
			String fileName = "query";
			if(model.containsKey("nameBando"))
			 fileName = (String) model.get("nameBando");
			
			rb.header("Content-Disposition",
					"attachment; filename=" + fileName + ".xls");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}
		return rb.build();
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
