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
