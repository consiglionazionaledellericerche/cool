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
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.service.ProxyService;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@Path("proxy")
@Component
public class Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
    @Autowired
    private ProxyService proxyService;
    @Autowired
    private CMISService cmisService;
    private Map<String, Map<String, String>> backends;

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
            if (!Optional.ofNullable(base).filter(s -> s.length() > 0).isPresent()) {
                throw new CoolException("The request url is not specified", HttpStatus.SC_BAD_REQUEST);
            }
            LOGGER.info(base);

            urlBuilder = proxyService.getUrl(req, base);

        } else {
            if (cmisService.getCMISUserFromSession(req).isGuest()) {
                LOGGER.error("The request url is forbidden");
                throw new CoolException("The request url is forbidden", HttpStatus.SC_FORBIDDEN);
            }
            bindingSession = cmisService.getCurrentBindingSession(req);
            urlBuilder = proxyService.getUrl(req, cmisService.getBaseURL());
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

    public void setBackends(Map<String, Map<String, String>> backends) {
        this.backends = backends;
    }
}
