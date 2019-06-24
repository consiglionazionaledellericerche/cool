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

package it.cnr.cool.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.cnr.bulkinfo.BulkInfoImpl.FieldProperty;
import it.cnr.bulkinfo.BulkInfoSerializer;
import it.cnr.bulkinfo.cool.BulkInfoCool;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe di utilita' per tradurre pojo BulkInfo in Json
 * In prospettiva sara' in grado di capire la versione del BulkInfo e
 * tradurre in maniera diversa di conseguenza.
 * 
 * @author marcin
 *
 */
@Service
public class BulkInfoCoolSerializer extends BulkInfoSerializer {

	private String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private Gson gson = new Gson();

	/**
	 * @param result
	 * @param bulkInfo
	 */
	private void putAspects(JsonObject result, BulkInfoCool bulkInfo) {
		JsonArray aspectsJson = new JsonArray();
		for(String aspect : bulkInfo.getCmisImplementsNameList()) {
			aspectsJson.add(gson.toJsonTree(aspect));
		}
		result.add("aspect", aspectsJson);
	}

	protected void putCustomProperties(JsonObject result,
			Map<String, Object> model) {
		// no action
		// put custom logic in subclasses
		BulkInfoCool bulkInfo = (BulkInfoCool) model.get("bulkInfo");
		CmisObject cmisObject = (CmisObject) model.get("cmisObject");

		if(bulkInfo.getCmisTypeName() != null) {
			result.addProperty("cmisObjectTypeId", bulkInfo.getCmisTypeName());
		}
		result.addProperty("action", "/crud/cmis/object?cmis:objectId=" + (cmisObject != null ? cmisObject.getId() : "") ); //<#if cmisObject??>${cmisObject.id}</#if>");

		putAspects(result, bulkInfo);

	}

	protected void putValue(JsonObject fpJson, FieldProperty fieldProperty, Map<String, Object> model) {

		BulkInfoCool bulkInfo = (BulkInfoCool) model.get("bulkInfo");
		CmisObject cmisObject = (CmisObject) model.get("cmisObject");
		String inheritedPermission = Optional.ofNullable(model.get("inheritedPermission")).map(Object::toString).orElse("");
		if(cmisObject != null) {
			if (fieldProperty.getName().equals("inheritedPermission")) {
				fpJson.add("val", gson.toJsonTree(inheritedPermission) );
			} else {
				fpJson.add("val", gson.toJsonTree(propJSONCMISValue(fieldProperty, bulkInfo, cmisObject)) );
			}
		}
	}


	/**
	 * Il nome di questo metodo e' migrato dall'ftl
	 * onestamente non so cosa voglia dire e tocca trovare un nome adatto
	 * @param fieldProperty
	 * @param cmisObject
	 * @param fpJson
	 */
	private String propJSONCMISValue(FieldProperty fieldProperty, BulkInfoCool bulkInfo, CmisObject cmisObject) {
		String propertyName = fieldProperty.getAttribute("property"), propertyMultiple = fieldProperty.getAttribute("multiple");
		if( propertyName != null && !propertyName.equals("")) {
			Object value = cmisObject.getPropertyValue(propertyName);
			
			if ( value != null && !value.toString().equals("")) {
				PropertyDefinition property = bulkInfo.getPropertyDefinition(null, cmisObject, fieldProperty);
				
				if(property != null && !property.toString().equals("") ) {
					if( property.getCardinality().value().equals("multi") && "multiple".equals(propertyMultiple)) {
						return getMultiValue(cmisObject, propertyName);
					} else {
						return getValueByPropertyType(cmisObject, propertyName,
								property);
					}
				}
			}
		}
		return "";
	}

	/**
	 * @param cmisObject
	 * @param propertyName
	 * @return
	 */
	private String getMultiValue(CmisObject cmisObject, String propertyName) {
		// TODO test
		String multiReturn = "[";
		Iterable valueList = (Iterable) cmisObject.getPropertyValue(propertyName);
		Iterator iterator = valueList.iterator();
		while(iterator.hasNext()) {
			Object multiValue = iterator.next();
			multiReturn += multiValue.toString();
			if(iterator.hasNext()) {
				multiReturn += ",";
			}
		}
		return multiReturn + "]";
	}

	/**
	 * @param cmisObject
	 * @param propertyName
	 * @param property
	 * @return
	 */
	private String getValueByPropertyType(CmisObject cmisObject,
			String propertyName, PropertyDefinition property) {
		if( property.getPropertyType().value().equals("boolean")) {
			return cmisObject.getProperty(propertyName).getValueAsString();
		} else if ( property.getPropertyType().value().equals("datetime") ) {
			Date date = ((Calendar)cmisObject.getPropertyValue(propertyName)).getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(date);
		} else if ( property.getPropertyType().value().equals("integer") ) {
			return cmisObject.getProperty(propertyName).getValueAsString(); // TODO formattare come un numero
		} else if ( property.getPropertyType().value().equals("decimal") ) {
			DecimalFormat df = new DecimalFormat("#0.##");
			BigDecimal bdValue = (BigDecimal) cmisObject.getPropertyValue(propertyName);
			return df.format(bdValue); // TODO formattare come un decimale
		} else {
			return cmisObject.getProperty(propertyName).getValueAsString();
		}
	}
}
