package it.cnr.cool.service.frontOffice;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.util.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class LogTest {
	@Autowired
	private FrontOfficeService frontOfficeService;
	@Autowired
	private CMISService cmisService;
	private Session cmisSession;
	private String nodeRefLog;


	@Before
	public void createLog() throws ParseException {
		cmisSession = cmisService.createAdminSession();

		int codice = 1;
		String application = "cool-jconon";
		String afterString = "2013-07-09";
		String stackTrace = "{\"codice\":"
				+ codice
				+ ",\"mappa\":{\"user\":\"spaclient\",\"url\":\"/doccnr/logger\", \"application\":\""
				+ application
				+ "\"}, \"testo\":\"Ajax request has employed 1770 msec\",\"user-agent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17\",\"Date\":\""
				+ afterString + "\",\"IP\":\"127.0.0.1\"}";
		
		Map<String, Object> mapPost = frontOfficeService.post(null, null, null, null, TypeDocument.Log, stackTrace);
		nodeRefLog = (String) mapPost.get("objectId");
		assertTrue(nodeRefLog!= null);
	}
	
	@After
	public void deleteLog() {
		frontOfficeService.deleteSingleNode(
				cmisSession, nodeRefLog);
	}
	

	
	@Test
	public void testGetLogger() throws IOException{
		List<AlfrescoDocument> logs = frontOfficeService.getLog(cmisSession,
				null, null, null, null, null);
		assertTrue(logs.size() > 0);
	}
}