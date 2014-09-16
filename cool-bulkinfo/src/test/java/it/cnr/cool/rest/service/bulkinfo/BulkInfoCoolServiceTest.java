package it.cnr.cool.rest.service.bulkinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.cnr.bulkinfo.cool.BulkInfoCool;
import it.cnr.bulkinfo.exception.BulkInfoException;
import it.cnr.bulkinfo.exception.BulkinfoKindException;
import it.cnr.bulkinfo.exception.BulkinfoNameException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.BulkInfoCoolService;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-bulkinfo-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class BulkInfoCoolServiceTest {

	private static String DEFAULT_RESOURCE_CMIS_TYPE = "cmis:folder";
	private static String DEFAULT_RESOURCE_BULKINFO_XML = "F:jconon_call:folder";
	@Autowired
	private CMISService cmisService;
	@Autowired
	private BulkInfoCoolService bulkInfoCoolService;


	@Test
	public void testHappyCaseFromCmisType() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_CMIS_TYPE, "form", "default", null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testHappyCaseWithAspects() { // for coverage
		try {
			Session cmisSession = cmisService.createAdminSession();
			bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "form", "default", null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testHappyCaseWithExtends() { //for coverage
		try {
			Session cmisSession = cmisService.createAdminSession();
			bulkInfoCoolService.getView(cmisSession, "D_jconon_attachment_document_mono", "form", "default", null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetWrongName() throws BulkInfoException, BulkinfoKindException, BulkinfoNameException {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> view = bulkInfoCoolService.getView(cmisSession, "wrong", "form", "default", null);
			assertNull(view.get("bulkInfo"));
			assertNull(bulkInfoCoolService.find("wrong"));
	}

	@Test
	public void testGetDefaultFormKindWrong() {
		Session cmisSession = cmisService.createAdminSession();
		try {
			bulkInfoCoolService.getView(cmisSession, "F:jconon_call:folder", "wrong", "default", null);
			fail();
		} catch(Exception e) {
			assertTrue(e instanceof BulkinfoKindException);
		}
	}

	@Test
	public void testGetDefaultFormNameNull() {
		Session cmisSession = cmisService.createAdminSession();
		try {
			bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "form", null, null);
			fail(); //exception expected
		} catch(Exception e) {
			assertTrue(e instanceof BulkinfoNameException);
		}
	}
	
	@Test
	public void testGetDefaultFormNameEmpty() {
		Session cmisSession = cmisService.createAdminSession();
		try {
			bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "form", "", null);
			fail();
		} catch(Exception e) {
			assertTrue(e instanceof BulkinfoNameException);
		}
	}

	@Test
	public void testGetDefaultFormKindNull() {
		Session cmisSession = cmisService.createAdminSession();
		try {
			bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, null, "default", null);
			fail(); //exception expected
		} catch(Exception e) {
			assertTrue(e instanceof BulkinfoKindException);
		}
	}

	@Test
	public void testGetDefaultFormNotNull() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "form", "default", null);
			assertTrue(biMap != null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultFormFromCmisType() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_CMIS_TYPE, "form", "default", null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
			assertEquals(DEFAULT_RESOURCE_CMIS_TYPE, bi.getCmisTypeName());

			//get all column sets
			assertTrue(bi.getForms().containsKey("default"));
			//count columns for a random column set

			//get all forms
			assertEquals(1, bi.getForms().size());
			assertTrue(bi.getForms().containsKey("default"));
			assertTrue(bi.getForms().get("default") != null);

			//get a print form
			assertTrue(bi.getPrintForms().containsKey("default"));
			assertTrue(bi.getPrintForms().get("default") != null);

			//get a free search set
			assertTrue(bi.getFreeSearchSets().containsKey("default"));
			assertTrue(bi.getFreeSearchSets().get("default") != null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultFormTestValues() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "form", "default", null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
			assertEquals("F:jconon_call:folder", bi.getCmisTypeName());

			//get all column sets
			assertTrue(bi.getColumnSets().containsKey("default"));
			assertTrue(bi.getColumnSets().containsKey("home"));
			//count columns for a random column set
			assertTrue(bi.getColumnSet("home").size() == 19);

			//get all forms
			assertTrue(bi.getForms().size() == 10);
			assertTrue(bi.getForms().containsKey("default"));
			assertTrue(bi.getForms().containsKey("affix_sezione_1"));
			assertTrue(bi.getForms().containsKey("affix_sezione_2"));
			assertTrue(bi.getForms().containsKey("affix_sezione_3"));
			assertTrue(bi.getForms().containsKey("affix_sezione_permessi"));
			assertTrue(bi.getForms().containsKey("affix_sezione_commissione"));
			assertTrue(bi.getForms().containsKey("affix_sezione_allegati"));
			assertTrue(bi.getForms().containsKey("create_child_call"));
			assertTrue(bi.getForms().containsKey("filters"));
			assertTrue(bi.getForms().containsKey("all-filters"));

			//count properties for a random form
			assertTrue(bi.getForm("filters").size() == 5);
			//non posso testare i singoli formFieldProperty
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultColumn() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "column", "default", null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
			assertEquals("F:jconon_call:folder", bi.getCmisTypeName());

			//get all column sets
			assertTrue(bi.getColumnSets().containsKey("default"));
			assertTrue(bi.getColumnSets().containsKey("home"));
			//count columns for a random column set
			assertTrue(bi.getColumnSet("home").size() == 19);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultFind() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "find", "default", null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
			assertEquals("F:jconon_call:folder", bi.getCmisTypeName());

			//get all column sets
			assertTrue(bi.getColumnSets().containsKey("default"));
			assertTrue(bi.getColumnSets().containsKey("home"));
			//count columns for a random column set
			assertTrue(bi.getColumnSet("home").size() == 19);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultFreeSearchSet() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, DEFAULT_RESOURCE_BULKINFO_XML, "find", "default", null);
			BulkInfoCool bi = (BulkInfoCool) biMap.get("bulkInfo");
			assertTrue( bi != null);
			assertEquals("F:jconon_call:folder", bi.getCmisTypeName());

			//get all column sets
			assertTrue(bi.getFreeSearchSets().containsKey("default"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetFromChache() {
		try {
			Session cmisSession = cmisService.createAdminSession();
			Map<String, Object> biMap = bulkInfoCoolService.getView(cmisSession, "F:jconon_call:folder", "find", "default", null);
			assertNotNull(biMap.get("bulkInfo"));
			
			// accertarsi che nell'output debug, la costruzione di F_jconon_call_folder sia presente una sola volta
			System.out.println("No output after this point");
			biMap = bulkInfoCoolService.getView(cmisSession, "F:jconon_call:folder", "find", "default", null);
			System.out.println("Bulkinfo retrieved. There should be NO output after the previous statement");

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
