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

import it.cnr.bulkinfo.BulkInfoImpl;
import it.cnr.bulkinfo.cool.BulkInfoCool;
import it.cnr.bulkinfo.exception.BulkInfoException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.BulkInfoCoolSerializer;
import it.cnr.cool.service.BulkInfoCoolService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Map;
import java.util.Optional;

@Path("bulkInfo")
@Component
@Produces(MediaType.APPLICATION_JSON)

public class BulkInfoRest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoRest.class);

	private static final String DEFAULT_ERROR = "errore generico";

	@Autowired
	private BulkInfoCoolService bulkInfoCoolService;
	@Autowired
	private BulkInfoCoolSerializer bulkInfoCoolSerializer;
	@Autowired
	private CMISService cmisService;

	@GET
	@Path("view/{type}/{kind}/{name}")
	public Response getView(@Context HttpServletRequest req,
			@PathParam("type") String type,
			@PathParam("kind") String kind,
			@PathParam("name") String name,
			@QueryParam("cmis:objectId") String objectId) {

		ResponseBuilder builder;
		Map<String, Object> model;

		Session session = cmisService.getCurrentCMISSession(req);
		BindingSession bindingSession = cmisService.getCurrentBindingSession(req);
		try {
			model = bulkInfoCoolService
					.getView(session, bindingSession, type, kind, name, objectId);

			// String json = Util.processTemplate(model, TEMPLATE_PATH_VIEW);
			String json = bulkInfoCoolSerializer.serialize(model).toString();

			builder = Response.ok(json);

			if(objectId == null || objectId.isEmpty()) {
			  builder.cacheControl(Util.getCache(1800));
			}

		} catch (BulkInfoException e) {
			LOGGER.error("", e);
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		} catch (Exception e) {
			LOGGER.error("error for bulkinfo: " + req.getRequestURL(), e);
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		}

		return builder.build();
	}

	@GET
	@Path("structure/{type}")
	public Response getBulkinfoStructure(@Context HttpServletRequest req,
			@PathParam("type") String type,
			@QueryParam("prefix") String prefix) {

		ResponseBuilder builder;
		String json = "";

		try {
			BulkInfoCool bi = bulkInfoCoolService.find(type);

			json = "[";
			boolean coma = false;
			final Map<String, BulkInfoImpl.FieldPropertySet> forms = bi.getForms();
			for(String form : forms.keySet()) {
				if(form.contains(prefix) &&
						Optional.ofNullable(forms.get(form))
							.flatMap(fieldPropertySet -> Optional.ofNullable(fieldPropertySet.getKey()))
							.map(key -> !key.equals("hidden"))
							.orElse(Boolean.TRUE)
				) {
					json += (coma ? "," : "") + "\""+ form +"\"";
					coma = true;
				}
			}
			json += "]";

			builder = Response.ok(json);
	    builder.cacheControl(Util.getCache(1800));

		} catch (Exception e) {
			LOGGER.error("bulkinfo exception {} {}", type, prefix, e);
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(DEFAULT_ERROR);
		}

		return builder.build();
	}

}

