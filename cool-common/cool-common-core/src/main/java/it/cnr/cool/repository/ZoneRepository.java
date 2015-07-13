package it.cnr.cool.repository;

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

@Repository
public class ZoneRepository {

	private static final String ZONES = "zones";

	private static final String ZONES_URL = "service/cnr/groups/zones";

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoneRepository.class);

	@Autowired
	private CMISService cmisService;

	@Cacheable(ZONES)
	public String get() {

		LOGGER.debug("loading zones from alfresco");

		String link = cmisService.getBaseURL().concat(ZONES_URL);
		Response response = CmisBindingsHelper.getHttpInvoker(cmisService.getAdminSession()).invokeGET(new UrlBuilder(link),
				cmisService.getAdminSession());
		if (response.getStream() != null) {
			InputStreamReader responseReader = new InputStreamReader(response.getStream());
			JsonObject jsonZones = new JsonParser().parse(responseReader)
					.getAsJsonObject();
			LOGGER.debug("zones " + jsonZones);
			return jsonZones.toString();
		} else {
			return null;
		}
	}

}
