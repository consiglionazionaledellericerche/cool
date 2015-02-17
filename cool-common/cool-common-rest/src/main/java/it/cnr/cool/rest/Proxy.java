package it.cnr.cool.rest;


import it.cnr.cool.service.ProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;


@Path("proxy")
@Component
public class Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    @Autowired
    private ProxyService proxyService;

	@GET
	public void get(@Context HttpServletRequest req, @QueryParam("backend") String backend,
			@Context HttpServletResponse res) throws IOException {
        proxyService.processGet(req, backend, res);
	}


	@POST
	public void post(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
        proxyService.processRequest(req, res, true);
	}

	@PUT
	public void put(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
        proxyService.processRequest(req, res, false);
	}

	@DELETE
	public void delete(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
       proxyService.processDelete(req, res);
	}



}
