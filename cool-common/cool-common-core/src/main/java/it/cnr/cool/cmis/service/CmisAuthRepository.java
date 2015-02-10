package it.cnr.cool.cmis.service;

import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class CmisAuthRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmisAuthRepository.class);

    public static final String USER = "user";

    @Autowired
    private CMISService cmisService;

    @Autowired
    private CMISConfig cmisConfig;

    @Cacheable("cmis-session")
    public Session getSession(String ticket) {
        LOGGER.info("creating a new CMIS Session for ticket: " + ticket);
        return createSession("", ticket);
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
    public BindingSession getBindingSession(String ticket) {
        LOGGER.info("creating binding session for ticket " + ticket);
        return cmisService.createBindingSession("", ticket);
    }


    @Cacheable("guest-session")
    public Session getGuestSession() {
        LOGGER.info("creating a new guest session");
        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.GUEST_USERNAME);
        String password = params.get(CMISConfig.GUEST_PASSWORD);
        return createSession(username, password);
    }

    @Cacheable("admin-session")
    public Session getAdminSession() {
        LOGGER.info("creating a new admin session");
        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.ADMIN_USERNAME);
        String password = params.get(CMISConfig.ADMIN_PASSWORD);
        return createSession(username, password);
    }


    @Cacheable("adming-binding-session")
    public BindingSession getAdminBindingSession() {

        LOGGER.debug("creating an admin binding session");

        // TODO: vedi
        // it.cnr.cool.cmis.service.CMISService.createBindingSession()

        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.ADMIN_USERNAME);
        String password = params.get(CMISConfig.ADMIN_PASSWORD);

        return cmisService.createBindingSession(username, password);

    }


    @Cacheable("guest-binding-session")
    public BindingSession getGuestBindingSession() {

        LOGGER.debug("creating a guest binding session");

        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.GUEST_USERNAME);
        String password = params.get(CMISConfig.GUEST_PASSWORD);
        return cmisService.createBindingSession(username, password);

    }


    private Session createSession(String username, String password)
    {
        LOGGER.debug("creating a new session for user " + username);
        return cmisService.getRepositorySession(username, password);
    }

}