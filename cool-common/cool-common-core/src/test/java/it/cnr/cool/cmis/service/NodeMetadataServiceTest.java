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

import it.cnr.cool.MainTestContext;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class NodeMetadataServiceTest {

	@Autowired
	private NodeMetadataService nodeMetadataService;

	@Autowired
	private CMISService cmisService;

	private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeMetadataServiceTest.class);

	@Test
	public void testUpdateObjectProperties() throws ParseException {

		Session cmisSession = cmisService.createAdminSession();
		CmisObject object = cmisSession.getObjectByPath(OBJECT_PATH);

		LOGGER.info(object.getId());

		Map<String, Object> reqProperties = new HashMap<String, Object>();
		reqProperties.put(PropertyIds.OBJECT_ID, object.getId());
		reqProperties.put(PropertyIds.OBJECT_TYPE_ID, object.getType().getId());

		HttpServletRequest request = new MockHttpServletRequest();

		CmisObject doc = nodeMetadataService.updateObjectProperties(
				reqProperties, cmisSession,
				request);

		assertEquals(doc.getId(), object.getId());

	}


}
