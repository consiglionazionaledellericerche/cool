package it.cnr.cool.cmis.service.impl;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.FolderService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.Order;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

public class FolderServiceImpl implements FolderService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FolderServiceImpl.class);

	@Autowired
	private OperationContext cmisDefaultOperationContext;
	@Autowired
	private CMISService cmisService;
	private final Map<String, Folder> cacheFolder = new HashMap<String, Folder>();

	private Folder dataDictionary;

	protected String dataDictionaryPath;

	public void setDataDictionaryPath(String dataDictionaryPath) {
		this.dataDictionaryPath = dataDictionaryPath;
	}

	@Override
	public Folder getRootNode(Session session) {
		return session.getRootFolder(cmisDefaultOperationContext);
	}

	@Override
	public ItemIterable<QueryResult> getFolderTree(Session session,
			Folder parentFolder, boolean communityRoot) {
		return getFolderTree(session, parentFolder.getId(), communityRoot);
	}

	@Override
	public ItemIterable<QueryResult> getFolderTree(Session session,
			String parentFolderId, boolean communityRoot) {
		Criteria criteria = CriteriaFactory
				.createCriteria(BaseTypeId.CMIS_FOLDER.value());
		criteria.addColumn(PropertyIds.NAME);
		criteria.addColumn(PropertyIds.OBJECT_ID);
		criteria.addColumn(PropertyIds.OBJECT_TYPE_ID);
		criteria.add(Restrictions.inFolder(parentFolderId));
		criteria.addOrder(Order.asc(PropertyIds.NAME));
		return criteria.executeQuery(session, false,
				cmisDefaultOperationContext);
	}

	private boolean isLeaf(Session session, String parentFolderId) {
		Criteria criteria = CriteriaFactory
				.createCriteria(BaseTypeId.CMIS_FOLDER.value());
		criteria.addColumn(PropertyIds.OBJECT_ID);
		criteria.add(Restrictions.inFolder(parentFolderId));
		OperationContext operationContext = session.createOperationContext();
		operationContext.setCacheEnabled(true);
		operationContext.setMaxItemsPerPage(1);
		operationContext.setIncludeAllowableActions(false);
		operationContext.setIncludePathSegments(false);
		ItemIterable<QueryResult> results = criteria.executeQuery(session,
				false, operationContext);
		return results.getPage().getTotalNumItems() == 0;
	}

	/**
	 *
	 * Check if a folder contains subfolders or is leaf
	 *
	 * @param nodeRef
	 * @param cmisSession
	 * @return true if node is leaf
	 */
	@Override
	@Cacheable("isLeaf")
	public Boolean cachedIsLeaf(final String nodeRef, final Session cmisSession) {
		LOGGER.debug("retrieving from alfresco if " + nodeRef + " is Leaf ");
		return isLeaf(cmisSession, nodeRef);
	}


	private boolean isLeaf(Session session, Folder parentFolder) {
		return isLeaf(session, parentFolder.getId());
	}

	@Override
	public void clearFolderCache() {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Clear Folder cache");
		cacheFolder.clear();
	}

	@Override
	public Folder createFolderFromPath(Session session, String path, String name) {
		if (cacheFolder.get(path.concat("/").concat(name)) != null)
			return cacheFolder.get(path.concat("/").concat(name));
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				BaseTypeId.CMIS_FOLDER.value());
		properties.put(PropertyIds.NAME, name);
		Folder sourceFolder = (Folder) session.getObjectByPath(path);
		Folder newFolder;
		try {
			newFolder = sourceFolder.createFolder(properties);
			return newFolder;
		} catch (CmisContentAlreadyExistsException e) {
			newFolder = (Folder) session.getObjectByPath(sourceFolder.getPath()
					.concat("/").concat(name));
		}
		cacheFolder.put(newFolder.getPath(), newFolder);
		return newFolder;
	}

	@Override
	public String integrityChecker(String folderName) {
		folderName = folderName.trim().replaceAll(":", "_")
				.replaceAll("/", "_").replaceAll("\"", "'")
				.replaceAll("  ", " ");
		Pattern specialCharacters = Pattern.compile("[*?:\"|\\\\<>\\/]");
		Pattern blackListEnd = Pattern.compile("\\.+$");
		String temp;
		temp = specialCharacters.matcher(folderName).replaceAll(" ");
		temp = blackListEnd.matcher(temp).replaceAll("");
		return temp;
	}

	@Override
	public String getDataDictionaryId() {
		if (dataDictionary == null)
			dataDictionary = (Folder) cmisService.createAdminSession()
					.getObjectByPath(dataDictionaryPath);
		return dataDictionary.getId();
	}

}
