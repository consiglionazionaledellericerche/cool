package it.cnr.cool.security;


import it.cnr.cool.web.PermissionService;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SecurityChecked
public class SecurityCheckInterceptor implements ContainerRequestFilter{

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityCheckInterceptor.class);
	@Context
	HttpServletRequest request;

	@Autowired
	private PermissionService permission;
	@Context
	UriInfo uriInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		HttpSession session = request.getSession(false);
		Object obj = uriInfo.getMatchedResources().get(0);
		SecurityChecked sc = obj.getClass().getAnnotation(SecurityChecked.class);
		if (sc.needExistingSession() && session == null){
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity("Session exipred.").build());
		}
		if (sc.checkrbac()) {
			String url = removePathParameter(request.getPathInfo(), uriInfo
					.getPathParameters() );
			LOGGER.debug(url);
			if (!permission.isAuthorizedSession(url, request.getMethod(),
					session)) {
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
						.entity("User cannot access the resource.").build());
			}			
		}
	}

	/**
	 * rimuove i PathParameters dall'url del servizio
	 *
	 * @param url
	 * @param parameters
	 * @return
	 */
	private final static String removePathParameter(String url,
			MultivaluedMap<String, String> parameters) {
		LOGGER.debug("removing parameters [" + parameters.keySet()
				+ "] from url: " + url);
		for (List<String> value : parameters.values()) {
			url = url.replace("/" + value.get(0), "");
		}

		return url;
	}
}