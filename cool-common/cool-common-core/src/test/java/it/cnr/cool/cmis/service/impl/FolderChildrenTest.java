package it.cnr.cool.cmis.service.impl;

import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.FolderChildrenService;
import it.cnr.cool.service.util.AlfrescoFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
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
public class FolderChildrenTest {
	private static final String folderStateNull = "Guest Home";
	private static final String folderStateClosed = "Data Dictionary";
	@Autowired
	private FolderChildrenService folderChildrenService;
	@Autowired
	private CMISService cmisService;
	private Session cmisSession;
	private final String username = "admin";


	@Before
	public void setUp() {
		cmisSession = cmisService.createAdminSession();
	}

	@Test
	public void testGet() throws IOException {

		// recupero il noderef di Company Home
		String parentFolderId = cmisSession.getObjectByPath("/").getId();
		ArrayList<AlfrescoFolder> json = folderChildrenService.get(
				cmisSession, parentFolderId, username);

		assertTrue(json.size() > 0);

		Iterator<AlfrescoFolder> it = json.iterator();
		// verifico che il json sia ben formato
		while (it.hasNext()) {
			AlfrescoFolder folder = it.next();
			if (folder.getData().equals(folderStateClosed)) {
				assertTrue(folder.getState().equals("closed"));
				assertTrue(folder
						.getAttr()
						.get("id")
						.equals(cmisSession.getObjectByPath(
								"/" + folderStateClosed).getId()));
				assertTrue(folder.getAttr().get("rel").equals("folder"));
			}
		}
	}
}