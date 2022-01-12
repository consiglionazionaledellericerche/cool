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
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.service.FolderChildrenService;
import it.cnr.cool.service.util.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String username = cmisService.getCMISUserFromSession(req).getId();

		List<AlfrescoFolder> model;
		Response response;

		try {
			model = folderChildrenService.get(
					cmisService.getCurrentCMISSession(req), parentFolderId,
					username);
			response = Response.ok(model).build();
		} catch (CmisRuntimeException e) {
			LOGGER.error("get children of {}", parentFolderId, e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getErrorContent()).build();
		}
		return response;
	}

	@GET
	@Path("root")
	public Map<String, Object> getRoot(@Context HttpServletRequest req) {
		Session cmisSession = cmisService.getCurrentCMISSession(req);
		Folder folder = folderService.getRootNode(cmisSession);

		List<String> actions = getActions(folder);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("id", folder.getId());
		response.put("allowableActions", actions);

		return response;

	}

	@GET
	@Path("by-path")
	public Map<String, Object> getFolderByPath(@Context HttpServletRequest req, @QueryParam("path") String path) {
		Session cmisSession = cmisService.getCurrentCMISSession(req);
		Folder folder = (Folder) cmisSession.getObjectByPath(path);

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