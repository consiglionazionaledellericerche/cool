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

package it.cnr.cool.util.format;


import com.google.gson.*;
import it.cnr.cool.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

@Service
public class GsonParser {
	
	public <T> T fromJson(Reader json, Class<T> classOfT)
			throws JsonParseException {
		return gsonBuilder().create().fromJson(json, classOfT);
	}
	public String toJson(Object src) {
		return gsonBuilder().create().toJson(src);
	}

	@Bean
	public GsonBuilder gsonBuilder() {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date arg0, Type arg1,
										 JsonSerializationContext arg2) {
				return arg0 == null ? null : new JsonPrimitive(StringUtil.CMIS_DATEFORMAT.format(arg0));
			}
		};

		JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement arg0, Type arg1,
									JsonDeserializationContext arg2) throws JsonParseException {
				try {
					return arg0 == null ? null : StringUtil.CMIS_DATEFORMAT.parse(arg0.getAsString());
				} catch (ParseException e) {
					return null;
				}
			}
		};
		gsonBuilder.registerTypeAdapter(Date.class, ser).registerTypeAdapter(Date.class, deser);
		return gsonBuilder;
	}
}
