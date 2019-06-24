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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MainTestContext.class})
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
		request.addParameter("q", "select * from jconon_call:folder");
		request.addParameter("fetchCmisObject", "false");
		request.addParameter("relationship", "parent");
		request.addParameter("calculateTotalNumItems", Boolean.TRUE.toString());

		Response response = search.query(request);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		final Long totalNumItems = Optional.ofNullable(response.getEntity())
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.flatMap(map -> Optional.ofNullable(map.get("totalNumItems")))
				.filter(Long.class::isInstance)
				.map(Long.class::cast)
				.orElse(Long.valueOf(0));

		assertTrue(totalNumItems > 0);

	}

}
