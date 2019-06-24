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

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

public interface FolderService {

	Folder getRootNode(Session session);

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
