package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.workflow.WorkflowService;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Path("workflow")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class WorkflowDefinitions {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDefinitions.class);

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private CMISService cmisService;

	/**
	 *
	 * Put the workflow definition list filtered by RBAC in the model in JSON
	 * format.
	 *
	 * If the request has a querystring parameter "user", the list will be
	 * filtered for this user, otherwise will be filtered for the current user.
	 *
	 * @return
	 *
	 */
	@GET
	@Path("definitions")
	public Response getDefinitions(@Context HttpServletRequest req, @QueryParam("user")  String username) {

		CMISUser user = getUser(username, req.getSession(false));

		BindingSession bindingSession = cmisService.getCurrentBindingSession(req);

		String content = workflowService.get(user, bindingSession);

        String resp = "{ \"definitions\": " + content + "}";
		return Response.ok(resp).build();

	}


	/**
	 * Create CMISUser with a specified username
	 *
	 * @param username
	 * @return a CMISUser with username received as parameter and no groups
	 */
	private CMISUser getUser(String username, HttpSession session) {

		CMISUser user;

		if (username != null) {
			LOGGER.info("username: " + username);
			user = new CMISUser(username);
			user.setGroups(new ArrayList<CMISGroup>());

		} else {
			user = cmisService.getCMISUserFromSession(session);
		}

		return user;
	}

}
