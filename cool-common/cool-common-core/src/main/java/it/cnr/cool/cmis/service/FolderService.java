package it.cnr.cool.cmis.service;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

public interface FolderService {

	Folder getRootNode(Session session);

	boolean isLeaf(Session session, Folder parentFolder);

	boolean isLeaf(Session session, String parentFolderId);

	ItemIterable<QueryResult> getFolderTree(Session session,
			Folder parentFolder, boolean communityRoot);

	ItemIterable<QueryResult> getFolderTree(Session session,
			String parentFolderId, boolean communityRoot);

	String integrityChecker(String folderName);

	Folder createFolderFromPath(Session session, String path, String name);

	void clearFolderCache();

	String getDataDictionaryId();

	Boolean cachedIsLeaf(String nodeRef, Session cmisSession);

}
