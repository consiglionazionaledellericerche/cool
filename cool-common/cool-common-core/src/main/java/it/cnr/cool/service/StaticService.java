package it.cnr.cool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 19/02/15.
 */
public class StaticService {


    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";

    public static final int CACHE_DAYS = 3; // Cache expiration

    private static final Logger LOGGER = LoggerFactory
            .getLogger(StaticService.class);

    public static String getCacheControl() {
        int n = CACHE_DAYS * 24 * 60 * 60;
        return String.format("max-age=%d, public", n);
    }


    public static String getMimeType(String path) {
        String mimeType;

        if (path.indexOf(".css") > 0) {
            mimeType = "text/css";
        } else if (path.indexOf(".json") > 0) {
            mimeType = "application/json";
        } else if (path.indexOf(".js") > 0) {
            mimeType = "application/javascript";
        } else if (path.indexOf(".png") > 0) {
            mimeType = "image/png";
        } else if (path.indexOf(".gif") > 0) {
            mimeType = "image/gif";
        } else if (path.indexOf(".handlebars") > 0) {
            mimeType = "text/x-handlebars-template";
        } else if (path.indexOf(".ico") > 0) {
            mimeType = "image/x-icon";
        } else if (path.indexOf(".woff") > 0) {
            mimeType = "application/font-woff";
        } else if (path.indexOf(".ttf") > 0) {
            mimeType = "font/ttf";
        } else if (path.indexOf(".otf") > 0) {
            mimeType = "font/opentype";
        } else if (path.indexOf(".html") > 0) {
            mimeType = "text/html";
        } else {
            mimeType = DEFAULT_MIME_TYPE;
            LOGGER.warn("mimetype not found for path: " + path
                    + ", using default");
        }
        return mimeType;
    }

}
