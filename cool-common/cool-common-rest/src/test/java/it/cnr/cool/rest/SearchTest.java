package it.cnr.cool.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.service.CMISService;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SearchTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchTest.class);

	@Autowired
	private Search search;
	
	@Autowired
	private CMISService cmisService;

	@Test
	public void testProcessRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		session.setAttribute(CMISService.DEFAULT_SERVER, cmisService.createAdminSession());
		request.addParameter("q", "select * from jconon_call:folder");
		request.addParameter("calculateTotalNumItems", Boolean.TRUE.toString());

		Response response = search.query(request);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		JSONObject json = new JSONObject(response.getEntity().toString());
		LOGGER.info(json.toString());
		assertTrue(json.getInt("totalNumItems") > 0);

	}

}
