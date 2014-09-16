package it.cnr.cool.cmis.service;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class NodeMetadataServiceTest {

	@Autowired
	private NodeMetadataService nodeMetadataService;

	@Autowired
	private CMISService cmisService;

	private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeMetadataServiceTest.class);

	@Test
	public void testUpdateObjectProperties() throws ParseException {

		Session cmisSession = cmisService.createAdminSession();
		CmisObject object = cmisSession.getObjectByPath(OBJECT_PATH);

		LOGGER.info(object.getId());

		Map<String, Object> reqProperties = new HashMap<String, Object>();
		reqProperties.put(PropertyIds.OBJECT_ID, object.getId());
		reqProperties.put(PropertyIds.OBJECT_TYPE_ID, object.getType().getId());

		HttpServletRequest request = new MockHttpServletRequest();

		CmisObject doc = nodeMetadataService.updateObjectProperties(
				reqProperties, cmisSession,
				request);

		assertEquals(doc.getId(), object.getId());

	}


}
