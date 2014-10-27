package it.cnr.cool.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Path("static")
@Component
public class StaticResouce {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticResouce.class);

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";

	private static final int CACHE_DAYS = 3; // Cache expiration

	@GET
	@Path("{path:.*}")
	public Response getStaticResouce(@PathParam("path") String path,
			@Context HttpServletResponse res) {

		InputStream input = StaticResouce.class.getResourceAsStream("/META-INF/"
				+ path);

		if (input != null) {

			LOGGER.debug("resource found: " + path);
			res.setStatus(Status.OK.getStatusCode());
			res.setContentType(getMimeType(path));
			res.setHeader(HTTP_HEADER_CACHE_CONTROL, getCacheControl());
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

	private String getCacheControl() {
		int n = CACHE_DAYS * 24 * 60 * 60;
		return String.format("max-age=%d, public", n);
	}


	private String getMimeType(String path) {
		String mimeType;

		if (path.indexOf(".css") > 0) {
			mimeType = "text/css";
		} else if (path.indexOf(".json") > 0) {
			mimeType = "application/json";
		} else if (path.indexOf(".js") > 0) {
			mimeType = "application/javascript";
		} else if (path.indexOf(".png") > 0) {
			mimeType = "image/png";
		} else if (path.indexOf(".gif") > 0) {
			mimeType = "image/gif";
		} else if (path.indexOf(".handlebars") > 0) {
			mimeType = "text/x-handlebars-template";
		} else if (path.indexOf(".ico") > 0) {
			mimeType = "image/x-icon";
		} else if (path.indexOf(".woff") > 0) {
			mimeType = "application/font-woff";
		} else if (path.indexOf(".ttf") > 0) {
			mimeType = "font/ttf";
		} else if (path.indexOf(".otf") > 0) {
			mimeType = "font/opentype";
		} else if (path.indexOf(".html") > 0) {
			mimeType = "text/html";
		} else {
			mimeType = DEFAULT_MIME_TYPE;
			LOGGER.warn("mimetype not found for path: " + path
					+ ", using default");
		}
		return mimeType;
	}

}
