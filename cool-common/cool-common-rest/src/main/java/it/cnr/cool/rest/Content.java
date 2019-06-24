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

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.util.UriUtils;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Path("content")
@Component
@SecurityChecked
public class Content {

	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);

	@GET
	public Response content(@Context HttpServletRequest req,
			@Context HttpServletResponse res,
			@QueryParam("path") String path,
			@QueryParam("nodeRef") String nodeRef,
			@QueryParam("deleteAfterDownload") Boolean deleteAfterDownload,
			@QueryParam("fileName") String fileName) throws URISyntaxException {
		Session cmisSession = cmisService.getCurrentCMISSession(req);

		Document document = null;
		try {
			
			if (path != null && !path.isEmpty()) {
				String cleanPath = path.replaceAll("\\?.*", "");
				LOGGER.debug("get content for path: " + cleanPath);
				document = (Document) cmisSession.getObjectByPath(cleanPath);
			} else if (nodeRef != null && !nodeRef.isEmpty()) {
				LOGGER.debug("get content for nodeRef: " + nodeRef);
				document = (Document) cmisSession.getObject(nodeRef);
			} else {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			final String id = document.getId();
			res.setContentType(document.getContentStreamMimeType());

			String attachFileName = document.getContentStreamFileName();
			if (fileName != null)
				attachFileName = fileName;
			String headerValue = "attachment";
			if (attachFileName != null && !attachFileName.isEmpty()) {
				headerValue += "; filename=\"" + attachFileName + "\"";
			}
			res.setHeader("Content-Disposition", headerValue);
			OutputStream outputStream = res.getOutputStream();
			InputStream inputStream = Optional.ofNullable(document.getContentStream()).map(ContentStream::getStream).orElseThrow(() -> new IOException("document with id " + id + " has no content"));
			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
			inputStream.close();
			outputStream.close();
		} catch(CmisUnauthorizedException e) {
			return redirect(req, nodeRef, path, "content", e);
		} catch (CmisObjectNotFoundException _ex) {
			LOGGER.warn("unable to send content {} {}", path, nodeRef, _ex);
			try {
				res.getOutputStream().print("unable to send content file not exist");
			} catch (IOException e) {
				LOGGER.error("unable to send content {} {}", path, nodeRef, _ex);
			}
			res.setStatus(HttpStatus.SC_NOT_FOUND);
		} catch (SocketException e) {
			// very frequent errors of type java.net.SocketException: Pipe rotta
			LOGGER.warn("unable to send content {} {}", path, nodeRef, e);
			res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			if (e.getCause() instanceof SocketException) {
				LOGGER.warn("unable to send content {} {}", path, nodeRef, e);
			} else {
				LOGGER.error("unable to get content for path " + path + " or nodeRef "+ nodeRef + ", URL " + req.getRequestURL() + " - " + e.getMessage(), e);				
			}
			res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

		if (document != null && deleteAfterDownload != null && deleteAfterDownload) {
			LOGGER.info("deleting " + document.getName() + " (" + document.getId() + ")");
			document.delete();
		}
		return Response.ok().build();
	}

	public Response redirect(HttpServletRequest req, String nodeRef, String path, String content, CmisUnauthorizedException e) throws URISyntaxException {
        LOGGER.debug("unauthorized to get {}", nodeRef, e);
        String redirect = "/" + Page.LOGIN_URL;
        redirect = redirect.concat("?redirect=rest/").concat(content);
        if (path != null && !path.isEmpty())
            redirect = redirect.concat("&path=" + UriUtils.encode(path));
        if (nodeRef != null && !nodeRef.isEmpty())
            redirect = redirect.concat("&nodeRef="+nodeRef);
        return Response.seeOther(new URI(getUrl(req) + redirect)).build();
    }

	static String getUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		int l = url.indexOf(req.getServletPath());
		return url.substring(0, l);
	}

}
