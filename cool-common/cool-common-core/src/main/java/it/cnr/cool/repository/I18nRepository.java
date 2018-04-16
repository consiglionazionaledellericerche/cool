package it.cnr.cool.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by francesco on 13/02/15.
 */
@Repository
public class I18nRepository {

    public static final String UTF_8 = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(I18nRepository.class);

    @Cacheable(value = "labels", key = "#locale")
    public Properties loadProperties(String locale, List<String> locations) {

        Properties labels = new Properties();

        LOGGER.debug("loading labels for locale " + locale);

        Locale myLocale = new Locale(locale);

        LOGGER.debug("loading labels for locale " + myLocale);

        for (String location : locations) {

            LOGGER.debug("loading location: " + location);

            try {
                ResourceBundle resourcebundle = ResourceBundle.getBundle(
                        location, myLocale, new UTF8Control());
                resourcebundle.keySet().stream()
                        .forEach(key -> labels.put(key, resourcebundle.getString(key)));
            } catch (MissingResourceException e) {
                LOGGER.warn("resource bundle not found: " + location + " "
                        + myLocale.getLanguage(), e);
            }
        }
        LOGGER.info(labels.size() + " properties loaded for locale " + myLocale);
        return labels;
    }

    @Cacheable("labels-uri")
    public Properties getLabelsForUrl(String f) {

        Properties result = new Properties();

        String path = "/i18n/" + f + ".properties";

        InputStream is = I18nRepository.class.getResourceAsStream(path);

        try {
            if (is != null) {
                result.load(new InputStreamReader(is, UTF_8));
            } else {
                LOGGER.debug("lang file " + path + " doesnt exist");
            }
        } catch (IOException e) {
            LOGGER.error("unable to load lang file " + path, e);
        }

        return result;

    }

    public class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle
                (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, UTF_8));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }


}
