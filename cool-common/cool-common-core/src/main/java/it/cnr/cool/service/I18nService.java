package it.cnr.cool.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: integrare it.cnr.cool.mvc.LocaleResolver ?
public class I18nService {

	private static final String EXTENSION = ".ftl";
	private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private final Map<String, Properties> labels = new HashMap<String, Properties>();

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

		if (!labels.containsKey(language)) {
			LOGGER.info("loading labels for locale " + language);

			if (locale.equals(Locale.ITALIAN) || locale.equals(Locale.ITALY) || locale.equals(Locale.ENGLISH)) {
				labels.put(language, loadProperties(locale.getLanguage()));
			} else {
				LOGGER.warn("locale " + language
						+ " not found, fallback to english");
				labels.put(language, loadProperties(Locale.ENGLISH.getLanguage()));
			}
		}

		return labels.get(language);
	}

	private Properties loadProperties(String locales) {

		Properties labels = new Properties();

		LOGGER.debug("loading labels for locale " + locales);

		Locale myLocale = new Locale(locales);

		LOGGER.debug("loading labels for locale " + myLocale);

		for (String location : locations) {

			LOGGER.debug("loading location: " + location);

			try {
				ResourceBundle resourcebundle = ResourceBundle.getBundle(
						location, myLocale);
				Enumeration<String> enumKeys = resourcebundle.getKeys();
				while (enumKeys.hasMoreElements() == true) {
					String key = enumKeys.nextElement();
                    String val = resourcebundle.getString(key);
                    String utf8val;
                    try {
                        byte[] bytes = val.getBytes("ISO-8859-1");
                         utf8val = new String(bytes, "UTF-8");
                    } catch(UnsupportedEncodingException e) {
                        LOGGER.info("error for string " + key + " = " + val, e);
                        utf8val = val;
                    }
					labels.put(key, utf8val);
				}
			} catch (MissingResourceException e) {
				LOGGER.warn("resource bundle not found: " + location + " "
						+ myLocale.getLanguage(), e);

			}


		}

		LOGGER.info(labels.size() + " properties loaded for locale " + myLocale);

		return labels;
	}

	public Properties getLabels(Locale locale, String uri) {

		Properties result = new Properties();
		LOGGER.debug("loading labels for " + uri + " " + locale.getLanguage());

		result.putAll(loadLabels(locale));

		LOGGER.debug("loaded " + result.keySet().size() + " default "
				+ locale.getLanguage() + " labels");

		String path = "/i18n/" + uri + "_" + locale.getLanguage()
				+ ".properties";
		InputStream is = getStream(path);

		try {
			if (is != null) {
				result.load(is);
			} else {
				LOGGER.debug("lang file " + path + " doesnt exist");
			}
		} catch (IOException e) {
			LOGGER.error("unable to load lang file " + path, e);
		}

		return result;

	}

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public void addLocation(String location) {
		locations.add(location);
	}
}
