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

package it.cnr.cool.cmis.service.impl;

import org.apache.chemistry.opencmis.client.bindings.cache.TypeDefinitionCache;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

import java.util.HashMap;
import java.util.Map;


public class ObjectTypeCacheImpl implements TypeDefinitionCache {
	private static final long serialVersionUID = 1L;

    private static Map<String, TypeDefinition> cache = new HashMap<String, TypeDefinition>();

	@Override
	public void initialize(BindingSession session) {
		/*
		 * Cache is global not for session 
		 */
	}

	@Override
	public void put(String repositoryId, TypeDefinition typeDefinition) {
		cache.put(typeDefinition.getId(), typeDefinition);
	}

	@Override
	public TypeDefinition get(String repositoryId, String typeId) {

        return cache.get(typeId);
	}

	@Override
	public void remove(String repositoryId, String typeId) {
        cache.remove(typeId);
	}

	@Override
	public void remove(String repositoryId) {
        cache.clear();
	}

	@Override
	public void removeAll() {
		//After session creation will call this method, and we do nothing
	}

}
