package it.cnr.cool.service;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.service.util.AlfrescoFolder;

import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FolderChildrenService {
	@Autowired
	private FolderService folderService;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FolderChildrenService.class);

	public ArrayList<AlfrescoFolder> get(Session cmisSession,
			String parentFolderId,
			String username) throws CmisRuntimeException {
		ArrayList<AlfrescoFolder> model = new ArrayList<AlfrescoFolder>();
		if (parentFolderId != null) {
			ItemIterable<QueryResult> children = folderService.getFolderTree(
					cmisSession, parentFolderId, true);
			for (QueryResult result : children.getPage(Integer.MAX_VALUE)) {
				model.add(new AlfrescoFolder(result, false));
			}
		}
		return model;
	}
}
