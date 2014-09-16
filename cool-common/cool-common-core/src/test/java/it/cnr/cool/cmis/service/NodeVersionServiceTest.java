package it.cnr.cool.cmis.service;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class NodeVersionServiceTest {

	@Autowired
	private CMISService cmisService;

	@Autowired
	private NodeVersionService nodeVersionService;

	private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";

	@Test
	public void testAddAutoVersionDocument() {
		Document doc = getDocument();

		nodeVersionService.addAutoVersion(doc, true);
		nodeVersionService.addAutoVersion(doc, false);
	}

	@Test
	public void testAddAutoVersionDocumentBoolean() {
		Document doc = getDocument();
		nodeVersionService.addAutoVersion(doc);
		nodeVersionService.addAutoVersion(doc, false);
	}

	private Document getDocument() {
		Session cmisSession = cmisService.createAdminSession();
		Document doc = (Document) cmisSession.getObjectByPath(OBJECT_PATH);
		return doc;
	}

}
