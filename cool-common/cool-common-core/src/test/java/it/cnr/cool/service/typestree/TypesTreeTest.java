package it.cnr.cool.service.typestree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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
