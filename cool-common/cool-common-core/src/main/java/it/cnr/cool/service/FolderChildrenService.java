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

package it.cnr.cool.service;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.service.util.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class FolderChildrenService {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FolderChildrenService.class);
    @Autowired
    private FolderService folderService;

    public ArrayList<AlfrescoFolder> get(Session cmisSession,
                                         String parentFolderId,
                                         String username) throws CmisRuntimeException {
        ArrayList<AlfrescoFolder> model = new ArrayList<AlfrescoFolder>();
        if (parentFolderId != null) {
            LOGGER.info("get children of folder {}", parentFolderId);
            ItemIterable<QueryResult> children = folderService.getFolderTree(
                    cmisSession, parentFolderId, true);
            for (QueryResult result : children.getPage(Integer.MAX_VALUE)) {
                model.add(new AlfrescoFolder(result, false));
            }
        }
        return model;
    }
}
