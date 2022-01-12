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
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.util.OperationContextUtils;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderChildrenService {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FolderChildrenService.class);
    @Autowired
    private FolderService folderService;

    public List<AlfrescoFolder> get(Session cmisSession,
                                         String parentFolderId,
                                         String username) throws CmisRuntimeException {
        ArrayList<AlfrescoFolder> model = new ArrayList<AlfrescoFolder>();
        final OperationContext operationContext = OperationContextUtils.copyOperationContext(cmisSession.getDefaultContext());
        operationContext.setIncludePathSegments(false);
        operationContext.setIncludeAllowableActions(true);
        operationContext.setMaxItemsPerPage(100);
        cmisSession.setDefaultContext(operationContext);
        if (parentFolderId != null) {
            LOGGER.info("get children of folder {}", parentFolderId);
            final List<Tree<FileableCmisObject>> folderTree = ((Folder) cmisSession.getObject(parentFolderId))
                    .getFolderTree(1, operationContext);
            folderTree.stream()
                    .forEach(fileableCmisObjectTree -> {
                        final FileableCmisObject item = fileableCmisObjectTree.getItem();
                        model.add(new AlfrescoFolder(item,false));
                    });
        }
        return model
                .stream()
                .sorted((o1, o2) -> o1.getData().toUpperCase().compareTo(o2.getData().toUpperCase()))
                .collect(Collectors.toList());
    }
}
