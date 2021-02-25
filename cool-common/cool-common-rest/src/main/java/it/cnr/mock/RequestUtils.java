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

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class RequestUtils {
	
	public static Map<String, String[]> extractFormParams(
			MultivaluedMap<String, String> formParams) {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		for (Entry<String, List<String>> appo : formParams.entrySet()) {
			List<String> value = appo.getValue();
			properties.put(appo.getKey(), value.toArray(new String[value.size()]));
		}
		return properties;
	}

	public enum LANG {
		it,en;

		public static Stream<String> allowedValues() {
			return Arrays.asList(RequestUtils.LANG.values()).stream().map(lang -> lang.name());
		}

		public static boolean isAllowed(String s) {
			return Arrays.asList(RequestUtils.LANG.values()).stream().map(lang -> lang.name()).anyMatch(lang -> lang.equalsIgnoreCase(s));
		}
	}
}