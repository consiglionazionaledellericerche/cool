package it.cnr.cool.rest;

import it.cnr.cool.service.typestree.TypesTreeService;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("typesTree")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class TypesTree {

	@Autowired
	private TypesTreeService typesTreeService;
	
	@GET
	@Path("tree")
	public Response getTree(@QueryParam("seeds") List<String> seeds) {
		return Response.ok(typesTreeService.getTree(seeds)).build();
	}
	
}
