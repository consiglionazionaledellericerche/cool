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

public class BulkInfoCoolImpl extends BulkInfoImpl implements BulkInfoCool {


	public static final String DEFAULT_VERSION = "1.5";
	public static final String VERSION_2_0 = "2.0";

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoCoolImpl.class);
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
			FieldProperty fieldProperty = new FieldProperty();
			fieldProperty.addAttribute("name", propertyDefinition.getLocalName());
			fieldProperty.addAttribute("property", propertyDefinition.getId());
			fieldProperty.addAttribute(
					"jsonlabel",
					"{ \"key\" : \"label."
							.concat(propertyDefinition.getId().replaceAll(":",
									".")).concat("\", \"default\":\"")
							.concat(propertyDefinition.getDisplayName())
							.concat("\"}"));
			fieldProperty.addAttribute("visible", String.valueOf(Boolean.TRUE));
			fieldProperty.addAttribute("generated", String.valueOf(Boolean.TRUE));
			boolean isDateTime = propertyDefinition.getPropertyType().equals(PropertyType.DATETIME);
			if (propertyDefinition.getDescription().indexOf("class:") != -1) {
				String classValue = propertyDefinition.getDescription().substring(propertyDefinition.getDescription().indexOf("class:") + 6);
				if (!classValue.contains("input-"))
					classValue.concat((isDateTime ? " input-large" : " input-xxlarge"));
				fieldProperty.addAttribute("class", classValue);
			} else {
				fieldProperty.addAttribute("class", isDateTime ? "input-large" : "input-xxlarge");				
			}
			if (propertyDefinition.getCardinality().equals(Cardinality.MULTI)) {
				fieldProperty.addAttribute("multiple", "multiple");
			}
			if (propertyDefinition.getPropertyType().equals(PropertyType.BOOLEAN)) {
				if (propertyDefinition.getDescription().contains("ui.radio")) {
					fieldProperty.addAttribute("inputType", "RADIOGROUP");
					fieldProperty.addAttribute("widget", "ui.radio");
					fieldProperty.addAttribute("class", "check");
					
					FieldProperty field = new FieldProperty();
					field.setElementName("jsonlist");
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
					fieldProperty.addSubProperty("jsonlist", field);
				} else {					
					fieldProperty.addAttribute("inputType", "CHECKBOX");
					fieldProperty.addAttribute("widget", "ui.checkbox");
					
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
			if (propertyDefinition.getLocalName().contains("stato_estero")) {
				fieldProperty.addAttribute("widget", "ui.country");
			}
			if (isDateTime) {
				fieldProperty.addAttribute("widget", "ui.datepicker");				
			}
			if (propertyDefinition.getDefaultValue() != null && !propertyDefinition.getDefaultValue().isEmpty()) {
				fieldProperty.addAttribute("default", String.valueOf(propertyDefinition.getDefaultValue().get(0)));
			}
			fieldProperty.addAttribute("labelClass", "control-label");
			if (propertyDefinition.getPropertyType().equals(PropertyType.INTEGER)) {
				jsonvalidator.put("digits", true);
			}
			if (propertyDefinition.isRequired()) {
				if (fieldProperty.getAttribute("widget") != null)
					jsonvalidator.put("requiredWidget", true);
				else
					jsonvalidator.put("required", true);
			}
						
			if (jsonvalidator.length() > 0 ) {
				fieldProperty.addAttribute("jsonvalidator", jsonvalidator.toString());				
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
							jsonObj.put("label", choice.getDisplayName());
							jsonObj.put("defaultLabel", choice.getDisplayName());
							json.put(jsonObj);
						}
						fieldProperty.addAttribute("jsonlist", json.toString());
						fieldProperty.addAttribute("widget", "ui.select");
						if (propertyDefinition.isRequired()) {
							fieldProperty.addAttribute("jsonvalidator", "{\"requiredWidget\": true}");
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