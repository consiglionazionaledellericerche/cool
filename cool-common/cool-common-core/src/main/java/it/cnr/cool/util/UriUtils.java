package it.cnr.cool.util;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriUtils {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(UriUtils.class);

	@SuppressWarnings("deprecation")
	public static String encode(String s) {
		String encoded;
		try {
			encoded = URIUtil.encodePathQuery(s);
		} catch (URIException e) {
			LOGGER.warn("unable to encode string " + s, e);
			encoded = URLEncoder.encode(s);
		}

		return encoded;
	}

	@SuppressWarnings("deprecation")
	public static String decode(String s) {
		String encoded;
		try {
			encoded = URIUtil.decode(s);
		} catch (URIException e) {
			LOGGER.warn("unable to encode string " + s, e);
			encoded = URLDecoder.decode(s);
		}

		return encoded;
	}

}
