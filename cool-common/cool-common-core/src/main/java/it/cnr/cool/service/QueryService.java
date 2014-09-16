package it.cnr.cool.service;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.model.RelationshipTypeParam;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;

public class QueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

	@Autowired
	private OperationContext cmisDefaultOperationContext;

	@Autowired
	private OperationContext cmisAllOperationContext;

	@Autowired
	private CMISService cmisService;

	@Autowired
	private UserService userService;

	// cache nodes parents
	private final com.google.common.cache.Cache<String, List<Folder>> nodeParentsCache = CacheBuilder
			.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)
			.maximumSize(100).build();

	public Map<String, Object> query(HttpServletRequest req) {

		// request params
		String statement = req.getParameter("q");
		String folder = req.getParameter("f");
		String objectRel = req.getParameter("objectRel");
		String maxItems = req.getParameter("maxItems");
		Boolean calculateTotalNumItems = Boolean.valueOf(req
				.getParameter("calculateTotalNumItems"));
		Boolean fetchCmisObject = Boolean.valueOf(req
				.getParameter("fetchCmisObject"));
		Boolean exportData = Boolean.valueOf(req.getParameter("exportData"));
		String parameter_relationship = req.getParameter("relationship");
		String[] parameter_relationship_name = req
				.getParameterValues("relationship.name");
		String[] parameter_relationship_field = req
				.getParameterValues("relationship.field");
		String parameter_skip_count = req.getParameter("skipCount");
		String orderBy = req.getParameter("orderBy");

		Session cmisSession = getSession(req);

		return query(cmisSession, parameter_relationship,
				parameter_relationship_name, maxItems, exportData, folder,
				objectRel, fetchCmisObject, calculateTotalNumItems, statement,
				parameter_relationship_field, parameter_skip_count, orderBy);

	}

	protected Session getSession(HttpServletRequest request) {
		LOGGER.debug("retrieving CMIS session from HTTP Session");
		HttpSession session = request.getSession(false);
		return cmisService.getCurrentCMISSession(session);
	}


	private Map<String, Object> query(Session cmisSession,
			String parameter_relationship,
			String[] parameter_relationship_name, String maxItems,
			Boolean exportData, String folder, String objectRel,
			Boolean fetchCmisObject, Boolean calculateTotalNumItems,
			String statement, String[] parameter_relationship_field,
			String parameter_skip_count, String orderBy) {



		Map<String, Object> model = new HashMap<String, Object>();

		RelationshipTypeParam relationshipTypeParam = RelationshipTypeParam.none;
		List<String> relationshipName = null;
		List<String> relationshipField = null;
		if (parameter_relationship != null)
			relationshipTypeParam = RelationshipTypeParam.valueOf(parameter_relationship);

		if (parameter_relationship_name != null && parameter_relationship_name.length > 0)
			relationshipName = Arrays.asList(parameter_relationship_name);
		if (parameter_relationship_field != null && parameter_relationship_field.length > 0)
			relationshipField = Arrays.asList(parameter_relationship_field);
		OperationContext operationContext = new OperationContextImpl(cmisDefaultOperationContext);


		if (maxItems != null)
			operationContext.setMaxItemsPerPage(Integer.valueOf(maxItems));
		if (relationshipTypeParam.isChildRelationship() || relationshipTypeParam.isSearchRelationship())
			operationContext.setIncludeRelationships(IncludeRelationships.BOTH);
		else
			operationContext.setIncludeRelationships(IncludeRelationships.NONE);

		BigInteger skipTo = BigInteger.ZERO;
		if (parameter_skip_count != null){
			skipTo = BigInteger.valueOf(Long.valueOf(parameter_skip_count));
		}
		long s = skipTo.compareTo(BigInteger.ZERO) < 0 ? BigInteger.ZERO
				.longValue() : skipTo.longValue();

				Long totalNumItems;
				boolean hasMoreItems;

				if (folder != null) {

					operationContext.setOrderBy("cmis:baseTypeId DESC"
							+ ((orderBy != null && orderBy.length() > 0) ? ","
									+ orderBy : ""));

					Set<String> columns = new HashSet<String>();
					columns.add(PropertyIds.OBJECT_ID);
					columns.add(PropertyIds.OBJECT_TYPE_ID);
					columns.add(PropertyIds.BASE_TYPE_ID);
					columns.add(PropertyIds.NAME);
					columns.add(PropertyIds.LAST_MODIFICATION_DATE);
					columns.add(PropertyIds.LAST_MODIFIED_BY);
					columns.add(PropertyIds.CONTENT_STREAM_LENGTH);
					columns.add(PropertyIds.CONTENT_STREAM_MIME_TYPE);
					columns.add("alfcmis:nodeRef");
					operationContext.setFilter(columns);

					operationContext.setIncludeAllowableActions(true);
					operationContext.setIncludePathSegments(false);
					ItemIterable<CmisObject> items = ((Folder) cmisSession
							.getObject(folder, operationContext))
							.getChildren(operationContext);
					items = items.skipTo(s);
					items = items.getPage();

					totalNumItems = items.getTotalNumItems();


					List<CmisObject> result = new ArrayList<CmisObject>();
					for (CmisObject item : items) {
						result.add(item);
					}

					hasMoreItems = items.getHasMoreItems();

					model.put("models", result);
				} else if (objectRel != null){
					operationContext.setIncludeRelationships(IncludeRelationships.SOURCE);
					CmisObject cmisObject = cmisSession.getObject(objectRel, operationContext);
					hasMoreItems = false;
					List<CmisObject> rels = new ArrayList<CmisObject>();
					totalNumItems = Long.valueOf(0);
					if (cmisObject.getRelationships() != null && !cmisObject.getRelationships().isEmpty()) {
						totalNumItems = Long.valueOf(cmisObject.getRelationships().size());
						for (Relationship relationship : cmisObject.getRelationships()) {
							if (relationshipName != null) {
								if (relationshipName.contains(relationship.getType().getId()))
									rels.add(relationship.getTarget());
							} else {
								rels.add(relationship.getTarget());
							}
						}
					}
					model.put("models", rels);
				} else {

					ItemIterable<QueryResult> queryResult = cmisSession.query(
							statement, false, operationContext);

					queryResult = queryResult.skipTo(s);
					queryResult = queryResult.getPage();
					hasMoreItems = queryResult.getHasMoreItems();
					totalNumItems = getTotalNumItems(cmisSession, queryResult, statement);
					Long maxItemsPerPage = Long.valueOf(operationContext.getMaxItemsPerPage());


					if (totalNumItems.equals(maxItemsPerPage) && hasMoreItems) {
						totalNumItems = Long.MIN_VALUE;
					}

			if (calculateTotalNumItems && totalNumItems == Long.MIN_VALUE) {
						totalNumItems = getQuickTotalNumItems(cmisSession, statement);
					}
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("There are " + totalNumItems
								+ " documents in repository");

					Map<String, String> matricole = new HashMap<String, String>();
					List<Object> recentSubmission = new ArrayList<Object>();
					Map<String, Map<String, Object>> relationships = new HashMap<String, Map<String, Object>>();
					for (QueryResult result : queryResult) {
						if (fetchCmisObject) {
							String nodeRef = result.getPropertyValueById(PropertyIds.OBJECT_ID);
							CmisObject cmisObject = cmisSession.getObject(nodeRef, operationContext);
							recentSubmission.add(cmisObject);
							//Recupero anche le matricole (export-xls)
							if(exportData) {
								String userName = cmisObject.getPropertyValue("jconon_application:user");
								try {
									if(!matricole.containsKey(userName)){
										CMISUser user = (CMISUser) userService.loadUserForConfirm(userName);
										matricole.put(userName, String.valueOf(user.getMatricola()));
									}
								} catch (CoolUserFactoryException e) {
									matricole.put(userName, "");
									LOGGER.info("UserFactoryException - Utente: " + userName, e);
								}
							}
						} else {
							recentSubmission.add(result);
						}
						if (relationshipTypeParam.isSearchRelationship())
							addRelationshipToModel(operationContext,
									result.getRelationships(), relationships,
									relationshipTypeParam, relationshipName,
									relationshipField);
						if (relationshipTypeParam.isChildRelationship())
					addChildToModel(cmisSession, result, relationships,
							relationshipName, relationshipField);
						if (relationshipTypeParam.isParentRelationship())
					addParentToModel(cmisSession, result, relationships,
							relationshipName, relationshipField);

					}
					model.put("models", recentSubmission);
					if (!relationships.isEmpty())
						model.put("relationships", relationships);

					if(exportData){
						Folder bandoPadre;
						Folder domandaGenerica;
						if (!recentSubmission.isEmpty()){
							domandaGenerica =(Folder) recentSubmission.get(0);
							Folder bandoAppo = domandaGenerica.getFolderParent();
							if(bandoAppo.getProperty("jconon_call:has_macro_call") == null || bandoAppo.getPropertyValue("jconon_call:has_macro_call").equals(false)){
								bandoPadre = bandoAppo;
							}else{
								bandoPadre = bandoAppo.getFolderParent();
							}
							model.put("nameBando", bandoPadre.getName());
							model.put("matricole", matricole);
						}
					}
				}

				model.put("hasMoreItems", hasMoreItems);
				model.put("totalNumItems",totalNumItems);
				model.put("maxItemsPerPage", operationContext.getMaxItemsPerPage());
				model.put("activePage", getActivePage(BigInteger.valueOf(operationContext.getMaxItemsPerPage()),skipTo));

				return model;
	}

	protected Long getQuickTotalNumItems(Session cmisSession, String statement) {
		cmisAllOperationContext.setIncludeAllowableActions(false);
		statement = statement
				.replaceAll("select +\\*", "select score()")
				.replaceAll("order +by.*", "");
		return cmisSession.query(statement, false, cmisAllOperationContext)
				.getTotalNumItems();
	}

	protected Long getTotalNumItems(Session cmisSession, ItemIterable<QueryResult> queryResult, String statement) {
		return queryResult.getTotalNumItems();
	}

	private BigInteger getActivePage(BigInteger maxItemsPerPage, BigInteger skipTo) {
		return skipTo.divide(maxItemsPerPage);
	}

	private void addRelationshipToModel(OperationContext operationContext,
			List<Relationship> relationships,
			Map<String, Map<String, Object>> rel,
			RelationshipTypeParam relationshipTypeParam,
			List<String> relationshipName, List<String> relationshipField) {
		OperationContext localOperationContext = new OperationContextImpl(
				operationContext);
		if (relationshipField != null && !relationshipField.isEmpty()) {
			Set<String> fields = new HashSet<String>(relationshipField);
			fields.add(PropertyIds.SOURCE_ID);
			fields.add(PropertyIds.TARGET_ID);
			localOperationContext.setFilter(fields);
		}
		if (!relationshipTypeParam.equals(RelationshipTypeParam.cascade))
			localOperationContext
					.setIncludeRelationships(IncludeRelationships.NONE);
		for (Relationship relationship : relationships) {
			if (relationshipName != null
					&& !relationshipName.isEmpty()
					&& !relationshipName.contains(relationship.getType()
							.getId())) {
				continue;
			}
			if (!rel.containsKey(relationship.getType().getLocalName()))
				rel.put(relationship.getType().getLocalName(),
						new HashMap<String, Object>());
			Map<String, Object> result = rel.get(relationship.getType()
					.getLocalName());
			CmisObject cmisObject = relationship
					.getTarget(localOperationContext);
			addToPropertyMap(result, relationship.getSourceId().getId(),
					cmisObject);
			if (relationshipTypeParam.equals(RelationshipTypeParam.cascade))
				addRelationshipToModel(localOperationContext,
						cmisObject.getRelationships(), rel,
						relationshipTypeParam, relationshipName,
						relationshipField);
		}
	}

	private void addChildToModel(Session cmisSession, QueryResult result,
			Map<String, Map<String, Object>> relationships,

			List<String> relationshipName, List<String> relationshipField) {
		String objectId = (String) result
				.getPropertyValueById(PropertyIds.OBJECT_ID);
		CmisObject cmisObject = cmisSession.getObject(objectId);
		if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)) {
			Folder folder = (Folder) cmisObject;
			ItemIterable<CmisObject> childs = folder.getChildren();
			Map<String, Object> rels = new HashMap<String, Object>();
			for (CmisObject child : childs) {
				if (relationshipField != null && !relationshipField.isEmpty()) {
					Set<String> propertiesField = new HashSet<String>();
					for (String relField : relationshipField) {
						propertiesField.add(relField);
					}
				}
				if (relationshipName != null && !relationshipName.isEmpty()) {
					if (relationshipName.contains(child.getType().getId()))
						addToPropertyMap(rels, objectId, child);
				} else {
					addToPropertyMap(rels, objectId, child);
				}
			}
			if (!rels.isEmpty()) {
				if (relationships.containsKey("child")) {
					relationships.get("child").putAll(rels);
				} else {
					relationships.put("child", rels);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addToPropertyMap(Map<String, Object> properties, String key,
			Object value) {
		if (properties.containsKey(key)) {
			List<Object> values = (List<Object>) properties.get(key);
			values.add(value);
		} else {
			List<Object> values = new ArrayList<Object>();
			values.add(value);
			properties.put(key, values);
		}
	}

	private void addParentToModel(final Session cmisSession,
			QueryResult result,
			Map<String, Map<String, Object>> relationships,
			List<String> relationshipName, List<String> relationshipField) {
		final String objectId = (String) result
				.getPropertyValueById(PropertyIds.OBJECT_ID);
		try {
			// get parents from cache, if present
			List<Folder> parents = nodeParentsCache.get(objectId,
					new Callable<List<Folder>>() {
						@Override
						public List<Folder> call() throws Exception {
							FileableCmisObject cmisObject = (FileableCmisObject) cmisSession
									.getObject(objectId);
							List<Folder> parents = cmisObject.getParents();
							return parents;
						}
					});

			Map<String, Object> rels = new HashMap<String, Object>();
			for (CmisObject parent : parents) {
				if (relationshipField != null && !relationshipField.isEmpty()) {
					Set<String> propertiesField = new HashSet<String>();
					for (String relField : relationshipField) {
						propertiesField.add(relField);
					}
				}
				if (relationshipName != null && !relationshipName.isEmpty()) {
					if (relationshipName.contains(parent.getType().getId()))
						addToPropertyMap(rels, objectId, parent);
				} else {
					addToPropertyMap(rels, objectId, parent);
				}
			}
			if (!rels.isEmpty()) {
				if (relationships.containsKey("parent")) {
					relationships.get("parent").putAll(rels);
				} else {
					relationships.put("parent", rels);
				}
			}
		} catch (CmisPermissionDeniedException _ex) {
			LOGGER.warn("Parent is not visible", _ex);
		} catch (ExecutionException e) {
			LOGGER.warn("Cache Exception", e);
		}
	}

}