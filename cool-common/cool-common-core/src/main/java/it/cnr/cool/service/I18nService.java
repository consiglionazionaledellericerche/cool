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

import it.cnr.cool.repository.I18nRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
@Service
public class I18nService {

	private static final String EXTENSION = ".ftl";
	private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Autowired
    private I18nRepository i18nRepository;


    private List<String> locations = new ArrayList<String>();

	public static Locale getLocale(HttpServletRequest request, String lang) {
		if (lang == null)
			return request.getLocale();
		else
			return new Locale(lang);
	}

	public String getTemplate(String path, Locale locale) {

		String templateId;

		// look for specific (localized) template
		String nameLocalized = path + "_" + locale.getLanguage() + EXTENSION;
		String nameGeneric = path + EXTENSION;

		if (getStream(nameLocalized) != null) {
			LOGGER.debug("found " + nameLocalized);
			templateId = nameLocalized;
		} else if (getStream(nameGeneric) != null) {
			LOGGER.debug("found " + nameGeneric);
			templateId = nameGeneric;
		} else {
			LOGGER.warn("unable to find " + nameLocalized + " or " + nameGeneric);
			templateId = null;
		}

		return templateId;
	}
	
	public String getLabel(String id, String uri, Locale locale, Object ... params) {
		LOGGER.debug("looking for label + " + id);
		Properties props = getLabels(locale, uri);
		return getLabel(props, id, locale, params);		
	}
	public String getLabel(String id, Locale locale, Object ... params) {
		LOGGER.debug("looking for label + " + id);
		Properties props = loadLabels(locale);
		return getLabel(props, id, locale, params);
	}

	private String getLabel(Properties props, String id, Locale locale, Object ... params) {
		if (props == null) {
			LOGGER.warn("locale " + locale.getLanguage()
					+ " not found, switching to default locale: "
					+ DEFAULT_LOCALE.getLanguage());
			props = loadLabels(DEFAULT_LOCALE);
		}
		if (params != null && params.length > 0)
			return MessageFormat.format(props.getProperty(id), params);
		return props.getProperty(id);		
	}
	

	private InputStream getStream(String path) {
		return I18nService.class.getResourceAsStream(path);
	}


	public Properties loadLabels(Locale locale) {

		String language = locale.getLanguage();

        LOGGER.debug("loading labels for locale " + language);

        Properties p;

        if (locale.equals(Locale.ITALIAN) || locale.equals(Locale.ITALY) || locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            p = i18nRepository.loadProperties(locale.getLanguage(), locations);
        } else {
            LOGGER.warn("locale " + language
                    + " not found, fallback to english");
            p = i18nRepository.loadProperties(Locale.ENGLISH.getLanguage(), locations);
        }

        return p;

	}


	public Properties getLabels(Locale locale, String uri) {

		LOGGER.debug("loading labels for " + uri + " " + locale.getLanguage());

        Properties result = loadLabels(locale);

        LOGGER.debug("loaded " + result.keySet().size() + " default "
                + locale.getLanguage() + " labels");

        Properties labels = i18nRepository.getLabelsForUrl(uri + "_" + locale.getLanguage());

        result.putAll(labels);

		return result;

	}



    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public void addLocation(String location) {
        locations.add(location);
    }



}
