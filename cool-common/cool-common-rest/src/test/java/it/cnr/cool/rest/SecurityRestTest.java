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

package it.cnr.cool.rest;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.LoginException;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.mock.RequestUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class SecurityRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRestTest.class);

    private final static String USERNAME = "test.selezioni";
    private final static String NEWUSERNAME = "pippo.paperino";

    private static final String URL = "url";

    @Autowired
    private SecurityRest security;
    @Autowired
    private Proxy proxy;
    @Autowired
    private CMISService cmisService;

    @Autowired
    private UserService userService;

    @Autowired
    private CMISAuthenticatorFactory cmisAuthenticatorFactory;

    @Autowired
    private I18nService i18nService;

    @Test
    public void test0beforeClassSetup() {
        i18nService.setLocations(Collections.singletonList("i18n.labels"));
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addPreferredLocale(Locale.ITALIAN);
        MultivaluedMap<String, String> form = new MultivaluedHashMap<String, String>();
        form.add("userName", NEWUSERNAME);
        form.add("password", NEWUSERNAME);
        form.add("firstName", "PIPPO");
        form.add("lastName", "PAPERINO");
        form.add("email", "pippo.paperino@pluto.it");
        form.add("codicefiscale", "LNUFNC84P23H501L");

        Response outcome = security.doCreateUser(req, form, RequestUtils.LANG.it.name());
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), outcome.getStatus());

        form.remove("codicefiscale");
        form.add("codicefiscale", "SSSSSS73H02C495G");
        outcome = security.doCreateUser(req, form, RequestUtils.LANG.it.name());
        LOGGER.debug(outcome.getEntity().toString());
        assertEquals(Status.OK.getStatusCode(), outcome.getStatus());
    }

    @Test
    public void test1ConfirmAccountFail() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        Response response = security.confirmAccount(req, NEWUSERNAME, "INVALID_PIN", RequestUtils.LANG.it.name());
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    public void test2LoginFailed() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        Response response = security.login(req, res, NEWUSERNAME, NEWUSERNAME, "/home", null);
        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("failure=yes"));
    }

    @Test
    public void test3ConfirmAccount() throws Exception {
        CMISUser user = userService.loadUserForConfirm(NEWUSERNAME);
        MockHttpServletRequest req = new MockHttpServletRequest();
        Response response = security.confirmAccount(req, NEWUSERNAME, user.getPin(), RequestUtils.LANG.it.name());
        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
    }

    @Test
    public void test4Login() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        Response response = security.login(req, res, NEWUSERNAME, NEWUSERNAME, "/home", null);
        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").equals("/home"));
    }

    @Test
    public void test5ForgotPassword() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addPreferredLocale(Locale.ITALIAN);
        Response outcome = security.forgotPassword(req, NEWUSERNAME, Locale.getDefault().getLanguage());
        assertEquals(Status.OK.getStatusCode(), outcome.getStatus());

        String content = outcome.getEntity().toString();

        JSONObject json = new JSONObject(content);

        LOGGER.info(json.toString());

        assertEquals("pippo.paperino@pluto.it", json.getString("email"));
    }

    @Test
    public void test6ForgotPasswordFail() {
        HttpServletRequest req = new MockHttpServletRequest();
        Response outcome = security.forgotPassword(req, "doesNotExist", Locale.getDefault().getLanguage());
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), outcome.getStatus());

        String content = outcome.getEntity().toString();
        JSONObject json = new JSONObject(content);
        LOGGER.info(json.toString());

        assertTrue(json.has("error"));
    }


    @Test
    public void test7ChangePassword() {
        HttpServletRequest req = new MockHttpServletRequest();


        Response response = security.changePassword(req, USERNAME, "", "");

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        String content = response.getEntity().toString();

        JSONObject json = new JSONObject(content);
        LOGGER.info(json.toString());

        assertTrue(json.has("error"));

    }

    @Test
    public void test8ChangePasswordPin() throws CoolUserFactoryException {
        HttpServletRequest req = new MockHttpServletRequest();

        CMISUser user = userService.loadUserForConfirm(NEWUSERNAME);

        String pin = "123456";

        user.setPin(pin);
        userService.updateUser(user);

        LOGGER.debug("pin: " + pin);

        Response response = security.changePassword(req, NEWUSERNAME, pin, "AAA");

        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        String content = response.getEntity().toString();

        JSONObject json = new JSONObject(content);
        LOGGER.info(json.toString());

        assertTrue(json.has("fullName"));

    }

    @Test
    public void test9afterClassSetup() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        try {
            req.addHeader(CMISService.AUTHENTICATION_HEADER, cmisAuthenticatorFactory.getTicket("admin", "admin"));
        } catch (LoginException e) {
            LOGGER.error("error getting ticket", e);
        }
        req.setParameter(URL, "service/api/people/" + NEWUSERNAME);
        MockHttpServletResponse res = new MockHttpServletResponse();

        try {
            proxy.delete(req, res);
        } catch (Exception e) {
            LOGGER.error("error deleting user " + NEWUSERNAME, e);
        }

        assertEquals(HttpStatus.OK.value(), res.getStatus());

    }

    @Test
    public void testRegularExpTagHtml() {
        String pattern ="[^<>()'\"]*";
        assertTrue(!"<img>".matches(pattern));
        assertTrue(!"confirm()".matches(pattern));
        assertTrue(!"\"prova\"".matches(pattern));
        assertTrue("nodeRef=2bf7c9c5-7b46-4dcb;1.0&guest=true".matches(pattern));
        assertTrue("my-applications".matches(pattern));
    }
}
