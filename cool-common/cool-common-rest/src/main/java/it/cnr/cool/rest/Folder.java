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
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

@Path("folder")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class Folder {

	private static final Logger LOGGER = LoggerFactory.getLogger(Folder.class);

	@Autowired
	private CMISService cmisService;

	@POST
	public Response create(@Context HttpServletRequest request,
			@FormParam("cmis:name") String name,
			@FormParam("cmis:objectTypeId") String objectTypeId,
			@FormParam("cmis:parentId") String parentId) {

		Session cmisSession = cmisService.getCurrentCMISSession(request);

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PropertyIds.NAME, name);
		properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

		ResponseBuilder rb;

		Map<String, String> model = new HashMap<String, String>();

		try {
			ObjectId folderId = cmisSession.createFolder(properties,
					new ObjectIdImpl(parentId));

			CmisObject folder = cmisSession.getObject(folderId);

			model.put("folder", folder.getName());
			model.put("id", folder.getId());

			rb = Response.ok(model);
		} catch (CmisContentAlreadyExistsException e) {
			LOGGER.error("content already exists {} {} {}", objectTypeId, parentId, name, e);
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		} catch (CmisInvalidArgumentException e) {
			LOGGER.error("folder creation failed {} {} {}", objectTypeId, parentId, name, e);
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		}
		return rb.build();

	}

}
