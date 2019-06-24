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

package it.cnr.cool.service.typestree;

import it.cnr.cool.MainTestContext;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class TypesTreeTest {

	@Autowired
	private TypesTreeService typesTreeService;
	
	@Test
	public void testGetTree() throws IOException {
		
		List<String> seeds = new ArrayList<String>();
		seeds.add("F:jconon_call:folder");
		seeds.add("F:jconon_application:folder");
		seeds.add("D:jconon_attachment:document");
		
		List<Type> tree = typesTreeService.getTree(seeds);
		assertTrue(tree.size() >= seeds.size());

	}
	
	@Test
	public void testGetTreeWrongSeeds() throws IOException {
		
		List<String> seeds = new ArrayList<String>();
		seeds.add("alpha");
		seeds.add("beta");
		seeds.add("gamma");
		
		try {
			List<Type> tree = typesTreeService.getTree(seeds);			
			assertFalse(tree.size() >= seeds.size());	
		} catch (CmisObjectNotFoundException exp) {
			// Exception caught: success
		}
		
	}
	
}
