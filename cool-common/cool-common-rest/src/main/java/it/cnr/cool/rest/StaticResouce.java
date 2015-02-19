package it.cnr.cool.rest;

import it.cnr.cool.service.StaticService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;

@Path("static")
@Component
public class StaticResouce {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticResouce.class);

	@GET
	@Path("{path:.*}")
	public Response getStaticResouce(@PathParam("path") String path,
			@Context HttpServletResponse res) {

		InputStream input = StaticResouce.class.getResourceAsStream("/META-INF/"
				+ path);

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
