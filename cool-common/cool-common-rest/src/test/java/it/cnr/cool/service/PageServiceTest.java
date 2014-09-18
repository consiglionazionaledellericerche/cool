package it.cnr.cool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.cnr.cool.dto.CoolPage;

import java.util.Map;

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
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-rest-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class PageServiceTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PageServiceTest.class);

	@Autowired
	private PageService pageService;

	@Test
	public void testLoadPages() {
		Map<String, CoolPage> map = pageService.loadPages();
		LOGGER.info(map.keySet().toString());
		assertTrue(map.keySet().size() > 0);

		CoolPage page = map.get("home");
		LOGGER.info(page.toString());
		assertEquals(CoolPage.Authentication.USER, page.getAuthentication());
	}

}
