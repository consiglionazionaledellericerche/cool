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

package it.cnr.cool.cmis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Repository
public class CmisAuthRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmisAuthRepository.class);

    public static final String USER = "user";

    private CMISService cmisService;

    @Autowired
    private CMISConfig cmisConfig;

    @Autowired
    private UserService userService;

    public CmisAuthRepository (CMISService cmisService) {
        this.cmisService = cmisService;
    }

    @Cacheable("cmis-session")
    public Session getSession(String ticket) {
        LOGGER.info("creating a new CMIS Session for ticket: " + ticket);
        return createSession("", ticket);
    }


    @Cacheable(value= USER, key="#ticket")
    public CMISUser getCachedCMISUser(String ticket, BindingSession bindingSession) {
        LOGGER.info("user not cached for ticket " + ticket);

        // who am I ?
        String link = cmisService.getBaseURL().concat("service/cnr/person/whoami");
        UrlBuilder url = new UrlBuilder(link);
        Response response = CmisBindingsHelper.getHttpInvoker(bindingSession).invokeGET(url, bindingSession);
        int status = response.getResponseCode();

        if (status == HttpStatus.SC_OK) {

            try {
                InputStreamReader reader = new InputStreamReader(response.getStream());
                CMISUser tempUser = new ObjectMapper().readValue(reader, CMISUser.class);

                String username = tempUser.getUserName();

                LOGGER.debug("retrieved user: " + username);

                CMISUser user = userService.loadUser(username, bindingSession);
                LOGGER.info("add to cache user " + (user == null ? "null" : user.getId()) + " to the cache having ticket = " + ticket);
                return user;

            } catch (IOException e) {
                LOGGER.warn("IO error retrieving user having ticket: " + ticket, e);
                return null;
            }

        } else {
            LOGGER.warn("HTTP Error retrieving user having ticket: " + ticket + ", status: " + status);
            return null;
        }

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
        String username = params.get(CMISConfig.CMISSessionParameter.GUEST_USERNAME.value());
        String password = params.get(CMISConfig.CMISSessionParameter.GUEST_PASSWORD.value());
        return createSession(username, password);
    }

    @Cacheable("admin-session")
    public Session getAdminSession() {
        LOGGER.info("creating a new admin session");
        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.CMISSessionParameter.ADMIN_USERNAME.value());
        String password = params.get(CMISConfig.CMISSessionParameter.ADMIN_PASSWORD.value());
        return createSession(username, password);
    }


    @Cacheable("admin-binding-session")
    public BindingSession getAdminBindingSession() {

        LOGGER.debug("creating an admin binding session");

        // TODO: vedi
        // it.cnr.cool.cmis.service.CMISService.createBindingSession()

        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.CMISSessionParameter.ADMIN_USERNAME.value());
        String password = params.get(CMISConfig.CMISSessionParameter.ADMIN_PASSWORD.value());

        return cmisService.createBindingSession(username, password);

    }


    @Cacheable("guest-binding-session")
    public BindingSession getGuestBindingSession() {

        LOGGER.debug("creating a guest binding session");

        Map<String, String> params = cmisConfig.getServerParameters();
        String username = params.get(CMISConfig.CMISSessionParameter.GUEST_USERNAME.value());
        String password = params.get(CMISConfig.CMISSessionParameter.GUEST_PASSWORD.value());
        return cmisService.createBindingSession(username, password);

    }


    private Session createSession(String username, String password)
    {
        LOGGER.debug("creating a new session for user " + username);
        return cmisService.getRepositorySession(username, password);
    }

}
