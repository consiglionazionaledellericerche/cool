/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.web;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.repository.PermissionRepository;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class PermissionTest {

	private static final String WORKFLOW_ASSOCIATION = "workflowAssociation";
	private static final String TEST_WEBSCRIPT = "testWebScript";

	private static final String POST = "POST";
	private static final String GET = "GET";


    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionTest.class);

	@Autowired
	private PermissionServiceImpl p;

    @Autowired
    private CMISService cmisService;


    @Autowired
	private PermissionRepository permissionRepository;

    @Value("${rbac.path}")
    private String rbacPath;

    @After
    public void after () {
		permissionRepository.update(null);
    }


    // guest and unlisted webscript
	@Test
	public void testUnlistedFunctionality() {
		assertFalse(p.isAuthorizedCMIS("unlistedWebScript", GET, getUser("guest")));
	}


	// per-user permission
	@Test
	public void testAllowedUser() {
		assertTrue(p.isAuthorizedCMIS(WORKFLOW_ASSOCIATION, GET, getUser("admin")));
	}

	@Test
	public void testForbiddenUser() {
		assertFalse(p.isAuthorizedCMIS(TEST_WEBSCRIPT, GET, getUser("mario.rossi")));
	}

	@Test
	public void testAllowedUserForbiddenGroup() {
		List<String> groups = new ArrayList<String>();
		groups.add("itc");
		assertTrue(p.isAuthorizedCMIS(TEST_WEBSCRIPT, GET, getUser("abc", groups)));
	}


	// per-group permission
	@Test
	public void testForbiddenGroup() {
		List<String> groups = new ArrayList<String>();
		groups.add("itc");
		assertFalse(p.isAuthorizedCMIS(TEST_WEBSCRIPT, GET, getUser("itc", groups)));
	}

	@Test
	public void testAllowedGroup() {
		List<String> groups = new ArrayList<String>();
		groups.add("si");
		assertTrue(p.isAuthorizedCMIS(TEST_WEBSCRIPT, POST, getUser("somebody", groups)));
	}

	// multiple groups
	@Test
	public void testAllowedMultipleGroup() {
		List<String> groups = new ArrayList<String>();
		groups.add("itc");
		groups.add("si");
		assertTrue(p.isAuthorizedCMIS(TEST_WEBSCRIPT, POST, getUser("somebody", groups)));
	}

	// all allowed/forbidden
	@Test
	public void testAllowedAll(){
		assertTrue(p.isAuthorizedCMIS("i18n", GET, getUser("somebody")));
	}

	@Test
	public void testForbiddenAll() {
		assertFalse(p.isAuthorizedCMIS("/private", GET, getUser("somebody")));
	}

	@Test
	public void testAdd() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "mario.rossi";

		assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
		assertTrue(p.add(id, method, list, type, authority));
		assertTrue(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
	}
	
	@Test
	public void testAddNullValuesId() {

		String id = null;
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "mario.rossi";

		//assertFalse(p.isAuthorizedCMIS(id, method, getUser(authority)));
		try {
			assertTrue(p.add(id, method, list, type, authority));		
			assertTrue(false); // Exception expected: fail
		} catch (IllegalArgumentException exp) {
			// Exception caught: success
		}
	}
	
	@Test
	public void testAddNullValuesMethod() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = null;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "mario.rossi";

		try {
			assertTrue(p.add(id, method, list, type, authority));
			assertTrue(false);
		} catch (IllegalArgumentException exp) {
			// no-action
		}
	}
	
	@Test
	public void testAddNullValuesList() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = null;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "mario.rossi";

		try {
			assertTrue(p.add(id, method, list, type, authority));
			assertTrue(false);
		} catch (IllegalArgumentException exp) {
			// no-action
		}
	}
	
	@Test
	public void testAddNullValuesType() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = null;
		String authority = "mario.rossi";

		try {
			assertTrue(p.add(id, method, list, type, authority));
			assertTrue(false);
		} catch (IllegalArgumentException exp) {
			// no-action
		}
	}
	
	
	@Test
	public void testAddNullValuesAuthority() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = null;

		try {
			assertTrue(p.add(id, method, list, type, authority));
			assertTrue(false);
		} catch (IllegalArgumentException exp) {
			// no-action
		}

	}

	@Test
	public void testAddEmptyStringsId() {

		String id = "";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "mario.rossi";

		try {
			assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
			assertTrue(p.add(id, method, list, type, authority));
		} catch (IllegalArgumentException exp) {
			// no-action
		}
	}
	
	@Test
	public void testAddEmptyStringsAuthority() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "";

		try {
			assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
			assertTrue(p.add(id, method, list, type, authority));
		} catch (IllegalArgumentException exp) {
			// no-action
		}
	}
	
	@Test
	public void testEnableAll() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		String authority = "mario.rossi";
		
		assertTrue(p.enableAll(id, method));
		assertTrue(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
	}

	@Test
	public void testDisableAll() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		String authority = "mario.rossi";

		assertTrue(p.disableAll(id, method));
		assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
	}
	
	@Test
	public void testDelete() {

		String id = WORKFLOW_ASSOCIATION;

		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.GET;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "admin";

		assertTrue(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
		assertTrue(p.delete(id, method, list, type, authority));
		assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
	}

	
	@Test
	public void testDeleteNotExisting() {

		String id = "webscript1";
		PermissionServiceImpl.methods method = PermissionServiceImpl.methods.PUT;
		PermissionServiceImpl.lists list = PermissionServiceImpl.lists.whitelist;
		PermissionServiceImpl.types type = PermissionServiceImpl.types.user;
		String authority = "admin";

		assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
		assertTrue(p.delete(id, method, list, type, authority));
		assertFalse(p.isAuthorizedCMIS(id, method.toString(), getUser(authority)));
	}

	
	// utility methods
	private CMISUser getUser(String username) {
		return getUser(username, null);
	}

	private CMISUser getUser(String username, List<String> groups) {

		CMISUser user = new CMISUser(username);

		List<CMISGroup> l = new ArrayList<CMISGroup>();

		if (groups != null) {
			for (String s : groups) {
				CMISGroup g = new CMISGroup();
				g.setDisplayName(s);
				g.setGroupName(s);
				l.add(g);
			}
		}

		user.setGroups(l);

		return user;
	}
}
