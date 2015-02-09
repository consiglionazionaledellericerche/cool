package it.cnr.cool.service;

import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.QueryResultImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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
		req.getSession().setAttribute(CMISService.DEFAULT_SERVER, cmisSession);

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

        req.getSession().setAttribute(CMISService.DEFAULT_SERVER, session);

		req.setParameter("f", nodeRef);

		List<QueryResultImpl> resultSet = getResultSet(req, session);

		assertTrue(resultSet.size() > 0);

		LOGGER.debug(resultSet.toString());

	}

	private List<QueryResultImpl> getResultSet(MockHttpServletRequest req, Session session) {
        Map<String, Object> m = queryService.query(req, session);
		List<QueryResultImpl> resultSet = (List<QueryResultImpl>) m
				.get("models");
		return resultSet;
	}

}
