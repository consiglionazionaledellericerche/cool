package it.cnr.cool.service.util;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;



public class AlfrescoDocument {
	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm:ss");
	private final String dataCreazione;
	private final String createdBy;
	private final String name;
	private final String nodeRef;

	public AlfrescoDocument(QueryResult doc) {	
		createdBy = doc.getPropertyValueById(PropertyIds.CREATED_BY);
		dataCreazione = DATEFORMAT.format(((GregorianCalendar) doc.getPropertyValueById(PropertyIds.CREATION_DATE)).getTime());
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
