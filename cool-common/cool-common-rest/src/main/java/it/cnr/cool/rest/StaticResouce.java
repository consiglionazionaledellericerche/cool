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

import it.cnr.cool.exception.CoolException;
import it.cnr.cool.service.StaticService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Optional;

@Path("static")
@Component
public class StaticResouce {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticResouce.class);

	@GET
	@Path("{path:.*}")
	public Response getStaticResouce(@PathParam("path") String path,
			@Context HttpServletResponse res) throws FileNotFoundException {
		if (!Optional.ofNullable(path)
				.filter(s -> s.length() > 0)
				.filter(s -> !s.endsWith("/"))
				.filter(s -> s.contains("."))
				.isPresent() ||
				StaticResouce.class.getResource("/META-INF/" + path) == null) {
			throw new CoolException("Bad Request", HttpStatus.SC_BAD_REQUEST);
		}
		InputStream input = StaticResouce.class.getResourceAsStream("/META-INF/" + path);

		if (input != null) {

			LOGGER.debug("resource found: " + path);
			res.setStatus(Status.OK.getStatusCode());
			res.setContentType(StaticService.getMimeType(path));
			res.setHeader(StaticService.HTTP_HEADER_CACHE_CONTROL, StaticService.getCacheControl());
			try {
				ServletOutputStream output = res.getOutputStream();
				IOUtils.copy(input, output);
				output.close();
			} catch (IOException e) {
				LOGGER.error("error serving resource: " + path, e);
				throw new InternalServerErrorException(
						"error processing resourde " + path);
			}

		} else {
			LOGGER.warn("resouce not found: " + path);
			res.setStatus(Status.NOT_FOUND.getStatusCode());
		}
		return Response.status(Status.OK).build();
	}


}
