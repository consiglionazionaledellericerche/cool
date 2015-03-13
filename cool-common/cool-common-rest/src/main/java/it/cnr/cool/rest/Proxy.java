package it.cnr.cool.rest;


import it.cnr.cool.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Path("proxy")
@Component
public class Proxy {

    @Autowired
    private ProxyService proxyService;
    private Map<String, Map> backends;

    @GET
    public void get(
            @Context HttpServletRequest req, @QueryParam("backend") String backend,
            @Context HttpServletResponse res) throws IOException {
        if (backend != null) {
            if (backends.get(backend).containsKey("userName") && backends.get(backend).containsKey("psw")) {
                proxyService.processAutenticateRequest(req, res, backends.get(backend));
            } else {
                proxyService.processGet(req, (String) backends.get(backend).get("url"), res);
            }
        } else {
            proxyService.processGet(req, null, res);
        }
    }


    @POST
    public void post(
            @Context HttpServletRequest req,
            @Context HttpServletResponse res) throws IOException {
        proxyService.processRequest(req, res, true);
    }

    @PUT
    public void put(
            @Context HttpServletRequest req,
            @Context HttpServletResponse res) throws IOException {
        proxyService.processRequest(req, res, false);
    }

    @DELETE
    public void delete(
            @Context HttpServletRequest req,
            @Context HttpServletResponse res) throws IOException {
        proxyService.processDelete(req, res);
    }

    public void setBackends(HashMap<String, Map> backends) {
        this.backends = backends;
    }
}
