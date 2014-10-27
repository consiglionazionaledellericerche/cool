package it.cnr.cool.rest;


import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.I18nService;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("i18n")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class I18n {

	private static final int CACHE_CONTROL = 86400;

	private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);

	@Autowired
	private I18nService i18nService;

	@GET
	public Response i18n(@Context HttpServletRequest request,
			@QueryParam("method") String method,
			@QueryParam("uri") String uri,
			@CookieParam("__lang") String __lang) {

		LOGGER.info(method + " " + uri);

		Locale locale = I18nService.getLocale(request, __lang);

		Properties labels = i18nService.getLabels(locale, uri);
		labels.put("locale", locale.getLanguage());
		LOGGER.info("loaded " + labels.keySet().size() + " "
				+ locale.getLanguage() + " labels " + " uri " + uri);

		ResponseBuilder rb = Response.ok(labels);
		rb.cacheControl(Util.getCache(CACHE_CONTROL));

		return rb.build();
	}

}
