package it.cnr.cool.rest;


import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CmisAuthRepository;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.service.ProxyService;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private CmisAuthRepository cmisAuthRepository;

    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

    @Autowired
    private CMISService cmisService;

    private Map<String, Map> backends;

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    @GET
    public void get(
            @Context HttpServletRequest req, @QueryParam("backend") String backend,
            @Context HttpServletResponse res) throws IOException {

        UrlBuilder urlBuilder;
        BindingSession bindingSession;

        if (backend != null) {


            String username = (String) backends.get(backend).get("userName");
            String password = (String) backends.get(backend).get("psw");

            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                LOGGER.info("using backend {} with user {}", backend, username);

                bindingSession = cmisService.createBindingSession(username, password);

            } else {
                LOGGER.info("no credentials provided for backend {}", backend);
                bindingSession = cmisService.getCurrentBindingSession(req);
            }

            String base = (String) backends.get(backend).get("url");

            LOGGER.info(base);

            urlBuilder = ProxyService.getUrl(req, base);

        } else {
            bindingSession = cmisService.getCurrentBindingSession(req);
            urlBuilder = ProxyService.getUrl(req, cmisService.getBaseURL());
        }

        LOGGER.info(urlBuilder.toString());


        proxyService.processGet(bindingSession, urlBuilder, res);
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
