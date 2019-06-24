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

package it.cnr.mock;


public class JSONUtils {

	public Object encodeJSONString(Object value) {
		if (value instanceof String) {
			return encodeJSONStringInner((String) value);
		} else {
			return value;
		}
	}

	/**
	 * Safely encode a JSON string value.
	 * 
	 * @return encoded string, null is handled and returned as "".
	 */
	private static String encodeJSONStringInner(final String s) {
		if (s == null || s.length() == 0) {
			return "";
		}

		StringBuilder sb = null; // create on demand
		String enc;
		char c;
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			enc = null;
			c = s.charAt(i);
			switch (c) {
			case '\\':
				enc = "\\\\";
				break;
			case '"':
				enc = "\\\"";
				break;
			case '/':
				enc = "\\/";
				break;
			case '\b':
				enc = "\\b";
				break;
			case '\t':
				enc = "\\t";
				break;
			case '\n':
				enc = "\\n";
				break;
			case '\f':
				enc = "\\f";
				break;
			case '\r':
				enc = "\\r";
				break;

			default:
				if ((c) >= 0x80) {
					// encode all non basic latin characters
					String u = "000" + Integer.toHexString(c);
					enc = "\\u" + u.substring(u.length() - 4);

				}
				break;
			}

			if (enc != null) {
				if (sb == null) {
					String soFar = s.substring(0, i);
					sb = new StringBuilder(i + 8);
					sb.append(soFar);
				}
				sb.append(enc);
			} else {
				if (sb != null) {
					sb.append(c);
				}
			}
		}

		if (sb == null) {
			return s;
		} else {
			return sb.toString();
		}
	}

}
