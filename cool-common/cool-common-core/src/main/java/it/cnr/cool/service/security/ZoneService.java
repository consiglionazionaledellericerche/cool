package it.cnr.cool.service.security;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.GlobalCache;

import java.io.InputStreamReader;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ZoneService implements GlobalCache, InitializingBean{
	@Autowired
	private CacheService cacheService;
	@Autowired
	private CMISService cmisService;

	private static final String ZONES_URL = "service/cnr/groups/zones";

	private String zones;

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoneService.class);

	@Override
	public String name() {
		return "zones";
	}

	@Override
	public void clear() {
		zones = null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cacheService.register(this);
	}

	@Override
	public String get() {
		if (zones == null) {
			String link = cmisService.getBaseURL().concat(ZONES_URL);
			Response response = CmisBindingsHelper.getHttpInvoker(cmisService.getAdminSession()).invokeGET(new UrlBuilder(link),
					cmisService.getAdminSession());
			InputStreamReader responseReader = new InputStreamReader(response.getStream());
			JsonObject jsonZones = new JsonParser().parse(responseReader)
					.getAsJsonObject();
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("zones " + jsonZones);
			zones = jsonZones.toString();
		}
		return zones;
	}

}
