package it.cnr.cool.cmis.service;

import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.model.ACLType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ACLServiceTest {

	private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";

	@Autowired
	private ACLService aclService;
	@Autowired
	private CMISService cmisService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ACLServiceTest.class);

	@Test
	public void testAddAndRemoveAcl() {
		Map<String, ACLType> permission = getPermission();
		aclService.addAcl(getBindingSession(), getNodeRef(), permission);
		aclService.removeAcl(getBindingSession(), getNodeRef(), permission);
		assertTrue(true);

	}


	private Map<String, ACLType> getPermission() {
		Map<String, ACLType> permission = new HashMap<String, ACLType>();
		permission.put("mjackson", ACLType.Consumer);
		return permission;
	}

	@Test
	public void testChangeOwnership() {

		String nodeRef = getNodeRef();
		LOGGER.debug(nodeRef);
		String userId = "admin";
		aclService.changeOwnership(getBindingSession(), nodeRef, userId, false,
				Arrays.asList(""));

		LOGGER.info("ownership changed successfully");
		assertTrue(true);
	}

	@Test
	public void testSetInheritedPermission() {
		aclService.setInheritedPermission(getBindingSession(), getNodeRef(),
				false);
		aclService.setInheritedPermission(getBindingSession(), getNodeRef(),
				true);
		assertTrue(true);
	}

	private String getNodeRef() {
		Session session = cmisService.createAdminSession();
		String nodeRef = session.getObjectByPath(OBJECT_PATH).getId()
				.split(";")[0];
		return nodeRef;
	}

	private BindingSession getBindingSession() {
		BindingSession cmisSession = cmisService.createBindingSession("admin",
				"admin");
		return cmisSession;
	}
}
