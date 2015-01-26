package it.cnr.cool.exception;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 21/01/15.
 */
public class UnauthorizedException extends WebApplicationException {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedException.class);

    public UnauthorizedException(String message, CmisUnauthorizedException e) {
        super(Response.status(Response.Status.UNAUTHORIZED).entity(message).build());
        LOGGER.debug(e.getMessage());

    }
}
