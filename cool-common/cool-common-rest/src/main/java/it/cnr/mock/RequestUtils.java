package it.cnr.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;

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
}