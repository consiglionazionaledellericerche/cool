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

package it.cnr.cool.security;


public enum PermissionEnum {
	CMIS_ALL("cmis:all"),
    CMIS_READ("cmis:read"),
    CMIS_WRITE("cmis:write"),
	COORDINATOR("{http://www.alfresco.org/model/content/1.0}cmobject.Coordinator"),
    COLLABORATOR("{http://www.alfresco.org/model/content/1.0}cmobject.Collaborator"),
    CONTRIBUTOR("{http://www.alfresco.org/model/content/1.0}cmobject.Contributor"),
    EDITOR("{http://www.alfresco.org/model/content/1.0}cmobject.Editor"),
    CONSUMER("{http://www.alfresco.org/model/content/1.0}cmobject.Consumer"),
    ALFRESCO_ALL("{http://www.alfresco.org/model/security/1.0}All.All");

    private final String value;

    PermissionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
	
}
