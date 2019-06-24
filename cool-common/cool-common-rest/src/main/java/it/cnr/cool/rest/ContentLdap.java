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
import it.cnr.cool.security.SecurityChecked;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Optional;

@Path("content-ldap")
@Component
@SecurityChecked
public class ContentLdap {

    @Autowired
    private CMISService cmisService;

    @Autowired
    private Content content;

    @GET
    public Response get(@Context HttpServletRequest req,
                        @Context HttpServletResponse res,
                        @QueryParam("path") String path,
                        @QueryParam("nodeRef") String nodeRef,
                        @QueryParam("deleteAfterDownload") Boolean deleteAfterDownload,
                        @QueryParam("fileName") String fileName) throws URISyntaxException {
        return Optional.ofNullable(cmisService.getCMISUserFromSession(req))
                .filter(cmisUser -> Optional.ofNullable(cmisUser.getImmutability()).map(stringBooleanMap -> !stringBooleanMap.isEmpty()).orElse(Boolean.FALSE))
                .map(cmisUser -> {
                    try {
                        return content.content(req, res, path, nodeRef, deleteAfterDownload, fileName);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() -> {
                    try {
                        return content.redirect(req, nodeRef, path, "content-ldap", new CmisUnauthorizedException());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
