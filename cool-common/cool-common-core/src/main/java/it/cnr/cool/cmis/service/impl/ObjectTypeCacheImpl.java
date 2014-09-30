package it.cnr.cool.cmis.service.impl;

import java.util.concurrent.TimeUnit;

import org.apache.chemistry.opencmis.client.bindings.cache.TypeDefinitionCache;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ObjectTypeCacheImpl implements TypeDefinitionCache {
	private static final long serialVersionUID = 1L;
	private static Cache<String, TypeDefinition> cache;
	static {
		cache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.DAYS)
				.build();
	}

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
		return cache.getIfPresent(typeId);
	}

	@Override
	public void remove(String repositoryId, String typeId) {
		cache.invalidate(typeId);
	}

	@Override
	public void remove(String repositoryId) {
		cache.invalidateAll();
	}

	@Override
	public void removeAll() {
		//After session creation will call this method, and we do nothing
	}

}
