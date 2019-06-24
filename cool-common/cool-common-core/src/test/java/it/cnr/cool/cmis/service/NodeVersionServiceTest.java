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
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class NodeVersionServiceTest {

	@Autowired
	private CMISService cmisService;

	@Autowired
	private NodeVersionService nodeVersionService;

	private static final String OBJECT_PATH = "/Data Dictionary/RSS Templates/RSS_2.0_recent_docs.ftl";

	@Test
	public void testAddAutoVersionDocument() {
		Document doc = getDocument();

		nodeVersionService.addAutoVersion(doc, true);
		nodeVersionService.addAutoVersion(doc, false);
	}

	@Test
	public void testAddAutoVersionDocumentBoolean() {
		Document doc = getDocument();
		nodeVersionService.addAutoVersion(doc);
		nodeVersionService.addAutoVersion(doc, false);
	}

	private Document getDocument() {
		Session cmisSession = cmisService.createAdminSession();
		Document doc = (Document) cmisSession.getObjectByPath(OBJECT_PATH);
		return doc;
	}

}
