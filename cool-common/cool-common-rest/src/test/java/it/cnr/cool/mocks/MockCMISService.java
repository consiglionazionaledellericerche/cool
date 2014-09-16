package it.cnr.cool.mocks;

import it.cnr.cool.cmis.service.CMISService;

import java.io.InputStream;

import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockCMISService extends CMISService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MockCMISService.class);

	@Override
	public InputStream getDocumentInputStream(Session session,
			String path) {
		LOGGER.info("using mock");
		return MockCMISService.class.getResourceAsStream("/rbac.get.json.ftl");
	}

}
