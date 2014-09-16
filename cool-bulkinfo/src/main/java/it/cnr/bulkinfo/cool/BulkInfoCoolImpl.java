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

	public BulkInfoCoolImpl() {
		super();
	}

	// TODO controllare se funziona anche con i BulkInfo v2/v3
	@Override
	public void addFieldProperty(PropertyDefinition<?> propertyDefinition) {

		if (getFieldPropertyByProperty(propertyDefinition.getId()) == null
				|| getFieldPropertyByProperty(propertyDefinition.getId()).isEmpty()) {

			FieldProperty fieldProperty = new FieldProperty();
			fieldProperty.addAttribute("name", propertyDefinition.getId());
			fieldProperty.addAttribute("property", propertyDefinition.getId());
			fieldProperty.addAttribute(
					"jsonlabel",
					"{ \"key\" : \"label."
							.concat(propertyDefinition.getId().replaceAll(":",
									".")).concat("\", \"default\":\"")
							.concat(propertyDefinition.getDisplayName())
							.concat("\"}"));
			fieldProperty.addAttribute("visible", String.valueOf(Boolean.TRUE));

			if (propertyDefinition.getCardinality().equals(Cardinality.MULTI)) {
				fieldProperty.addAttribute("multiple", "multiple");
			}
			if (propertyDefinition.getPropertyType().equals(PropertyType.BOOLEAN)) {
				fieldProperty.addAttribute("inputType", "CHECKBOX");
				fieldProperty.addAttribute("widget", "ui.checkbox");
			} else {
				fieldProperty.addAttribute("inputType", "TEXT");
			}

			if (propertyDefinition.getPropertyType().equals(PropertyType.DATETIME)) {
				fieldProperty.addAttribute("widget", "ui.datepicker");
			}
			if (propertyDefinition.getDefaultValue() != null) {
				fieldProperty.addAttribute("default", String.valueOf(propertyDefinition.getDefaultValue().get(0)));
			}
			fieldProperty.addAttribute("labelClass", "control-label");
			if (propertyDefinition.isRequired() && fieldProperty.getAttribute("jsonvalidator") == null) {
				fieldProperty.addAttribute("jsonvalidator", "{\"required\": true}");
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

	// TODO: ???
	// HttpSession se = ServletUtil.getRequest().getSession(false);
	// Session session = (Session)se.getAttribute(CMISService.DEFAULT_SERVER);

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