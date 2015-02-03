package it.cnr.cool.service.util;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;


public class AlfrescoDocument {
    static final SimpleDateFormat ALFRESCO_DATE_FORMAT = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm:ss");
    private final String dataCreazione;
    private final String createdBy;
    private final String name;
    private final String nodeRef;

    public AlfrescoDocument(CmisObject doc) {
        createdBy = doc.getPropertyValue(PropertyIds.CREATED_BY);
        dataCreazione = ALFRESCO_DATE_FORMAT.format(((GregorianCalendar) doc.getPropertyValue(PropertyIds.CREATION_DATE)).getTime());
        name = doc.getPropertyValue(PropertyIds.NAME);
        nodeRef = doc.getPropertyValue(PropertyIds.OBJECT_ID);
    }

    public AlfrescoDocument(QueryResult doc) {
        createdBy = doc.getPropertyValueById(PropertyIds.CREATED_BY);
        dataCreazione = ALFRESCO_DATE_FORMAT.format(((GregorianCalendar) doc.getPropertyValueById(PropertyIds.CREATION_DATE)).getTime());
        name = doc.getPropertyValueById(PropertyIds.NAME);
        nodeRef = doc.getPropertyValueById(PropertyIds.OBJECT_ID);
    }


    public String getDataCreazione() {
        return dataCreazione;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getName() {
        return name;
    }

    public String getNodeRef() {
        return nodeRef;
    }
}
