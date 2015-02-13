package it.cnr.cool.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by francesco on 13/02/15.
 */
@Repository
public class I18nRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(I18nRepository.class);


    @Cacheable(value="labels", key="#locale")
    public Properties loadProperties(String locale, List<String> locations) {

        Properties labels = new Properties();

        LOGGER.debug("loading labels for locale " + locale);

        Locale myLocale = new Locale(locale);

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



    @Cacheable("labels-uri")
    public Properties getLabelsForUrl(String f) {

        Properties result = new Properties();

        String path = "/i18n/" + f +  ".properties";

        InputStream is = I18nRepository.class.getResourceAsStream(path);

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




}
