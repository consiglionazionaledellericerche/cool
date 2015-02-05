package it.cnr.cool.cmis.service;

import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.impl.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class CmisAuthRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmisAuthRepository.class);

    public static final String USER = "user";

    @Autowired
    private CMISService cmisService;

    @Cacheable("cmis-session")
    public Session getSession(String ticket) {
        LOGGER.info("creating a new CMIS Session for ticket: " + ticket);
        return cmisService.createSession("", ticket);
    }

    @CachePut(value= USER, key="#ticket")
    public CMISUser getCMISUser(CMISUser user, String ticket) {
        LOGGER.info("add to cache user " + (user == null ? "null" : user.getId()) + " to the cache having ticket = " + ticket);
        return user;
    }

    @Cacheable(value= USER)
    public CMISUser getCachedCMISUser(String ticket) {
        LOGGER.error("user not cached for ticket " + ticket + ", returning null");
        return null;

    }

    @Cacheable("binding-session")
    public SessionImpl getBindingSession(String ticket) {
        LOGGER.info("creating binding session for ticket " + ticket);
        return cmisService.createBindingSession("", ticket);
    }

}