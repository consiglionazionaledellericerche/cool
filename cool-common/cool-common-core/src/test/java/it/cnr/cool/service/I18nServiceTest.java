package it.cnr.cool.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

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
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class I18nServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);
	@Autowired
	private I18nService i18nService;

	@Test
	public void testGetLocale() {
		HttpServletRequest request = new MockHttpServletRequest();
		Locale locale = I18nService.getLocale(request);
		LOGGER.info(locale.toString());
		assertEquals("en", locale.getLanguage());

	}

	@Test
	public void testGetTemplate() {

		String path = "/fake.html";
		String tpl = i18nService
				.getTemplate(
						path,
						Locale.ITALIAN);

		LOGGER.debug(tpl);
		assertEquals(path + "_it.ftl", tpl);

	}

	@Test
	public void testGetLabel() {
		String label = i18nService.getLabel("welcome",
				Locale.ITALIAN);
		LOGGER.info(label);
		assertEquals("Benvenuto", label);
	}

	@Test
	public void testGetLabelUnimplementedLocale() {
		String label = i18nService.getLabel("welcome",
				Locale.GERMAN);
		LOGGER.info(label);
		assertEquals("Welcome", label);
	}

	@Test
	public void testGetLabels() throws IOException {

		Properties labels = i18nService.getLabels(Locale.ITALIAN,
				"my-special-uri");

		LOGGER.info(labels.toString());
	}


}
