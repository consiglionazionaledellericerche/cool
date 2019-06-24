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

package it.cnr.cool.util;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;

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
