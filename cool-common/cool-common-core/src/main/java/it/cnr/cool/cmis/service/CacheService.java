package it.cnr.cool.cmis.service;

import it.cnr.cool.util.Pair;

import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.springframework.extensions.webscripts.connector.User;

public interface CacheService {
	public void register(GlobalCache globalCache);
	public void register(UserCache userCache);
	public List<Pair<String, String>> getCaches(User user, BindingSession session);
	public List<Pair<String, String>> getPublicCaches();
	public void clearCache();
}
