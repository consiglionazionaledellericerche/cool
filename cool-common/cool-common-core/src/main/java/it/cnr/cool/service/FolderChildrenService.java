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

import java.util.ArrayList;

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
