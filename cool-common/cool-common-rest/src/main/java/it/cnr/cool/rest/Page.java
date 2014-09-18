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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

	private static final String TEMPLATE = "/surf/templates/bootstrap.ftl";

	private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);

	@Autowired
	private PageService pageService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CMISService cmisService;

	@GET
	public Response html(@Context HttpServletRequest req,
			@QueryParam("id") String id) {

		ResponseBuilder rb;
		
		CoolPage page = pageService.loadPages().get(id);
		
		if (page == null) {
			rb = Response
					.status(Status.NOT_FOUND)
					.entity("page not found: " + id);
		} else if (!isAuthorized(page, id, req.getSession(false))) {
			URI uri = URI.create(req.getContextPath() + "/"
					+ LOGIN_URL);
			rb = Response.seeOther(uri);
		} else {

			Map<String, Object> model = pageService.getModel(id,
					req.getContextPath(), req.getLocale());

			try {
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
