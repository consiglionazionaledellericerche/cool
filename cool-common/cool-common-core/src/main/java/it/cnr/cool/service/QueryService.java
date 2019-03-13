package it.cnr.cool.service;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.service.model.RelationshipTypeParam;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class QueryService {

    public static final String ITEMS = "items";
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);
    @Autowired
    private OperationContext cmisDefaultOperationContext;

    @Autowired
    private OperationContext cmisAllOperationContext;

    public Map<String, Object> query(HttpServletRequest req, Session cmisSession) {

        // request params
        String statement = req.getParameter("q");
        String folder = req.getParameter("f");
        String objectRel = req.getParameter("objectRel");
        String maxItems = req.getParameter("maxItems");
        Boolean calculateTotalNumItems = Boolean.valueOf(req
                .getParameter("calculateTotalNumItems"));
        Boolean fetchCmisObject = Boolean.valueOf(req
                .getParameter("fetchCmisObject"));
        String parameter_relationship = req.getParameter("relationship");
        String[] parameter_relationship_name = req
                .getParameterValues("relationship.name");
        String[] parameter_relationship_field = req
                .getParameterValues("relationship.field");
        String parameter_skip_count = req.getParameter("skipCount");
        String orderBy = req.getParameter("orderBy");
        Boolean allColumns = Boolean.valueOf(req.getParameter("allColumns"));

        return query(cmisSession, parameter_relationship,
                parameter_relationship_name, maxItems, folder,
                objectRel, fetchCmisObject, calculateTotalNumItems, statement,
                parameter_relationship_field, parameter_skip_count, orderBy, allColumns);

    }

    public Map<String, Object> documentVersion(Session cmisSession, String nodeRef) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<Document> versions = ((Document) cmisSession.getObject(nodeRef)).getAllVersions();
        model.put(ITEMS, versions);
        model.put("hasMoreItems", false);
        model.put("totalNumItems", versions.size());
        model.put("maxItemsPerPage", 1000);
        model.put("activePage", 0);
        return model;
    }


    private Map<String, Object> query(Session cmisSession,
                                      String parameter_relationship,
                                      String[] parameter_relationship_name, String maxItems,
                                      String folder, String objectRel,
                                      Boolean fetchCmisObject, Boolean calculateTotalNumItems,
                                      String statement, String[] parameter_relationship_field,
                                      String parameter_skip_count, String orderBy, boolean allColumns) {


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
        if (parameter_skip_count != null) {
            skipTo = BigInteger.valueOf(Long.valueOf(parameter_skip_count));
        }
        long s = skipTo.compareTo(BigInteger.ZERO) < 0 ? BigInteger.ZERO
                .longValue() : skipTo.longValue();

        Long totalNumItems;
        boolean hasMoreItems;

        if (folder != null) {

            operationContext.setOrderBy("cmis:baseTypeId DESC"
                    + (orderBy != null && orderBy.length() > 0 ? ","
                    + orderBy : ""));

            if (!allColumns) {
                Set<String> columns = new HashSet<String>();
                columns.add(PropertyIds.OBJECT_ID);
                columns.add(PropertyIds.OBJECT_TYPE_ID);
                columns.add(PropertyIds.BASE_TYPE_ID);
                columns.add(PropertyIds.NAME);
                columns.add(PropertyIds.LAST_MODIFICATION_DATE);
                columns.add(PropertyIds.LAST_MODIFIED_BY);
                columns.add(PropertyIds.CONTENT_STREAM_LENGTH);
                columns.add(PropertyIds.CONTENT_STREAM_MIME_TYPE);
                columns.add(CoolPropertyIds.ALFCMIS_NODEREF.value());
                operationContext.setFilter(columns);
            }

            operationContext.setIncludeAllowableActions(true);
            operationContext.setIncludePathSegments(false);
            ItemIterable<CmisObject> items = ((Folder) cmisSession
                    .getObject(folder, operationContext))
                    .getChildren(operationContext);
            items = items.skipTo(s);
            items = items.getPage();

            totalNumItems = items.getTotalNumItems();


            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (CmisObject item : items) {
                result.add(convertToProperties(item));
            }
            hasMoreItems = items.getHasMoreItems();
            model.put(ITEMS, result);
        } else if (objectRel != null) {
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
            model.put(ITEMS, rels);
        } else {
            ItemIterable<QueryResult> queryResult = cmisSession.query(
                    statement, false, operationContext);

            queryResult = queryResult.skipTo(s);
            queryResult = queryResult.getPage();
            hasMoreItems = queryResult.getHasMoreItems();
            totalNumItems = getTotalNumItems(cmisSession, queryResult, statement);
            Long maxItemsPerPage = Long.valueOf(operationContext.getMaxItemsPerPage());

            boolean isEven = s + maxItemsPerPage == totalNumItems;
            if (isEven && hasMoreItems) {
                totalNumItems = Long.MIN_VALUE;
            }

            if (calculateTotalNumItems && totalNumItems == Long.MIN_VALUE) {
                totalNumItems = getQuickTotalNumItems(cmisSession, statement);
            }
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("There are " + totalNumItems
                        + " documents in repository");

            List<Map<String, Object>> recentSubmission = new ArrayList<Map<String, Object>>();
            Map<String, CmisObject> parents = new HashMap<String, CmisObject>();
            for (QueryResult result : queryResult) {
                Map<String, List<Map<String, Object>>> relationships = new HashMap<String, List<Map<String, Object>>>();
                Map<String, Object> properties = new HashMap<String, Object>();
                if (fetchCmisObject) {
                    String nodeRef = result.getPropertyValueById(PropertyIds.OBJECT_ID);
                    CmisObject cmisObject = cmisSession.getObject(nodeRef, operationContext);
                    properties.putAll(convertToProperties(cmisObject));
                } else {
                    properties.putAll(convertToProperties(result));
                }
                if (relationshipTypeParam.isSearchRelationship())
                    addRelationshipToModel(operationContext,
                            result.getRelationships(), relationships,
                            relationshipTypeParam, relationshipName,
                            relationshipField);
                if (relationshipTypeParam.isChildRelationship())
                    addChildToModel(cmisSession, result, relationships,
                            relationshipName, relationshipField);
                if (relationshipTypeParam.isParentRelationship()) {
                    addParentToModel(cmisSession, result, relationships,
                            relationshipName, relationshipField, parents);
                }
                if (!relationships.isEmpty())
                    properties.putAll(Collections.singletonMap("relationships", relationships));

                recentSubmission.add(properties);
            }
            model.put(ITEMS, recentSubmission);
        }

        model.put("hasMoreItems", hasMoreItems);
        model.put("totalNumItems", totalNumItems);
        model.put("maxItemsPerPage", operationContext.getMaxItemsPerPage());
        model.put("activePage", getActivePage(BigInteger.valueOf(operationContext.getMaxItemsPerPage()), skipTo));

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
                                        Map<String, List<Map<String, Object>>> rel,
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
                rel.put(relationship.getType().getLocalName(), new ArrayList());
            List<Map<String, Object>> result = rel.get(relationship.getType().getLocalName());
            CmisObject cmisObject = relationship.getTarget(localOperationContext);
            result.add(convertToProperties(cmisObject));
            if (relationshipTypeParam.equals(RelationshipTypeParam.cascade))
                addRelationshipToModel(localOperationContext,
                        cmisObject.getRelationships(), rel,
                        relationshipTypeParam, relationshipName,
                        relationshipField);
        }
    }

    private Map<String, Object> convertToProperties(QueryResult queryResult) {
        final HashMap<String, Object> collect1 = queryResult.getProperties()
                .stream()
                .collect(HashMap::new,
                        (m, c) -> m.put(c.getId(), getValue(c.getValues())),
                        (m, u) -> {
                        });
        collect1.put("allowableActions", Optional.ofNullable(queryResult.getAllowableActions())
                .map(AllowableActions::getAllowableActions)
                .orElse(Collections.EMPTY_SET)
                .stream()
                .collect(Collectors.toList()));
        return collect1;
    }

    private Map<String, Object> convertToProperties(CmisObject cmisObject) {
        final HashMap<String, Object> collect1 = cmisObject.getProperties()
                .stream()
                .collect(HashMap::new,
                        (m, c) -> m.put(c.getId(), getValue(c.getValues())),
                        (m, u) -> {
                        });
        collect1.put("allowableActions", Optional.ofNullable(cmisObject.getAllowableActions())
                .map(AllowableActions::getAllowableActions)
                .orElse(Collections.EMPTY_SET)
                .stream()
                .collect(Collectors.toList()));
        return collect1;
    }

    private Object getValue(List<?> result) {
        return Optional.ofNullable(result)
                .filter(objects -> !objects.isEmpty())
                .map(objects -> {
                    if (objects.size() == 1) {
                        return getValue(objects.get(0));
                    } else {
                        return objects
                                .stream()
                                .map(o -> getValue(o))
                                .collect(Collectors.toList());
                    }
                }).orElse(null);
    }

    private Object getValue(Object result) {
        return Optional.ofNullable(result)
                .map(o -> {
                    if (Optional.of(o).filter(GregorianCalendar.class::isInstance).isPresent()) {
                        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ITALIAN).format(
                                Optional.of(o)
                                        .filter(GregorianCalendar.class::isInstance)
                                        .map(GregorianCalendar.class::cast)
                                        .map(GregorianCalendar::toZonedDateTime)
                                        .orElse(null)
                        );
                    }
                    return o;
                }).orElse(null);
    }


    private void addChildToModel(Session cmisSession, QueryResult result,
                                 Map<String, List<Map<String, Object>>> relationships,

                                 List<String> relationshipName, List<String> relationshipField) {
        String objectId = result
                .getPropertyValueById(PropertyIds.OBJECT_ID);
        CmisObject cmisObject = cmisSession.getObject(objectId);
        if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)) {
            Folder folder = (Folder) cmisObject;
            ItemIterable<CmisObject> childs = folder.getChildren();
            List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
            for (CmisObject child : childs) {
                if (relationshipField != null && !relationshipField.isEmpty()) {
                    Set<String> propertiesField = new HashSet<String>();
                    for (String relField : relationshipField) {
                        propertiesField.add(relField);
                    }
                }
                if (relationshipName != null && !relationshipName.isEmpty()) {
                    if (relationshipName.contains(child.getType().getId()))
                        rels.add(convertToProperties(child));
                } else {
                    rels.add(convertToProperties(child));
                }
            }
            if (!rels.isEmpty()) {
                if (relationships.containsKey("child")) {
                    relationships.get("child").addAll(rels);
                } else {
                    relationships.put("child", rels);
                }
            }
        }
    }

    private void addParentToModel(final Session cmisSession,
                                  QueryResult result,
                                  Map<String, List<Map<String, Object>>> relationships,
                                  List<String> relationshipName, List<String> relationshipField, Map<String, CmisObject> parents) {
        String objectId = result
                .getPropertyValueById(PropertyIds.OBJECT_ID);
        String parentId = result
                .getPropertyValueById(PropertyIds.PARENT_ID);
        try {
            CmisObject parent;
            if (parentId == null) {
                CmisObject cmisObject = cmisSession.getObject(objectId);
                parentId = cmisObject.getPropertyValue(PropertyIds.PARENT_ID);
                if (parentId == null && cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
                    List<Folder> parentsFolder = ((Document) cmisObject).getParents();
                    if (parentsFolder.size() > 1)
                        LOGGER.warn("Object {} have multiple parents", objectId);
                    parentId = parentsFolder.get(0).getId();
                }
            }
            if (parents.containsKey(parentId))
                parent = parents.get(parentId);
            else {
                parent = cmisSession.getObject(parentId);
                parents.put(parentId, parent);
            }
            List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
            if (relationshipField != null && !relationshipField.isEmpty()) {
                Set<String> propertiesField = new HashSet<String>();
                for (String relField : relationshipField) {
                    propertiesField.add(relField);
                }
            }
            if (relationshipName != null && !relationshipName.isEmpty()) {
                if (relationshipName.contains(parent.getType().getId()))
                    rels.add(convertToProperties(parent));
            } else {
                rels.add(convertToProperties(parent));
            }
            if (!rels.isEmpty()) {
                if (relationships.containsKey("parent")) {
                    relationships.get("parent").addAll(rels);
                } else {
                    relationships.put("parent", rels);
                }
            }
        } catch (CmisPermissionDeniedException _ex) {
            LOGGER.warn("Parent is not visible", _ex);
        }
    }
}