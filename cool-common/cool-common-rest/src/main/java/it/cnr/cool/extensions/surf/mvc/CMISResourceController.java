package it.cnr.cool.extensions.surf.mvc;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.webscripts.Store;
import org.springframework.extensions.webscripts.servlet.mvc.ResourceController;
import org.springframework.util.FileCopyUtils;

public class CMISResourceController extends ResourceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CMISResourceController.class);
    private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_HEADER_LAST_MODIFIED = "Last-Modified";
    private static final String HTTP_HEADER_ETAG = "ETag";
    private static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";

    private static final Map<String, String> defaultMimeTypes = new HashMap<String, String>();
    {
        defaultMimeTypes.put(".css", "text/css");
        defaultMimeTypes.put(".gif", "image/gif");
        defaultMimeTypes.put(".ico", "image/vnd.microsoft.icon");
        defaultMimeTypes.put(".jpeg", "image/jpeg");
        defaultMimeTypes.put(".jpg", "image/jpeg");
        defaultMimeTypes.put(".js", "text/javascript");
        defaultMimeTypes.put(".png", "image/png");
    }
    // the web framework service registry
    private WebFrameworkServiceRegistry webFrameworkServiceRegistry;
    private Store remoteStore;
    /**
     * Sets the service registry.
     *
     * @param webFrameworkServiceRegistry the new service registry
     */
    public void setServiceRegistry(WebFrameworkServiceRegistry webFrameworkServiceRegistry)
    {
        this.webFrameworkServiceRegistry = webFrameworkServiceRegistry;
    }

    /**
     * Gets the service registry.
     *
     * @return the service registry
     */
    public WebFrameworkServiceRegistry getServiceRegistry()
    {
        return this.webFrameworkServiceRegistry;
    }

	public void setRemoteStore(Store remoteStore)
	{
		this.remoteStore = remoteStore;
	}

    @Override
	public boolean dispatchResource(String path, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
        {
            boolean resolved = retrieveRemoteResource(response, path);

            if (path.lastIndexOf('.') == -1)
			LOGGER.warn("Path doesn't have extension: " + path);
            if (!resolved && path.lastIndexOf('.') != -1)
            {
                resolved = super.dispatchResource(path, request, response);
            }
            return resolved;
        }


    @Override
	public void commitResponse(
            final String path, final Resource resource, final HttpServletRequest request, final HttpServletResponse response)
        throws IOException, ServletException
    {

        // determine properties of the resource being served back
        final URLConnection resourceConn = resource.getURL().openConnection();
		applyHeaders(path, response, resourceConn.getContentLength(), resourceConn.getLastModified(),
				request.getDateHeader("If-Modified-Since"));
        // stream back to response
        RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/" + path);
        rd.include(request, response);
    }

    private void applyHeaders(
            final String path, final HttpServletResponse response, final long contentLength,
            final long lastModified, final long modifiedSince)
    {
        // determine mimetype
        String mimetype = getServletContext().getMimeType(path);
        if (mimetype == null)
        {
            String extension = path.substring(path.lastIndexOf('.'));
            mimetype = defaultMimeTypes.get(extension.toLowerCase());
        }
		LOGGER.debug("Modified since: " + modifiedSince);
        // set response headers
        response.setContentType(mimetype);
        response.setHeader(HTTP_HEADER_CONTENT_LENGTH, Long.toString(contentLength));
//TODO
//        if (lastModified > modifiedSince)
//        	response.setStatus(HttpStatus.SC_NOT_MODIFIED);

        if (lastModified != 0)
        {
            response.setHeader(HTTP_HEADER_ETAG, '"' + Long.toString(lastModified) + '"');
            response.setDateHeader(HTTP_HEADER_LAST_MODIFIED, lastModified);
        }

        response.setHeader(HTTP_HEADER_CACHE_CONTROL, "max-age=259200, public");

    }
	private boolean retrieveRemoteResource(HttpServletResponse response, String path) {
        boolean resolved = false;

        try {
			resolved = remoteStore.hasDocument(path);
			if (resolved){
				FileCopyUtils.copy(remoteStore.getDocument(path), response.getOutputStream());
			}
		} catch (IOException e) {
			return false;
		}
		return resolved;
	}


}
