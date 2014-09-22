package it.cnr.cool.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.PageService;
import it.cnr.cool.web.PermissionService;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("page")
@Component
@Produces(MediaType.TEXT_HTML)
public class Page {

	public static final String LOGIN_URL = "login";

	public static final String LOGOUT_URL = "logout";

	private static final String TEMPLATE = "/surf/templates/bootstrap.ftl";

	private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);

	@Autowired
	private PageService pageService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CMISService cmisService;

	@GET
	@Path("{id}")
	public Response html(@Context HttpServletRequest req,
			@PathParam("id") String id) {
		return processRequest(req, id, null);
	}

	@POST
	@Path("{id}")
	public Response post(@Context HttpServletRequest req,
			@PathParam("id") String id,
			MultivaluedMap<String, String> formParams) {
		return processRequest(req, id, formParams);

	}

	private Response processRequest(HttpServletRequest req, String id,
			MultivaluedMap<String, String> formParams) {
		ResponseBuilder rb;
		
		CoolPage page = pageService.loadPages().get(id);
		
		if (page == null) {
			rb = Response
					.status(Status.NOT_FOUND)
					.entity("page not found: " + id);
		} else if (!isAuthorized(page, id, req.getSession(false))) {
			URI uri = URI.create(req.getContextPath() + "/"
					+ LOGIN_URL + "?redirect=" + id);
			rb = Response.seeOther(uri);
		} else {

			Map<String, Object> model = pageService.getModel(req, id,
					req.getContextPath(), req.getLocale());

			try {

				if (formParams != null) {
					Map<String, String> requestParameters = new HashMap<String, String>();

					for (String key : formParams.keySet()) {
						LOGGER.debug(key.toString());
						List<String> values = formParams.get(key);

						if (values != null && values.size() > 0) {

							String value = values.get(0);

							if (values.size() > 1) {
								LOGGER.warn("more than one value for key "
										+ key
										+ ", will be used only first entry");
							}

							requestParameters.put(key, value);
						} else {
							LOGGER.debug(key + " is null");
						}

					}

					model.put("RequestParameters", requestParameters);
				}

				// process HEAD html content
				String headUrl = page.getUrl().replaceAll("\\.html\\.ftl$",
						".head.ftl");
				String headHtml = Util.processTemplate(model, headUrl);
				model.put("head", headHtml);

				// process whole HTML content
				String htmlContent = Util.processTemplate(model, TEMPLATE);
				LOGGER.debug(htmlContent);

				rb = Response.ok(htmlContent);
			} catch (TemplateException e) {
				LOGGER.error("template exception for page " + id, e);
				rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
						e.getMessage());
			} catch (IOException e) {
				LOGGER.error("IO exception for page " + id, e);
				rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(
						e.getMessage());
			}
		}

		return rb.build();
	}

	private boolean isAuthorized(CoolPage page, String id, HttpSession session) {
		
		CMISUser user = session == null ? null : cmisService
				.getCMISUserFromSession(session);
		
		boolean authorizedToViewPage;
		
		if (page.getAuthentication() == CoolPage.Authentication.ADMIN) {
			LOGGER.debug("page " + page + " requires admin authorization");
			authorizedToViewPage = user != null && user.isAdmin();
		} else if (page.getAuthentication() == CoolPage.Authentication.USER) {
			LOGGER.debug("page " + page + " requires user authorization");
			authorizedToViewPage = user != null && !user.isGuest();
		}  else {
			authorizedToViewPage = true;
		}
		
		LOGGER.debug(user + " "
				+ (authorizedToViewPage ? "authorized" : "unauthorized")
				+ " to view page, now checking RBAC");
		
		return authorizedToViewPage
				&& permissionService.isAuthorizedSession(id, "GET", session);
	}

}
