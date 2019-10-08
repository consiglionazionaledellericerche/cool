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

package it.cnr.cool.cmis.service;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.model.ACLType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class ACLServiceTest {

    public static final String WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";
    private static final Logger LOGGER = LoggerFactory.getLogger(ACLServiceTest.class);
    @Autowired
    private ACLService aclService;
    @Autowired
    private CMISService cmisService;

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
        String nodeRef = WORKSPACE_SPACES_STORE + session.getObjectByPath(OBJECT_PATH).getId()
                .split(";")[0];
        return nodeRef;
    }

    private BindingSession getBindingSession() {
        BindingSession cmisSession = cmisService.createBindingSession("admin",
                "admin");
        return cmisSession;
    }
}
