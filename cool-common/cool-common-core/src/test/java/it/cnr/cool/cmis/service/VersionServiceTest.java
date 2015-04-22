package it.cnr.cool.cmis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class VersionServiceTest {

	@Autowired
	private VersionService versionService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(VersionServiceTest.class);


	@Test
	public void testGetVersion() {
		String v = versionService.getVersion();
		LOGGER.info(v);
		assertTrue(v.startsWith("UNKNOWN"));
	}

	@Test
	public void testIsProduction() {
		boolean p = versionService.isProduction();
		LOGGER.info(p ? "produzione" : "sviluppo");
		assertFalse(p);
	}

}
