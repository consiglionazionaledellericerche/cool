package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.SecurityChecked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
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
	public void content(@Context HttpServletRequest req,
			@Context HttpServletResponse res, @QueryParam("path") String path,
			@QueryParam("nodeRef") String nodeRef,
			@QueryParam("deleteAfterDownload") Boolean deleteAfterDownload) {

		HttpSession session = req.getSession(false);
		Session cmisSession = cmisService.getCurrentCMISSession(session);

		Document document;

		if (path != null && !path.isEmpty()) {
			String cleanPath = path.replaceAll("\\?.*", "");
			LOGGER.debug("get content for path: " + cleanPath);
			document = (Document) cmisSession.getObjectByPath(cleanPath);
		} else {
			LOGGER.debug("get content for nodeRef: " + nodeRef);
			document = (Document) cmisSession.getObject(nodeRef);
		}

		res.setContentType(document.getContentStreamMimeType());

		try {
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
		} catch (IOException e) {
			LOGGER.error("unable to get content for path " + path, e);
			res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

		if (deleteAfterDownload != null && deleteAfterDownload) {
			LOGGER.info("deleting " + document.getName() + " (" + document.getId() + ")");
			document.delete();
		}

	}

}
