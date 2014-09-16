package it.cnr.cool.util.format;


import it.cnr.cool.util.StringUtil;

import java.io.Reader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonParser {
	@Autowired
	private GsonBuilder gsonBuilder;

	public void init(){
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
	}
	
	public <T> T fromJson(Reader json, Class<T> classOfT)
			throws JsonParseException {
		return gsonBuilder.create().fromJson(json, classOfT);
	}
	public String toJson(Object src) {
		return gsonBuilder.create().toJson(src);
	}

}
