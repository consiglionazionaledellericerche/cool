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
