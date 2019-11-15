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


import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

@Path("i18n")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class I18n {

	@Value("${rest.cache.control}")
	private int cacheControl;

	private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);

	@Autowired
	private I18nService i18nService;

	@GET
	public Response i18n(@Context HttpServletRequest request,
						 @QueryParam("method") String method,
						 @QueryParam("uri") String uri,
						 @QueryParam("lang") String lang,
						 @CookieParam("__lang") String __lang) {

		LOGGER.debug(method + " " + uri);

		String actualLanguage = Optional.ofNullable(lang).orElse(__lang);
		LOGGER.info("actual language: {}", actualLanguage);
		Locale locale = I18nService.getLocale(request, actualLanguage);

		Properties labels = i18nService.getLabels(locale, uri);
		labels.put("locale", locale.getLanguage());
		LOGGER.debug("loaded " + labels.keySet().size() + " "
				+ locale.getLanguage() + " labels " + " uri " + uri);

		ResponseBuilder rb = Response.ok(labels);
		rb.cacheControl(Util.getCache(cacheControl));

		return rb.build();
	}

}
