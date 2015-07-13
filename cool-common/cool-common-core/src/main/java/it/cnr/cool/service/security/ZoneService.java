package it.cnr.cool.service.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStreamReader;

//TODO: da aggiungere a cache
public class ZoneService {

	@Autowired
	private CMISService cmisService;

	private static final String ZONES_URL = "service/cnr/groups/zones";

	private String zones;

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoneService.class);

	public String get() {
		if (zones == null) {
			String link = cmisService.getBaseURL().concat(ZONES_URL);
			Response response = CmisBindingsHelper.getHttpInvoker(cmisService.getAdminSession()).invokeGET(new UrlBuilder(link),
					cmisService.getAdminSession());
			if (response.getStream() != null) {
				InputStreamReader responseReader = new InputStreamReader(response.getStream());
				JsonObject jsonZones = new JsonParser().parse(responseReader)
						.getAsJsonObject();
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("zones " + jsonZones);
				zones = jsonZones.toString();				
			}
		}
		return zones;
	}

}
