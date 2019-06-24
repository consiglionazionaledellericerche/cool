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

package it.cnr.cool.service;

import it.cnr.cool.MainTestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class I18nServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);
	@Autowired
	private I18nService i18nService;

	@Before
	public void initLocation() {
		i18nService.setLocations(Collections.singletonList("i18n.labels"));
	}

	@Test
	public void testGetLocale() {
		HttpServletRequest request = new MockHttpServletRequest();
		Locale locale = I18nService.getLocale(request, Locale.getDefault().getLanguage());
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
	public void testGetLabelEnglishCanada() {
		String label = i18nService.getLabel("welcome",
				Locale.CANADA);
		LOGGER.info(label);
		assertEquals("Welcome", label);
	}


    @Test
    public void testGetLabelUTF8() {
        String label = i18nService.getLabel("label.bpm.workflowPriority",
                Locale.ITALIAN);
        LOGGER.info(label);
        assertEquals("Priorità:", label);
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
