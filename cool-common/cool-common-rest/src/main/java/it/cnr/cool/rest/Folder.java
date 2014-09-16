package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("folder")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class Folder {

	@Autowired
	private CMISService cmisService;

	@POST
	public Response create(@Context HttpServletRequest request,
			@FormParam("cmis:name") String name,
			@FormParam("cmis:objectTypeId") String objectTypeId,
			@FormParam("cmis:parentId") String parentId) {

		HttpSession session = request.getSession(false);
		Session cmisSession = cmisService.getCurrentCMISSession(session);

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
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		} catch (CmisInvalidArgumentException e) {
			model.put("message", e.getMessage());
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(model);
		}
		return rb.build();

	}

}
