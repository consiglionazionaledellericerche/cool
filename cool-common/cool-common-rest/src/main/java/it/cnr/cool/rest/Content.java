package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.SecurityChecked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("content")
@Component
@SecurityChecked
public class Content {

	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);

	@GET
	public Response content(@Context HttpServletRequest req,
			@Context HttpServletResponse res, @QueryParam("path") String path,
			@QueryParam("nodeRef") String nodeRef,
			@QueryParam("deleteAfterDownload") Boolean deleteAfterDownload) throws URISyntaxException {
		Session cmisSession = cmisService.getCurrentCMISSession(req);

		Document document = null;
		try {

			if (path != null && !path.isEmpty()) {
				String cleanPath = path.replaceAll("\\?.*", "");
				LOGGER.debug("get content for path: " + cleanPath);
				document = (Document) cmisSession.getObjectByPath(cleanPath);
			} else {
				LOGGER.debug("get content for nodeRef: " + nodeRef);
				document = (Document) cmisSession.getObject(nodeRef);
			}

			res.setContentType(document.getContentStreamMimeType());

			String attachFileName = document.getContentStreamFileName();
			String headerValue = "attachment";
			if (attachFileName != null && !attachFileName.isEmpty()) {
				headerValue += "; filename=\"" + attachFileName + "\"";
			}
			res.setHeader("Content-Disposition", headerValue);
			OutputStream outputStream = res.getOutputStream();
			InputStream inputStream = document.getContentStream().getStream();

			IOUtils.copy(inputStream, outputStream);



			// TODO: caching ??

			outputStream.flush();

			inputStream.close();
			outputStream.close();
		} catch(CmisUnauthorizedException e) {
            LOGGER.debug("unauthorized to get " + nodeRef);
            String redirect = "/" + Page.LOGIN_URL;
            redirect = redirect.concat("?redirect=rest/content");
			if (path != null && !path.isEmpty())
				redirect = redirect.concat("&path="+path);
			if (nodeRef != null && !nodeRef.isEmpty())
				redirect = redirect.concat("&nodeRef="+nodeRef);
			return Response.seeOther(new URI(getUrl(req) + redirect)).build();
		} catch (IOException e) {
			LOGGER.error("unable to get content for path " + path, e);
			res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

		if (document != null && deleteAfterDownload != null && deleteAfterDownload) {
			LOGGER.info("deleting " + document.getName() + " (" + document.getId() + ")");
			document.delete();
		}
		return Response.ok().build();
	}

	static String getUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		int l = url.indexOf(req.getServletPath());
		return url.substring(0, l);
	}

}
