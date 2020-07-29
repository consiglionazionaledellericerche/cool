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

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.service.PageService;
import it.cnr.cool.util.GroupsUtils;
import it.cnr.cool.web.PermissionService;
import it.cnr.mock.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
	public Response html(@Context HttpServletRequest req, @Context HttpServletResponse res,
			@PathParam("id") String id, @CookieParam("__lang") String cookieLang, @QueryParam("lang") String reqLang) {
		String overrideLang = pageService.getOverrideLang();
		String language = Optional.ofNullable(overrideLang).orElse(reqLang);
		return processRequest(req, res, id, null, cookieLang, language);
	}

	@POST
	@Path("{id}")
	public Response post(@Context HttpServletRequest req, @PathParam("id") String id,
						 @RequestBody MultivaluedMap<String, String> formParams, @CookieParam("__lang") String cookieLang) {
		Map formParamz = new HashMap<>();
		formParamz.putAll(
				req.getParameterMap()
						.entrySet()
						.stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> Arrays.asList(entry.getValue())
						))
		);
		if (formParams != null && !formParams.isEmpty())
			formParamz.putAll(RequestUtils.extractFormParams(formParams));

		return processRequest(req, null, id, formParamz, cookieLang, null);
	}

	private String i18nCookie(HttpServletResponse res, String lang, String reqLang, boolean secure) {
		if (reqLang != null && reqLang.length() > 0 && res != null) {
			ResponseCookie cookie = ResponseCookie.from("__lang", reqLang)
					.path("/")
					.maxAge(-1)
					.secure(secure)
					.sameSite("strict")
					.build();
			res.addHeader("Set-Cookie", cookie.toString());
            return reqLang;
		}
		return lang;
	}

	private Response processRequest(HttpServletRequest req, HttpServletResponse res, String id,
									Map<String, List<String>> formParams, String cookieLang, String reqLang) {

		ResponseBuilder rb;
		String lang = i18nCookie(res, cookieLang, reqLang,
				Optional.ofNullable(req.getProtocol())
					.map(s -> !s.equals("HTTP/1.1"))
					.orElse(Boolean.TRUE)
		);
		CoolPage page = pageService.loadPages().get(id);
		CMISUser user = cmisService.getCMISUserFromSession(req);
		if (page == null) {
			rb = Response
					.status(Status.NOT_FOUND)
					.entity("page not found: " + id);
		} else if (!isAuthorized(page, id, user, formParams != null)) {
			String baseURI = req.getContextPath() + "/" + LOGIN_URL;
			final Optional<String> redirect = Optional.ofNullable(SecurityRest.getRedirect(req, id));
			if (redirect.isPresent()) {
				baseURI = baseURI.concat(redirect.get());
			}
			URI uri = URI.create(baseURI);
			rb = Response.seeOther(uri);
		} else {

			Map<String, Object> model = pageService.getModel(req, id,
					req.getContextPath(), I18nService.getLocale(req, lang), user);

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
							if (value.matches(SecurityRest.REGEX)) {
								requestParameters.put(key, value);
							}
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

	private boolean isAuthorized(CoolPage page, String id, CMISUser user, boolean isPost) {


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

        boolean rbacAuthorized = permissionService.isAuthorized(id, isPost ? "POST" : "GET", user.getId(), GroupsUtils.getGroups(user));

        if (rbacAuthorized) {
            LOGGER.debug("RBAC: " + user + " authorized to access page " + id);
        } else {
            LOGGER.warn("RBAC: " + user + " unauthorized to access page " + id);
        }

        return authorizedToViewPage
				&& rbacAuthorized;
	}

}
