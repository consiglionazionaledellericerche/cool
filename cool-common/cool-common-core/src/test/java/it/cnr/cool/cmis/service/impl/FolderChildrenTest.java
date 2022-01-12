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

package it.cnr.cool.cmis.service.impl;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.FolderChildrenService;
import it.cnr.cool.service.util.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class FolderChildrenTest {
    private static final String folderStateNull = "Guest Home";
    private static final String folderStateClosed = "Data Dictionary";
    private final String username = "admin";
    @Autowired
    private FolderChildrenService folderChildrenService;
    @Autowired
    private CMISService cmisService;
    private Session cmisSession;

    @BeforeEach
    public void setUp() {
        cmisSession = cmisService.createAdminSession();
    }

    @Test
    public void testGet() throws IOException {

        // recupero il noderef di Company Home
        String parentFolderId = cmisSession.getObjectByPath("/").getId();
        List<AlfrescoFolder> json = folderChildrenService.get(
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