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

package it.cnr.cool.security.service;

import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.impl.alfresco.CMISAuthority;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;

import java.util.List;

public interface GroupService {
	CMISGroup createGroup(String group_name, String display_name, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISGroup loadGroup(String group_name, BindingSession cmisSession) throws CoolUserFactoryException;
	List<CMISAuthority> children(String group_name, BindingSession cmisSession) throws CoolUserFactoryException;
}
