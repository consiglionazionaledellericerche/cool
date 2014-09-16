package it.cnr.cool.cmis.service.impl;

import it.cnr.cool.cmis.service.Cache;
import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.GlobalCache;
import it.cnr.cool.cmis.service.UserCache;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.Pair;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CacheServiceImpl implements CacheService, InitializingBean{

	private List<GlobalCache> globalCaches;
	private List<UserCache> userCaches;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CacheServiceImpl.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		globalCaches = new ArrayList<GlobalCache>();
		userCaches = new ArrayList<UserCache>();
	}

	@Override
	public void register(GlobalCache globalCache){
		globalCaches.add(globalCache);
	}

	@Override
	public void register(UserCache userCache){
		userCaches.add(userCache);
	}

	@Override
	public List<Pair<String, String>> getCaches(CMISUser user,
			BindingSession session) {
		List<Pair<String, String>> caches = new ArrayList<Pair<String, String>>();
		if (user != null && !user.isGuest()) {
			for (UserCache userCache : userCaches) {
				caches.add(new Pair<String, String>(userCache.name(), userCache.get(user, session)));
			}
		}
		return caches;
	}

	@Override
	public List<Pair<String, String>> getPublicCaches() {
		List<Pair<String, String>> caches = new ArrayList<Pair<String, String>>();
		for (GlobalCache globalCache : globalCaches) {
			caches.add(new Pair<String, String>(globalCache.name(), globalCache
					.get()));
		}
		return caches;
	}

	@Override
	public void clearCache(){
		LOGGER.debug("Reset cache service");
		for (Cache cache : globalCaches) {
			cache.clear();
		}
		for (Cache cache : userCaches) {
			cache.clear();
		}
	}
}
