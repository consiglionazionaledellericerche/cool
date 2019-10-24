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

package it.cnr.bulkinfo.cool;

import it.cnr.bulkinfo.BulkInfoImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class BulkInfoCoolImpl extends BulkInfoImpl implements BulkInfoCool, Serializable {

	private static final String CLASS = "class";
	private static final String REQUIRED_WIDGET = "requiredWidget";
	private static final String JSONVALIDATOR = "jsonvalidator";
	private static final String JSONSETTINGS = "jsonsettings";
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_VERSION = "1.5";
	public static final String VERSION_2_0 = "2.0";

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoCoolImpl.class);
	public static final String WIDGET = "widget";
	public static final String JSONLIST = "jsonlist";
	public static final String UI_SELECT = "ui.select";
	//private static final String version = DEFAULT_VERSION;

	public BulkInfoCoolImpl(String bulkInfoName, Document doc) {
		super(bulkInfoName, doc);
	}

	public BulkInfoCoolImpl(String bulkInfoName, Document doc, boolean isProduction) {
	  super(bulkInfoName, doc, isProduction);
	}

	public BulkInfoCoolImpl() {
		super();
	}

	// TODO controllare se funziona anche con i BulkInfo v2/v3
	@Override
	public void addFieldProperty(PropertyDefinition<?> propertyDefinition) {

		if (getFieldPropertyByProperty(propertyDefinition.getId()) == null
				|| getFieldPropertyByProperty(propertyDefinition.getId()).isEmpty()) {
			 
			JSONObject jsonvalidator = new JSONObject();
			JSONObject jsonsettings = new JSONObject();
			FieldProperty fieldProperty = new FieldProperty();
			fieldProperty.addAttribute("name", propertyDefinition.getLocalName());
			fieldProperty.addAttribute("property", propertyDefinition.getId());

			FieldProperty fieldPropertyLabel = new FieldProperty();
			fieldPropertyLabel.addAttribute("name", "jsonlabel");
			fieldPropertyLabel.addAttribute("key", "label.".concat(propertyDefinition.getId().replaceAll(":",".")));
			fieldPropertyLabel.addAttribute("default", propertyDefinition.getDisplayName());
			fieldProperty.addSubProperty("jsonlabel", fieldPropertyLabel);
			
			fieldProperty.addAttribute("visible", String.valueOf(Boolean.TRUE));
			fieldProperty.addAttribute("generated", String.valueOf(Boolean.TRUE));
			boolean isDateTime = propertyDefinition.getPropertyType().equals(PropertyType.DATETIME);
			boolean isBoolean = propertyDefinition.getPropertyType().equals(PropertyType.BOOLEAN);
			
			if (propertyDefinition.getDescription().indexOf("class:") != -1) {
				String classValue = propertyDefinition.getDescription().substring(propertyDefinition.getDescription().indexOf("class:") + 6);
				if (!classValue.contains("input-"))
					classValue = classValue.concat((isDateTime ? " input-medium " : isBoolean ? " input-sm " : " input-xxlarge "));
				fieldProperty.addAttribute(CLASS, classValue);
			} else {
				fieldProperty.addAttribute(CLASS, isDateTime ? " input-medium " : isBoolean ? " input-sm " : " input-xxlarge ");				
			}
			if (propertyDefinition.getCardinality().equals(Cardinality.MULTI)) {
				fieldProperty.addAttribute("multiple", "multiple");
			}
			if (isBoolean) {
				if (propertyDefinition.getDescription().contains("ui.radio")) {
					fieldProperty.addAttribute("inputType", "RADIOGROUP");
					fieldProperty.addAttribute(WIDGET, "ui.radio");
					fieldProperty.addAttribute(CLASS, Optional.ofNullable(fieldProperty.getAttribute(CLASS)).map(x -> x.concat(" check")).orElse("check"));
					
					FieldProperty field = new FieldProperty();
					field.setElementName(JSONLIST);
					FieldProperty yes = new FieldProperty();
					yes.addAttribute("defaultLabel", "Y");
					yes.addAttribute("key", "true");
					yes.addAttribute("label", "label.option.yes");					
					field.addListElement(yes);
					FieldProperty no = new FieldProperty();					
					no.addAttribute("defaultLabel", "N");
					no.addAttribute("key", "false");
					no.addAttribute("label", "label.option.no");										
					field.addListElement(no);
					fieldProperty.addSubProperty(JSONLIST, field);
				} else {					
					fieldProperty.addAttribute("inputType", "CHECKBOX");
					fieldProperty.addAttribute(WIDGET, "ui.checkbox");
				}
			} else {
				if (propertyDefinition.getDescription().indexOf("inputType:") != -1) {
					String inputType = propertyDefinition.getDescription().substring(propertyDefinition.getDescription().indexOf("inputType:") + 10);
					if (inputType.indexOf("class:") != -1)
						inputType = inputType.substring(0, inputType.indexOf("class:") -1);
					fieldProperty.addAttribute("inputType", inputType);
				} else {
					fieldProperty.addAttribute("inputType", "TEXT");					
				}
			}
			if (propertyDefinition.getDescription().indexOf("widget:") != -1) {
				fieldProperty.addAttribute(WIDGET, propertyDefinition.getDescription().substring(propertyDefinition.getDescription().indexOf("widget:") + 7));
			}
			
			if (propertyDefinition.getLocalName().contains("stato_estero")) {
				fieldProperty.addAttribute(WIDGET, "ui.country");
			}
			if (propertyDefinition.getLocalName().contains("comune")) {
				fieldProperty.addAttribute(WIDGET, "ui.city");
			}
			if (propertyDefinition.getLocalName().contains("username")) {
				fieldProperty.addAttribute(WIDGET, "ui.authority");
				jsonsettings.put("usersOnly", true);
			}
			if (isDateTime) {
				fieldProperty.addAttribute(WIDGET, "ui.datepicker");
			}
			if (propertyDefinition.getDefaultValue() != null && !propertyDefinition.getDefaultValue().isEmpty()) {
				fieldProperty.addAttribute("default", String.valueOf(propertyDefinition.getDefaultValue().get(0)));
			}
			fieldProperty.addAttribute("labelClass", "control-label");
			if (propertyDefinition.getPropertyType().equals(PropertyType.INTEGER)) {
				jsonvalidator.put("digits", true);
			}
			if (propertyDefinition.isRequired()) {
				if (fieldProperty.getAttribute(WIDGET) != null)
					jsonvalidator.put(REQUIRED_WIDGET, true);
				else
					jsonvalidator.put("required", true);
			}
						
			if (jsonvalidator.length() > 0 ) {
				fieldProperty.addAttribute(JSONVALIDATOR, jsonvalidator.toString());				
			}
			if (jsonsettings.length() > 0 ) {
				fieldProperty.addAttribute(JSONSETTINGS, jsonsettings.toString());
			}
			fieldProperty.setBulkInfo(this);
			if (!propertyDefinition.getUpdatability().equals(Updatability.READONLY)) {
				this.getForms().get("default").addFormFieldProperty(fieldProperty);
			}
			this.getColumnSets().get("default").addColumnFieldProperty(fieldProperty);
			this.getFreeSearchSets().get("default").addFindFieldProperty(fieldProperty);

		} else {
			for (FieldProperty fieldProperty : getFieldPropertyByProperty(propertyDefinition.getId())) {
				if (propertyDefinition.getChoices() != null && !propertyDefinition.getChoices().isEmpty()) {
					try {
						JSONArray json = new JSONArray();
						for (Choice<?> choice : propertyDefinition.getChoices()) {
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("key", choice.getValue().get(0));
							jsonObj.put("label", "label.".concat(propertyDefinition.getId()).concat(choice.getDisplayName()));
							jsonObj.put("defaultLabel", choice.getDisplayName());
							json.put(jsonObj);
						}
						fieldProperty.addAttribute(JSONLIST, json.toString());
						if (fieldProperty.getAttribute(WIDGET) == null)
							fieldProperty.addAttribute(WIDGET, UI_SELECT);
						putClassValue(propertyDefinition, fieldProperty);
						if (propertyDefinition.isRequired()) {
							FieldProperty fieldPropertyValidator = new FieldProperty();
							fieldPropertyValidator.setElementName(JSONVALIDATOR);
							fieldPropertyValidator.addAttribute(REQUIRED_WIDGET, Boolean.TRUE.toString());
							fieldProperty.getSubProperties().remove(JSONVALIDATOR);
							fieldProperty.addSubProperty(JSONVALIDATOR, fieldPropertyValidator);

							Optional<Collection<FieldProperty>> ofNullable = Optional.ofNullable(fieldProperty.getBulkInfo().getForm(fieldProperty.getBulkInfo().getCmisTypeName()));
							Optional<Stream<FieldProperty>> map2 = ofNullable.map(map -> map.stream().filter(field -> field.getName().equalsIgnoreCase(fieldProperty.getName())));
							map2.ifPresent(x -> x.forEach(a -> {
								a.getSubProperties().remove(JSONVALIDATOR);
								a.addSubProperty(JSONVALIDATOR, fieldPropertyValidator);
								putClassValue(propertyDefinition, a);
							}));
						}
					} catch (JSONException e) {
						LOGGER.error(e.getMessage(), e);
					}
					for (String name : getForms().keySet()) {
						if (getForms().get(name).getFieldProperty(fieldProperty.getName()) != null) {
							this.getForms().get(name).addFormFieldProperty(fieldProperty);
						}
					}
				}
			}
		}
	}
	
	private final void putClassValue(PropertyDefinition<?> propertyDefinition, FieldProperty fieldProperty) {
		String classValue = null;
		if (propertyDefinition.getDescription().indexOf("class:") != -1) {							
			classValue = propertyDefinition.getDescription().substring(propertyDefinition.getDescription().indexOf("class:") + 6);							
			classValue = classValue.substring(0, Optional.of(classValue.indexOf(" ")).filter(x -> x != -1).orElse(classValue.length()));
			fieldProperty.addAttribute(CLASS, Optional.ofNullable(fieldProperty.getAttribute(CLASS)).orElse("").concat(" ").concat(classValue));
		}
	}
	
	@Override
	public PropertyDefinition<?> getPropertyDefinition(Session session,
			CmisObject cmisObject, FieldProperty fieldProperty) {
		if (cmisObject != null)
			return cmisObject.getProperty(fieldProperty.getProperty()).getDefinition();
		else {
			ObjectType objectType = session.getTypeDefinition(getCmisTypeName());
			if (objectType.getPropertyDefinitions().containsKey(fieldProperty.getProperty()))
				return objectType.getPropertyDefinitions().get(fieldProperty.getProperty());
			else {
				if (getCmisImplementsName() != null) {
					for (String aspectName : getCmisImplementsName().keySet()) {
						ObjectType policyType = session.getTypeDefinition(aspectName);
						if (policyType.getPropertyDefinitions().containsKey(fieldProperty.getProperty())) {
							return policyType.getPropertyDefinitions().get(fieldProperty.getProperty());
						}
					}
				}
			}
		}
		return null;
	}
}