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
        nodeRef = doc.getPropertyValue("alfcmis:nodeRef");
    }

    public AlfrescoDocument(QueryResult doc) {
        createdBy = doc.getPropertyValueById(PropertyIds.CREATED_BY);
        dataCreazione = ALFRESCO_DATE_FORMAT.format(((GregorianCalendar) doc.getPropertyValueById(PropertyIds.CREATION_DATE)).getTime());
        name = doc.getPropertyValueById(PropertyIds.NAME);
        nodeRef = doc.getPropertyValueById("alfcmis:nodeRef");
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
