package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CustomAuthenticationProvider;
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.service.FolderChildrenService;
import it.cnr.cool.service.util.AlfrescoFolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/search/folder")
@Component
@Produces(MediaType.APPLICATION_JSON)
@SecurityChecked
public class FolderChildren {

	private static final Logger LOGGER = LoggerFactory.getLogger(FolderChildren.class);

	@Autowired
	private FolderChildrenService folderChildrenService;
	@Autowired
	private CMISService cmisService;

	@Autowired
	private FolderService folderService;

	@GET
	@Path("children")
	public Response getChildren(@Context HttpServletRequest req,
			@QueryParam("parentFolderId") String parentFolderId) {

		HttpSession session = req.getSession(false);
		String username = (String) session
				.getAttribute(CustomAuthenticationProvider.SESSION_ATTRIBUTE_KEY_USER_ID);

		ArrayList<AlfrescoFolder> model;
		Response response;

		try {
			model = folderChildrenService.get(
					cmisService.getCurrentCMISSession(session), parentFolderId,
					username);
			response = Response.ok(model).build();
		} catch (CmisRuntimeException e) {
			response = Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getErrorContent()).build();
		}
		return response;
	}

	@GET
	@Path("root")
	public Map<String, Object> getRoot(@Context HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		Session cmisSession = cmisService.getCurrentCMISSession(session);
		Folder folder = folderService.getRootNode(cmisSession);

		List<String> actions = getActions(folder);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("id", folder.getId());
		response.put("allowableActions", actions);

		return response;

	}

	private List<String> getActions(Folder folder) {
		List<String> actions = new ArrayList<String>();

		for (Action action : folder.getAllowableActions().getAllowableActions()) {
			String value = action.toString();
			LOGGER.debug(value);
			actions.add(value);
		}
		return actions;
	}
}