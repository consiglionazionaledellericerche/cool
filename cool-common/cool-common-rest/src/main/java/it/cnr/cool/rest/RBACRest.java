package it.cnr.cool.rest;

import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.service.workflow.WorkflowService;
import it.cnr.cool.web.PermissionService;
import it.cnr.cool.web.PermissionServiceImpl;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("rbac")
@Component
@SecurityChecked
public class RBACRest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RBACRest.class);

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private PermissionService permissionService;

	@POST
	public Response post(@FormParam("id") String id,
			@FormParam("method") String method, @FormParam("list") String list,
			@FormParam("type") String type,
			@FormParam("authority") String authority,
			@FormParam("workflow") String workflow) {

		ResponseBuilder builder;

		PermissionVO item = new PermissionVO(id, method, list, type, authority);

		LOGGER.info("inserting " + item.toString());

		if (!permissionService.add(item.getId(), item.getMethod(), item.getList(),
				item.getType(), item.getAuthority())) {

			builder = Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Unable to perform INSERT");
		} else {
			builder = Response.ok();

		}

		if (Boolean.valueOf(workflow)) {
			workflowService.clearWorkflowDefinitions(authority);
		}

		return builder.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() throws Exception {
		String content = permissionService.getRbacAsString();
		ResponseBuilder rb = Response.status(Status.OK).entity(
				content);
		return rb.build();
	}

	@DELETE
	public Response delete(@QueryParam("id") String id,
			@QueryParam("method") String method,
			@QueryParam("list") String list,
			@QueryParam("type") String type,
			@QueryParam("authority") String authority,
			@QueryParam("workflow") String workflow) {


		ResponseBuilder builder;

		PermissionVO item = new PermissionVO(id, method, list, type, authority);

		LOGGER.info("deleting " + item.toString());

		if (!permissionService.delete(item.getId(), item.getMethod(), item.getList(),
				item.getType(), item.getAuthority())) {

			builder = Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Unable to perform DELETE");
		} else {
			builder = Response.ok();

		}

		if (Boolean.valueOf(workflow)) {
			workflowService.clearWorkflowDefinitions(authority);
		}

		return builder.build();

	}


	class PermissionVO {

		private final String id;
		private final PermissionServiceImpl.methods method;
		private final PermissionServiceImpl.lists list;
		private final PermissionServiceImpl.types type;
		private final String authority;

		public PermissionVO(String id, String method, String list, String type,
				String authority) {
			super();
			this.id = id;
			this.method = PermissionServiceImpl.methods.valueOf(method);
			this.list = PermissionServiceImpl.lists.valueOf(list);
			this.type = PermissionServiceImpl.types.valueOf(type);
			this.authority = authority;

		}

		public String getId() {
			return id;
		}

		public PermissionServiceImpl.methods getMethod() {
			return method;
		}

		public PermissionServiceImpl.lists getList() {
			return list;
		}

		public PermissionServiceImpl.types getType() {
			return type;
		}

		public String getAuthority() {
			return authority;
		}

		@Override
		public String toString() {
			return id + " " + method.toString() + " " + list.toString() + " "
					+ type.toString() + " "
					+ (authority != null ? "authority " + authority.toString()
							: "");
		}



	}
}