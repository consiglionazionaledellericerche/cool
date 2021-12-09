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
import it.cnr.cool.exception.UnauthorizedException;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.QueryService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Map;

@Path("search")
@Component
public class Search {

    private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);

    @Autowired
    private QueryService queryService;

    @Autowired
    private CMISService cmisService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@Context HttpServletRequest request) {

        ResponseBuilder rb;

        Map<String, Object> model = null;

        try {
            Session session = cmisService.getCurrentCMISSession(request);
            model = queryService.query(request, session);
        } catch (CmisUnauthorizedException|CmisPermissionDeniedException e) {
            throw new UnauthorizedException("unauthorized search", e);
        }
        try {
            rb = Response.ok(model);
            String cache = request.getParameter("cache");
            CacheControl cacheControl;
            if (cache != null) {

                int seconds = Integer.parseInt(cache);
                LOGGER.debug("cache for {} seconds", seconds);
                cacheControl = Util.getCache(seconds);

            } else {
                LOGGER.debug("no cache");
                cacheControl = new CacheControl();
                cacheControl.setNoCache(true);
            }
            rb.cacheControl(cacheControl);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rb = Response.status(Status.INTERNAL_SERVER_ERROR);
        }

        return rb.build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("document/version")
    public Response documentVersion(@Context HttpServletRequest request, @QueryParam("nodeRef") String nodeRef) {

        ResponseBuilder rb;
        Session session = cmisService.getCurrentCMISSession(request);

        Map<String, Object> model = queryService.documentVersion(session, nodeRef);
        try {
            rb = Response.ok(model);
            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoCache(true);
            rb.cacheControl(cacheControl);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rb = Response.status(Status.INTERNAL_SERVER_ERROR);
        }

        return rb.build();

    }

    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

}