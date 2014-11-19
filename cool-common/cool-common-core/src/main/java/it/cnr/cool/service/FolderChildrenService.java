package it.cnr.cool.service;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.service.util.AlfrescoFolder;

import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

public class FolderChildrenService {

	@Autowired
	private FolderService folderService;
	private static Long MAX_FEATCH_LEAF = Long.valueOf(10000);

	public ArrayList<AlfrescoFolder> get(Session cmisSession,
			String parentFolderId,
			String username) throws CmisRuntimeException {
		ArrayList<AlfrescoFolder> model = new ArrayList<AlfrescoFolder>();
		if (parentFolderId != null) {
			ItemIterable<QueryResult> children = folderService.getFolderTree(
					cmisSession, parentFolderId, true);
			ItemIterable<QueryResult> page = children
					.getPage(Integer.MAX_VALUE);
			Long totalNumItems = page.getTotalNumItems();

			String userHomes = null;

			for (QueryResult result : page) {
				String nodeRef = (String) result
						.getPropertyById(PropertyIds.OBJECT_ID).getValues()
						.get(0);
				if (nodeRef.equals(userHomes)) {
					// exclude "User Homes" from resultset
					continue;
				}
				if (totalNumItems < MAX_FEATCH_LEAF) {
					model.add(new AlfrescoFolder(result, folderService
							.cachedIsLeaf(nodeRef, cmisSession)));
				} else {
					model.add(new AlfrescoFolder(result, false));
				}

			}
		}
		return model;
	}
}
