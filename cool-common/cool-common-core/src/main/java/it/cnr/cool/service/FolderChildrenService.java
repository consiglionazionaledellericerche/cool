package it.cnr.cool.service;

import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.service.util.AlfrescoFolder;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

public class FolderChildrenService {

	@Autowired
	private OperationContext cmisDefaultOperationContext;
	@Autowired
	private FolderService folderService;
	private static Long MAX_FEATCH_LEAF = Long.valueOf(100);
	private static final String USER_HOMES = "/User Homes/";





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

			if (folderService.getRootNode(cmisSession).getId()
					.equals(parentFolderId)) {
				userHomes = cmisSession.getObjectByPath(USER_HOMES).getId();
				QueryResult userHome = getUserHome(username, userHomes,
						cmisSession);

				String userHomeNodeRef = (String) userHome.getPropertyById(
						PropertyIds.OBJECT_ID).getFirstValue();
				model.add(new AlfrescoFolder(userHome, folderService
						.cachedIsLeaf(userHomeNodeRef, cmisSession)));
			}

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


	private QueryResult getUserHome(String username, String userHomes,
			Session cmisSession) {

		Criteria criteria = criteriaUserHome(username);
		criteria.add(Restrictions.inFolder(userHomes));
		Iterator<QueryResult> i = criteria.executeQuery(cmisSession, false,
				cmisDefaultOperationContext).iterator();

		QueryResult item = null;

		if (i.hasNext()) {
			item = i.next();
		}

		return item;
	}

	private Criteria criteriaUserHome(String username) {
		Criteria criteria = CriteriaFactory
				.createCriteria(BaseTypeId.CMIS_FOLDER.value());
		criteria.addColumn(PropertyIds.NAME);
		criteria.addColumn(PropertyIds.OBJECT_ID);
		criteria.addColumn(PropertyIds.OBJECT_TYPE_ID);
		criteria.add(Restrictions.eq(PropertyIds.NAME, username));
		return criteria;
	}
}