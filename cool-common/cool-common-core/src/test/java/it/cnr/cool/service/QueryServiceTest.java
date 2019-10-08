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

package it.cnr.cool.service;

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.QueryResultImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class QueryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private static final String QUERY = "select * from cmis:folder";

    private static final String FOLDER_PATH = "/Data Dictionary";

    @Autowired
    private QueryService queryService;

    @Autowired
    private CMISService cmisService;

    @Test
    public void testQuery() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("q", QUERY);

        Session cmisSession = cmisService.getCurrentCMISSession(req);
        List<QueryResultImpl> resultSet = getResultSet(req, cmisSession);

        assertTrue(resultSet.size() > 0);

        LOGGER.debug(resultSet.toString());

    }

    @Test
    public void testQueryExportData() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        Session cmisSession = cmisService.createAdminSession();

        req.setParameter("q", "select * from jconon_application:folder");
        req.setParameter("exportData", Boolean.TRUE.toString());
        req.setParameter("fetchCmisObject", Boolean.TRUE.toString());
        req.setParameter("relationship", "parent");

        Map<String, Object> m = queryService.query(req, cmisSession);
        Map<String, String> matricole = (Map<String, String>) m.get("matricole");
        if (matricole != null)
            assertTrue(matricole.keySet().size() > 0);

    }

    @Test
    public void testQueryFetchCmisObject() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("q", QUERY);
        req.setParameter("fetchCmisObject", Boolean.TRUE.toString());

        Session cmisSession = cmisService.getCurrentCMISSession(req);

        List<QueryResultImpl> resultSet = getResultSet(req, cmisSession);

        assertTrue(resultSet.size() > 0);

        LOGGER.debug(resultSet.toString());

    }

    @Test
    public void testQueryFolder() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        Session session = cmisService.createAdminSession();
        String nodeRef = session.getObjectByPath(FOLDER_PATH).getId();

        req.setParameter("f", nodeRef);

        List<QueryResultImpl> resultSet = getResultSet(req, session);

        assertTrue(resultSet.size() > 0);

        LOGGER.debug(resultSet.toString());

    }

    private List<QueryResultImpl> getResultSet(MockHttpServletRequest req, Session session) {
        Map<String, Object> m = queryService.query(req, session);
        List<QueryResultImpl> resultSet = (List<QueryResultImpl>) m
                .get(QueryService.ITEMS);
        return resultSet;
    }

}
