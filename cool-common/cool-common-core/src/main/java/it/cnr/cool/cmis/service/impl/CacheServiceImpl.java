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
import org.springframework.extensions.webscripts.connector.User;

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

	public void register(GlobalCache globalCache){
		globalCaches.add(globalCache);
	}

	public void register(UserCache userCache){
		userCaches.add(userCache);
	}

	public List<Pair<String, String>> getCaches(User user, BindingSession session) {
		List<Pair<String, String>> caches = new ArrayList<Pair<String, String>>();
		if (user != null && !user.isGuest() && user instanceof CMISUser) {
			for (UserCache userCache : userCaches) {
				caches.add(new Pair<String, String>(userCache.name(), userCache.get((CMISUser)user, session)));
			}
		}
		return caches;
	}

	public List<Pair<String, String>> getPublicCaches() {
		List<Pair<String, String>> caches = new ArrayList<Pair<String, String>>();
		for (GlobalCache globalCache : globalCaches) {
			caches.add(new Pair<String, String>(globalCache.name(), globalCache
					.get()));
		}
		return caches;
	}

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
