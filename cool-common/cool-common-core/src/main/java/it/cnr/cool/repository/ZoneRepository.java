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

package it.cnr.cool.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Repository
public class ZoneRepository {

	private static final String ZONES = "zones";

	private static final String ZONES_URL = "service/cnr/groups/zones";

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoneRepository.class);

	@Autowired
	private CMISService cmisService;

	@Cacheable(ZONES)
	public Map<String,String> get() {

		String link = cmisService.getBaseURL().concat(ZONES_URL);

		LOGGER.debug("loading zones from alfresco from {}", link);

		Response response = CmisBindingsHelper.getHttpInvoker(cmisService.getAdminSession()).invokeGET(new UrlBuilder(link),
				cmisService.getAdminSession());
		if (response.getStream() != null) {
			InputStreamReader responseReader = new InputStreamReader(response.getStream());
			JsonObject jsonZones = new JsonParser().parse(responseReader)
					.getAsJsonObject();
			LOGGER.debug("zones " + jsonZones);
			Map<String,String> result = new HashMap<String, String>();
			for (Entry<String, JsonElement> iterable_element : jsonZones.entrySet()) {
				result.put(iterable_element.getKey(), iterable_element.getValue().getAsString());
			}			
			return result;
		} else {
			return null;
		}
	}

}
