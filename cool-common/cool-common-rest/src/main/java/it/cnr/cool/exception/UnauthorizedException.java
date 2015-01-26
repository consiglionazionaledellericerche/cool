package it.cnr.cool.exception;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by francesco on 21/01/15.
 */
public class UnauthorizedException extends WebApplicationException {

    public UnauthorizedException(String message, CmisUnauthorizedException e) {
        super(Response.status(Response.Status.UNAUTHORIZED).entity(message).build());

    }
}
