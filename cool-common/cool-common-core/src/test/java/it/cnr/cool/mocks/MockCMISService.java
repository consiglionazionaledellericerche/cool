package it.cnr.cool.mocks;

import it.cnr.cool.cmis.service.CMISService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockCMISService extends CMISService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MockCMISService.class);

    private Map<String, String> map = new HashMap<>();



	@Override
	public InputStream getDocumentInputStream(Session session,
			String path) {

        if (map.containsKey(path)) {
            String s =  map.get(path);
            LOGGER.debug("loading RBAC from cache");
            return IOUtils.toInputStream(s);
        } else {
            LOGGER.debug("loading RBAC from classpath");
            return MockCMISService.class.getResourceAsStream("/rbac.get.json.ftl");
        }




	}

	@Override
	public void updateDocument(Session session, String path, String content) {
        map.put(path, content);
		LOGGER.info("do not perform update of " + path);
	}
}
