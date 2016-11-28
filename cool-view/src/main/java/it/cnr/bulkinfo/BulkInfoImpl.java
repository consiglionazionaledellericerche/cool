package it.cnr.bulkinfo;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections.MultiHashMap;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class BulkInfoImpl implements BulkInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoImpl.class);

	// cached values
	protected String shortDescription;
	protected String longDescription;
	protected String cmisTypeName;
	protected String cmisQueryName;
	protected String cmisExtendsName;

	// for tracing fieldProperties
	protected boolean debug = false;

	private Map<String, FieldPropertySet> forms;
	private Map<String, FieldPropertySet> columnSets;
	private Map<String, FieldPropertySet> freeSearchSets;
	private Map<String, FieldPropertySet> printForms;

	private Map<String, FieldProperty> fieldProperties;
	private Map<String, Object> fieldPropertiesByProperty;

	private Map<String, Boolean> cmisImplementsNameJoin;

	/*
	 * TODO rimuovere i campi non necessari
	 */
	private Document document;
	private String id;

	// TODO decidere se lasciare questo costruttore
	public BulkInfoImpl() {
		super();
	}

	public final Document getDocument() {
		return this.document;
	}

	@Override
	public String getId() {
		return this.id;
	}


	public BulkInfoImpl(String id, Document document) {
		setup(id, document);
	}

	public BulkInfoImpl(String id, Document document, boolean isProduction) {
	  this.debug = !isProduction;
	  setup(id, document);
	}

	private void setup(String id, Document document) {
		this.document = document;
		this.id = id;

		constructFieldProperties();

		forms = new LinkedHashMap<String, FieldPropertySet>();
		printForms = new LinkedHashMap<String, FieldPropertySet>();
		columnSets = new LinkedHashMap<String, FieldPropertySet>();
		freeSearchSets = new LinkedHashMap<String, FieldPropertySet>();
		
		constructSets(forms, PROP_FORM_ID, PROP_FORM_FIELD_PROPERTY_ID);
		constructSets(printForms, PROP_PRINT_FORM_ID, PROP_PRINT_FORM_FIELD_PROPERTY_ID);
		constructSets(columnSets, PROP_COLUMNSET_ID, PROP_COLUMN_FIELD_PROPERTY_ID);
		constructSets(freeSearchSets, PROP_FREESEARCHSET_ID, PROP_FIND_FIELD_PROPERTY_ID);
		
		constructCmisImplementsName();
	}

	@Deprecated
	public String getTypeId() {
		return TYPE_ID;
	}

	private void setAttribute(Element element, String attrId, String text) {
		if (element.attribute(attrId) == null) {
			element.addAttribute(attrId, text);
		} else {
			element.attribute(attrId).setText(text);
		}
	}

	private void setAttribute(String attrId, String text) {
		setAttribute(getDocument().getRootElement(), attrId, text);
	}

	private String getAttribute(Element element, String attrId) {
		Attribute attribute = element.attribute(attrId);
		if (attribute != null) {
			return attribute.getText();
		}
		return null;
	}

	private String getAttribute(String attrId) {
		return getAttribute(getDocument().getRootElement(), attrId);
	}

	@Override
	public String getShortDescription() {
		if (this.shortDescription == null) {
			this.shortDescription = getAttribute(ATTR_SHORT_DESCRIPTION_ID);
		}
		return this.shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		setAttribute(ATTR_SHORT_DESCRIPTION_ID, shortDescription);
		this.shortDescription = shortDescription;
	}

	@Override
	public String getLongDescription() {
		if (this.longDescription == null) {
			this.longDescription = getAttribute(ATTR_LONG_DESCRIPTION_ID);
		}
		return this.longDescription;
	}

	public void setLongDescription(String longDescription) {
		setAttribute(ATTR_LONG_DESCRIPTION_ID, shortDescription);
		this.longDescription = longDescription;
	}

	@Override
	public String getCmisTypeName() {
		if (this.cmisTypeName == null) {
			this.cmisTypeName = getAttribute(ATTR_CMIS_TYPE_NAME_ID);
		}
		return this.cmisTypeName;
	}

	@Override
	public void setCmisTypeName(String cmisTypeName) {
		setAttribute(ATTR_CMIS_TYPE_NAME_ID, cmisTypeName);
		this.cmisTypeName = cmisTypeName;
	}

	@Override
	public String getCmisQueryName() {
		if (this.cmisQueryName == null) {
			this.cmisQueryName = getAttribute(ATTR_CMIS_QUERY_NAME_ID);
		}
		return this.cmisQueryName;
	}

	@Override
	public void setCmisQueryName(String cmisQueryName) {
		setAttribute(ATTR_CMIS_QUERY_NAME_ID, cmisQueryName);
		this.cmisQueryName = cmisQueryName;
	}

	@Override
	public String getCmisExtendsName() {
		if (this.cmisExtendsName == null) {
			this.cmisExtendsName = getAttribute(ATTR_CMIS_EXTENDS_NAME_ID);
		}
		return this.cmisExtendsName;
	}

	public void setCmisExtendsName(String cmisExtendsName) {
		setAttribute(ATTR_CMIS_EXTENDS_NAME_ID, cmisExtendsName);
		this.cmisExtendsName = cmisExtendsName;
	}

	@SuppressWarnings("unchecked")
	private void constructCmisImplementsName() {
		cmisImplementsNameJoin = new HashMap<String, Boolean>();
		List<Element> elements = getDocument().getRootElement().elements(PROP_CMIS_IMPLEMENTS_ID);
		for (Element element : elements) {
			Boolean join = Boolean.TRUE;
			if (element.attribute("join") != null) {
				join = Boolean.valueOf(element.attribute("join").getText());
			}
			cmisImplementsNameJoin.put(element.attribute("name").getText(), join);
		}		
	}
	@Override
	public Map<String, Boolean> getCmisImplementsName() {
		return cmisImplementsNameJoin;
	}

	public List<String> getCmisImplementsNameList() {
		List<String> result = new ArrayList<String>();
		Map<String, Boolean> cmisImplementsName = getCmisImplementsName();
		for (String key : cmisImplementsName.keySet()) {
			if (cmisImplementsName.get(key)) {
				result.add(key);
			}
		}
		return result;
	}

	@Override
	public void addCmisExtensionElement(String key, Boolean value) {
		if (cmisImplementsNameJoin == null) {
			cmisImplementsNameJoin = new HashMap<String, Boolean>();
		}
		cmisImplementsNameJoin.put(key, value);
	}

	@Override
	public Map<String, FieldPropertySet> getForms() {
		return this.forms;
	}

	@Override
	public Map<String, FieldPropertySet> getColumnSets() {
		return this.columnSets;
	}

	@Override
	public Map<String, FieldPropertySet> getFreeSearchSets() {
		return this.freeSearchSets;
	}

	@Override
	public Map<String, FieldPropertySet> getPrintForms() {
		return this.printForms;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<FieldProperty> getForm(String name) {
		if (getForms().get(name) != null) {
			return getForms().get(name).getFieldProperties();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<FieldProperty> getColumnSet(String name) {
		if (getColumnSets().get(name) != null) {
			return getColumnSets().get(name).getFieldProperties();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<FieldProperty> getFreeSearchSet(String name) {
		if (getFreeSearchSets().get(name) != null) {
			return getFreeSearchSets().get(name).getFieldProperties();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<FieldProperty> getPrintForm(String name) {
		if (getPrintForms().get(name) != null) {
			return getPrintForms().get(name).getFieldProperties();
		}
		return Collections.EMPTY_LIST;
	}

	public Collection<FieldProperty> getDefaultForm() {
		return getForm("default");
	}

	public Collection<FieldProperty> getDefaultColumnSet() {
		return getColumnSet("default");
	}

	public Collection<FieldProperty> getDefaultFreeSearchSet() {
		return getFreeSearchSet("default");
	}

	@Override
	public Map<String, FieldProperty> getFieldProperties() {
		return this.fieldProperties;
	}

	/**
	 * Aggiunge una FieldProperty.
	 */
	public void addFieldProperty(FieldProperty fieldproperty) {
		fieldProperties.put(fieldproperty.getName(), fieldproperty);
		fieldPropertiesByProperty.put(fieldproperty.getProperty(), fieldproperty);
		fieldproperty.setBulkInfo(this);
	}

	/**
	 * Cerca una FieldProperty.
	 */
	public FieldProperty getFieldProperty(String name) {
		return fieldProperties.get(name);
	}

	/**
	 * Cerca una FormFieldProperty.
	 */
	public FieldProperty getFormFieldProperty(String name) {
		return getFormFieldProperty("default", name);
	}

	public FieldProperty getFormFieldProperty(String formName, String name) {
		if (forms.get(formName) != null) {
			return forms.get(formName).getFieldProperty(name);
		}
		return null;
	}

	/**
	 * Cerca una ColumnFieldProperty.
	 */
	public FieldProperty getColumnFieldProperty(String name) {
		return getColumnFieldProperty("default", name);
	}

	public FieldProperty getColumnFieldProperty(String columnSetName, String name) {
		if (columnSets.get(columnSetName) != null) {
			return columnSets.get(columnSetName).getFieldProperty(name);
		}
		return null;
	}

	/**
	 * Cerca una FindFieldProperty.
	 */
	public FieldProperty getFindFieldProperty(String name) {
		return getFindFieldProperty("default", name);
	}

	public FieldProperty getFindFieldProperty(String freeSearchSetName,
			String name) {
		if (freeSearchSets.get(freeSearchSetName) != null) {
			return freeSearchSets.get(freeSearchSetName).getFieldProperty(name);
		}
		return null;
	}

	/**
	 * Cerca una FieldProperty in base alla property
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<FieldProperty> getFieldPropertyByProperty(String property) {
		return (List<FieldProperty>) fieldPropertiesByProperty.get(property);
	}

	@Override
	public Map<String, Object> getFieldPropertiesByProperty() {
		return fieldPropertiesByProperty;
	}

	private void completeFieldProperty(FieldProperty fieldproperty, boolean fromParent) {
		FieldProperty localFieldproperty = getFieldProperty(fieldproperty.getName());
		if (localFieldproperty == null) {
			addFieldProperty(fieldproperty);
		}
		else {
			if (fromParent) {
			  if (debug) {
			    String trace = localFieldproperty.getAttribute("trace");
			    if(trace == null) {
			      trace = fieldproperty.getAttribute("trace");
			    } else {
			      trace = fieldproperty.getAttribute("trace") + ":::" + trace;
			    }
			    localFieldproperty.addAttribute("trace", trace);
			  }
				localFieldproperty.addAllAttribute(fieldproperty.attributes);
				localFieldproperty.addAllSubProperties(fieldproperty.subProperties);
			} else {
				fieldproperty.addAllAttribute(localFieldproperty.attributes);
				fieldproperty.addAllSubProperties(localFieldproperty.subProperties);
				// TODO if aspect?
			}
			for(FieldProperty listElement : localFieldproperty.getListElements()) {
				fieldproperty.addListElement(listElement);
			}
			if (fieldPropertiesByProperty.get(fieldproperty.getProperty()) == null) {
				fieldPropertiesByProperty.put(fieldproperty.getProperty(), fieldproperty);
			}

		}
		fieldproperty.setBulkInfo(this);
	}

	@Override
	public void completeFieldProperty(FieldProperty fieldproperty) {
		completeFieldProperty(fieldproperty, false);
	}

	public void addForm(FieldPropertySet fieldpropertyset) {
		addFieldPropertySet(forms, fieldpropertyset);
	}

	public void addForm(String formName) {
		addFieldPropertySet(forms, new FieldPropertySet(this, formName));
	}
	public void addPrintForm(FieldPropertySet fieldpropertyset) {
		addFieldPropertySet(printForms, fieldpropertyset);
	}
	public void addPrintForm(String formName) {
		addFieldPropertySet(printForms, new FieldPropertySet(this, formName));
	}

	public void addColumnSet(FieldPropertySet fieldpropertyset) {
		addFieldPropertySet(columnSets, fieldpropertyset);
	}

	public void addFreeSearchSet(FieldPropertySet fieldpropertyset) {
		addFieldPropertySet(freeSearchSets, fieldpropertyset);
	}

	private void addFieldPropertySet(Map<String, FieldPropertySet> map, FieldPropertySet fieldpropertyset) {
		map.put(fieldpropertyset.getName(), fieldpropertyset);
		fieldpropertyset.setBulkInfo(this);
	}

	@SuppressWarnings("unchecked")
	private void constructSets(Map<String, FieldPropertySet> set, String setName, String elementName) {

		LOGGER.debug("  Constructing set " + setName);

		LOGGER.debug("    Constructing default " + setName);
		FieldPropertySet fieldpropertysetDefault = new FieldPropertySet(this, "default");
		List<Element> formFieldsPropertiesDefault = getDocument().getRootElement().elements(elementName);
		for (Element elementFormFieldProperty : formFieldsPropertiesDefault) {
			addPropertyToSet(fieldpropertysetDefault, elementFormFieldProperty);
		}
		this.addFieldPropertySet(set, fieldpropertysetDefault);

		// all other sets are different from default
		List<Element> elements = getDocument().getRootElement().elements(setName);
		for (Element element : elements) {
			FieldPropertySet fieldpropertyset = new FieldPropertySet(this, getAttribute(element, "name"));
			LOGGER.debug("    Constructing " + setName + " with name "+ element.attributeValue("name"));

			fieldpropertyset.setKey(getAttribute(element, "key"));
			fieldpropertyset.setOverride(getAttribute(element, "override"));
			List<Element> formFieldsProperties = element.elements(elementName);
			for (Element elementFormFieldProperty : formFieldsProperties) {
				addPropertyToSet(fieldpropertyset, elementFormFieldProperty);
			}
			this.addFieldPropertySet(set, fieldpropertyset);
		}
	}

	@SuppressWarnings("unchecked")
	private void addPropertyToSet(FieldPropertySet fieldpropertyset,
			Element elementFormFieldProperty) {
		FieldProperty fieldProperty = new FieldProperty();

		LOGGER.debug("      form field property " + elementFormFieldProperty.attributeValue("name"));
		List<Attribute> attrsFormFieldProperty = elementFormFieldProperty.attributes();

		for (Attribute attrFormFieldProperty : attrsFormFieldProperty) {
			LOGGER.debug("      attribute " + attrFormFieldProperty.getName()
					+ " = " + attrFormFieldProperty.getText());

			//fieldProperty.addAttribute(attrFormFieldProperty.getName(), attrFormFieldProperty.getText()); //
			constructFieldProperty(elementFormFieldProperty, fieldProperty);
		}
		completeFieldProperty(fieldProperty);
		if(debug) {
		  String trace = fieldProperty.getAttribute("trace");
		  if (trace == null) {
		    fieldProperty.addAttribute("trace", this.id + "/" + fieldpropertyset.name);
		  } else {
		    fieldProperty.addAttribute("trace", trace + ":::" + this.id + "/" + fieldpropertyset.name);
		  }
		}
		fieldpropertyset.addFormFieldProperty(fieldProperty);
	}

	/**
	 * Questo tris di metodi inserisce le fieldProperties da xml nel pojo BulkInfo
	 * Prima c'era solo il seguente metodo, ma e' stato smembrato per chiarezza
	 * Nei BulkInfo2.0 abbiamo anche le subFieldProperties (per le proprieta' json)
	 *
	 * Il primo dei tre metodi si occupa di prendere le fieldProperties e inserirle nel Bulkinfo
	 * (diversamente dalle subFieldProperties che vengono inserite nelle fieldProperties parent)
	 *
	 * Chiaramente i sottoelementi vengono caricati solo per gli elementi che
	 * interessano a noi (PROP_FIELD_PROPERTY_ID)
	 *
	 * @SuppressWarnings
	 * perche' la libreria xml che usiamo restituisce List invece che List<Element>
	 * (forse e' un po' vecchiotta?)
	 *
	 * TODO sostituire MultiHashMap
	 * TODO (secondo la documentazione va sostituito con MultiValueMap)
	 *
	 * TODO attualmente @addDefaultAttribute viene aggiunto solo al primo livello
	 * TODO credo sia giusto (negli .xml le jsonproperties non settavano questo attributo)
	 * TODO ma e' da verificare
	 *
	 * TODO spostare in BulkInfoCool
	 */
	@SuppressWarnings("unchecked")
	private void constructFieldProperties() {
		fieldProperties = new LinkedHashMap<String, BulkInfoImpl.FieldProperty>();
		fieldPropertiesByProperty = new MultiHashMap();
		List<Element> elements = getDocument().getRootElement().elements(PROP_FIELD_PROPERTY_ID);
		for (Element element : elements) {
			FieldProperty fieldProperty = new FieldProperty();

			addDefaultAttribute(fieldProperty);
			constructFieldProperty(element, fieldProperty);
			if(debug) {
			  addTraceInfo(fieldProperty);
			}

			addFieldProperty(fieldProperty);
		}
	}

	private void constructFieldProperty(Element element, FieldProperty fieldProperty) {

		fieldProperty.setElementName(element.getName());

		if(element.attributes().size() > 0) {
			addAttributes(element, fieldProperty);
			addFieldSubProperties(element, fieldProperty);
		} else {
			addListProperty(element, fieldProperty);
		}
	}

	private void addTraceInfo(FieldProperty fieldProperty) {
	  String trace = fieldProperty.getAttribute("trace");
	  if(trace == null) {
	    fieldProperty.addAttribute("trace", id);
	  } else {
	    fieldProperty.addAttribute("trace", id + ":::" + trace);
	  }
	}

	/**
	 * Trasforma sottoelementi xml (<element />) in un array
	 * per esempio le liste di opzioni
	 * @param element
	 * @param fieldProperty
	 */
	@SuppressWarnings("unchecked")
	private void addListProperty(Element element, FieldProperty fieldProperty) {

		List<Element> subElements = element.elements();
		for (Element listElement : subElements) {
			FieldProperty subFieldProperty = new FieldProperty();

			constructFieldProperty(listElement, subFieldProperty);

			fieldProperty.addListElement(subFieldProperty);
		}

	}

	/**
	 * Trasforma sottoelementi xml (<element />) in fieldProperties
	 * (da inserire nelle FieldProperties parent Map<String, FieldProperty>)
	 * @param element
	 * @param fieldProperty
	 */
	@SuppressWarnings("unchecked")
	private void addFieldSubProperties(Element element, FieldProperty fieldProperty) {
		List<Element> subElements = element.elements();
		for (Element subElement : subElements) {
			FieldProperty subFieldProperty = new FieldProperty();

			constructFieldProperty(subElement, subFieldProperty);

			fieldProperty.addSubProperty(subElement.getName(), subFieldProperty);
		}
	}

	/**
	 * Trasforma attributi xml (key="value") in proprieta' (Map<String, String>)
	 * @param element
	 * @param fieldProperty
	 */
	@SuppressWarnings("unchecked")
	private void addAttributes(Element element, FieldProperty fieldProperty) {
		List<Attribute> attrsFieldProperty = element.attributes();
		for (Attribute attrFieldProperty : attrsFieldProperty) {
			fieldProperty.addAttribute(attrFieldProperty.getName(), attrFieldProperty.getText());
		}

	}

	private void addDefaultAttribute(FieldProperty fieldProperty) {
		if (fieldProperty.getAttribute("visible") == null)
			fieldProperty.addAttribute("visible", "true");
	}


	@Override
	public void completeWithParent(BulkInfo parent, boolean aspect) {

		if (parent.getCmisImplementsName() != null) {
			for (String key : parent.getCmisImplementsName().keySet()) {
				if (!getCmisImplementsName().containsKey(key))
					getCmisImplementsName().put(key,
							parent.getCmisImplementsName().get(key));
			}
		}
		for (FieldProperty fieldproperty : parent.getFieldProperties().values()) {
			completeFieldProperty(fieldproperty, true);
		}

		copyForms(parent, aspect);

		copyColumnSets(parent, aspect);

		copyFreeSearchSets(parent, aspect);

		copyPrintForms(parent);
	}

	/**
	 * @param parent
	 */
	private void copyPrintForms(BulkInfo parent) {
		Map<String, FieldPropertySet> parentFieldSet;
		Map<String, FieldPropertySet> localFieldSet;
		parentFieldSet = parent.getPrintForms();
		localFieldSet = this.getPrintForms();

		for (String formName : parentFieldSet.keySet()) {
			if (localFieldSet.get(formName) != null) {
				for (FieldProperty fieldproperty : parentFieldSet.get(formName).getFieldProperties()) {
					localFieldSet.get(formName).addFormFieldProperty(fieldproperty);
				}
			} else {
				FieldPropertySet fieldpropertyset = new FieldPropertySet(this, formName);
				fieldpropertyset.setKey(parentFieldSet.get(formName).getKey());
				fieldpropertyset.setLabel(parentFieldSet.get(formName).getLabel());
				for (FieldProperty fieldproperty : parentFieldSet.get(formName).getFieldProperties()) {
					fieldpropertyset.addFormFieldProperty(fieldproperty);
				}
				this.addPrintForm(fieldpropertyset);
			}
		}
	}

	/**
	 * @param parent
	 * @param aspect
	 */
	private void copyFreeSearchSets(BulkInfo parent, boolean aspect) {
		Map<String, FieldPropertySet> parentFieldSet;
		Map<String, FieldPropertySet> localFieldSet;
		parentFieldSet = parent.getFreeSearchSets();
		localFieldSet = this.getFreeSearchSets();

		for (String freeSearchSetName : parentFieldSet.keySet()) {
			if (!aspect) {
				if (localFieldSet.get(freeSearchSetName) != null) {
					for (FieldProperty fieldproperty : parentFieldSet.get(freeSearchSetName)
							.getFieldProperties()) {
						if (aspect) {
							fieldproperty.addAttribute(ATTR_CMIS_TYPE_NAME_ID,
									parent.getCmisTypeName());
							fieldproperty.addAttribute(ATTR_CMIS_QUERY_NAME_ID,
									parent.getCmisQueryName());
						}
						localFieldSet.get(freeSearchSetName)
								.addFindFieldProperty(fieldproperty);
					}
				} else {
					FieldPropertySet fieldpropertyset = new FieldPropertySet(
							this, freeSearchSetName);
					for (FieldProperty fieldproperty : parentFieldSet.get(freeSearchSetName).getFieldProperties()) {
						if (aspect) {
							fieldproperty.addAttribute(ATTR_CMIS_TYPE_NAME_ID, parent.getCmisTypeName());
							fieldproperty.addAttribute(ATTR_CMIS_QUERY_NAME_ID, parent.getCmisQueryName());
						}
						fieldpropertyset.addFindFieldProperty(fieldproperty);
					}
					this.addFreeSearchSet(fieldpropertyset);
				}
			}
		}
	}

	/**
	 * @param parent
	 * @param aspect
	 */
	private void copyColumnSets(BulkInfo parent, boolean aspect) {
		Map<String, FieldPropertySet> parentFieldSet;
		Map<String, FieldPropertySet> localFieldSet;
		parentFieldSet = parent.getColumnSets();
		localFieldSet = this.getColumnSets();

		for (String columnSetName : parentFieldSet.keySet()) {
			if (localFieldSet.get(columnSetName) != null) {
				for (FieldProperty fieldproperty : parentFieldSet.get(columnSetName).getFieldProperties()) {
					String override = decideOverride(aspect, parentFieldSet,
							localFieldSet, columnSetName);
					if (override != null && override == "false" || override == null) {
						localFieldSet.get(columnSetName).addColumnFieldProperty(fieldproperty);
					} else {
						if (this.getColumnSets().get(columnSetName).getFieldProperty(fieldproperty.getName()) != null) {
							if (aspect) {
								fieldproperty.addAttribute(ATTR_CMIS_TYPE_NAME_ID,  parent.getCmisTypeName());
								fieldproperty.addAttribute(ATTR_CMIS_QUERY_NAME_ID, parent.getCmisQueryName());
							}
							localFieldSet.get(columnSetName).addColumnFieldProperty(fieldproperty);
						}
					}
				}
			} else {
				FieldPropertySet fieldpropertyset = new FieldPropertySet(this, columnSetName);
				fieldpropertyset.setKey(parentFieldSet.get(columnSetName).getKey());
				fieldpropertyset.setLabel(parentFieldSet.get(columnSetName).getLabel());
				for (FieldProperty fieldproperty : parentFieldSet.get(columnSetName).getFieldProperties()) {
					if (aspect) {
						fieldproperty.addAttribute(ATTR_CMIS_TYPE_NAME_ID, parent.getCmisTypeName());
						fieldproperty.addAttribute(ATTR_CMIS_QUERY_NAME_ID, parent.getCmisQueryName());
					}
					fieldpropertyset.addColumnFieldProperty(fieldproperty);
				}
				this.addColumnSet(fieldpropertyset);
			}
		}
	}

	/**
	 * @param parent
	 * @param aspect
	 */
	private void copyForms(BulkInfo parent, boolean aspect) {
		Map<String, FieldPropertySet> parentFieldSet;
		Map<String, FieldPropertySet> localFieldSet;
		parentFieldSet = parent.getForms();
		localFieldSet = this.getForms();

		for (String formName : parentFieldSet.keySet()) {
			if (localFieldSet.get(formName) != null) {
				for (FieldProperty fieldproperty : parent.getForms().get(formName).getFieldProperties()) {

					String override = decideOverride(aspect, parentFieldSet, localFieldSet, formName);

					if (override != null && override == "false" || override == null) {
						localFieldSet.get(formName).addFormFieldProperty(fieldproperty);
					} else {
						if (localFieldSet.get(formName).getFieldProperty(fieldproperty.getName()) != null) {
							localFieldSet.get(formName).addFormFieldProperty(fieldproperty);
						}
					}
				}
			} else {
				FieldPropertySet fieldpropertyset = new FieldPropertySet(this, formName);
				fieldpropertyset.setKey(parentFieldSet.get(formName).getKey());
				fieldpropertyset.setLabel(parentFieldSet.get(formName).getLabel());
				for (FieldProperty fieldproperty : parentFieldSet.get(formName).getFieldProperties()) {
					fieldpropertyset.addFormFieldProperty(fieldproperty);
				}
				this.addForm(fieldpropertyset);
			}
		}
	}

	/**
	 * @param aspect
	 * @param parentFieldSet
	 * @param localFieldSet
	 * @param columnSetName
	 * @return
	 */
	private String decideOverride(boolean aspect,
			Map<String, FieldPropertySet> parentFieldSet,
			Map<String, FieldPropertySet> localFieldSet, String columnSetName) {
		String override;
		if (aspect) {
			override = parentFieldSet.get(columnSetName).getOverride();
		}
		else {
			override = localFieldSet.get(columnSetName).getOverride();
		}
		return override;
	}

	public class FieldProperty implements Serializable{

		private static final long serialVersionUID = 1L;
		private final Map<String, String> attributes;
		private BulkInfo bulkInfo;
		private Map<String, FieldProperty> subProperties;
		private String elementName;
		private List<FieldProperty> listElements;

		public FieldProperty() {
			super();
			attributes = new HashMap<String, String>();
			subProperties = new HashMap<String, FieldProperty>();
			listElements = new ArrayList<FieldProperty>();
			setElementName(null);
		}

		public void addListElement(FieldProperty listElement) {
			listElements.add(listElement);
		}

		public Map<String, FieldProperty> getSubProperties() {
			return subProperties;
		}

		public void setSubProperties(Map<String, FieldProperty> subProperties) {
			this.subProperties = subProperties;
		}

		public void addSubProperty(String name, FieldProperty subProperty) {
			this.subProperties.put(name, subProperty);
		}

		public FieldProperty getSubProperty(String name) {
			return subProperties.get(name);
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public void addAttribute(String key, String value) {
			attributes.put(key, value);
		}

		public void addAllAttribute(Map<String, String> newAttributes) {
			for (String key : newAttributes.keySet()) {
				if (attributes.get(key) == null)
					attributes.put(key, newAttributes.get(key));
			}
		}

		public void addAllSubProperties(Map<String, FieldProperty> newSubProperties) {
			for (String key : newSubProperties.keySet()) {
				if (subProperties.get(key) == null)
					subProperties.put(key, newSubProperties.get(key));
			}
		}

		public List<FieldProperty> getListElements() {
			return listElements;
		}

		public String getAttribute(String key) {
			return attributes.get(key);
		}

		public String getName() {
			return attributes.get("name");
		}

		public String getElementName() {
			return elementName;
		}

		public void setElementName(String elementName) {
			this.elementName = elementName;
		}

		public String getProperty() {
			return attributes.get("property");
		}

		public BulkInfo getBulkInfo() {
			return bulkInfo;
		}

		public void setBulkInfo(BulkInfo bulkInfo) {
			this.bulkInfo = bulkInfo;
		}

		public boolean isAttribute(String name) {
			return Boolean.valueOf(this.getAttribute(name));
		}

		// TODO modificare con gli elementi (non attributi) xml 2.0
		public boolean isNullable() {
			if (this.getAttribute("jsonvalidator") == null) {
				return true;
			}
			JsonObject element = (JsonObject) new JsonParser().parse(this.getAttribute("jsonvalidator"));
			if (element.get("required") != null) {
				return !element.get("required").getAsBoolean();
			}
			if (element.get("requiredWidget") != null) {
				return !element.get("requiredWidget").getAsBoolean();
			}
			return true;
		}

		public boolean isRadioGroupType() {
			return this.getAttribute("inputType") != null && this.getAttribute("inputType").equals("RADIOGROUP");
		}

		public boolean isCheckboxType() {
			return this.getAttribute("inputType") != null && this.getAttribute("inputType").equals("CHECKBOX");
		}

		@Override
		public String toString() {
			return "FieldProperty name :" + getName();
		}

		// TODO verificare la correctness
		public FieldProperty clone() {
			FieldProperty clone = new FieldProperty();

			clone.setBulkInfo(this.bulkInfo);
			clone.setElementName(this.elementName);
			clone.addAllAttribute(this.getAttributes());
			clone.addAllSubProperties(this.getSubProperties());
			for(FieldProperty fp : this.listElements) {
				clone.addListElement(fp);
			}

			return clone;
		}
	}

	public class FieldPropertySet implements Serializable {
		private String name;
		private final HashMap<String, FieldProperty> properties;
		private BulkInfo bulkInfo;
		private String label;
		private String key;
		private String override;
		private static final long serialVersionUID = 1L;

		public FieldPropertySet() {
			properties = new LinkedHashMap<String, FieldProperty>();
		}

		public FieldPropertySet(BulkInfo bulkinfo) {
			this();
			bulkInfo = bulkinfo;
		}

		public FieldPropertySet(BulkInfo bulkinfo, String name) {
			this(bulkinfo);
			bulkInfo = bulkinfo;
			this.name = name;
		}

		public void addFieldProperties(FieldPropertySet fieldpropertyset) {
			for (String key : fieldpropertyset.properties.keySet()) {
				properties.put(key, fieldpropertyset.properties.get(key));
			}
		}

		public void addFieldProperty(FieldProperty externalFieldProperty) {
			bulkInfo.completeFieldProperty(externalFieldProperty);
			if (properties.containsKey(externalFieldProperty.getName())) {
				FieldProperty localFieldProperty = properties.get(externalFieldProperty.getName());

				localFieldProperty.addAllAttribute(externalFieldProperty.attributes);
				localFieldProperty.addAllSubProperties(externalFieldProperty.subProperties);
				for(FieldProperty listElement : externalFieldProperty.getListElements()) {
					localFieldProperty.addListElement(listElement);
				}
			} else {
				properties.put(externalFieldProperty.getName(), externalFieldProperty);
			}
		}

		public void addColumnFieldProperty(FieldProperty columnfieldproperty) {
			addFieldProperty(columnfieldproperty);
		}

		public void addFindFieldProperty(FieldProperty fieldproperty) {
			addFieldProperty(fieldproperty);
		}

		public void addFormFieldProperty(FieldProperty fieldproperty) {
			addFieldProperty(fieldproperty);
		}

		public void addPrintFieldProperty(FieldProperty fieldproperty) {
			addFieldProperty(fieldproperty);
		}

		public BulkInfo getBulkInfo() {
			return bulkInfo;
		}

		public Collection<FieldProperty> getFieldProperties() {
			return properties.values();
		}

		public FieldProperty getFieldProperty(String name) {
			return properties.get(name);
		}

		public Map<String, FieldProperty> getFieldPropertyDictionary() {
			return properties;
		}

		public String getLabel() {
			return label;
		}

		public String getName() {
			return name;
		}

		public void setBulkInfo(BulkInfo bulkinfo) {
			bulkInfo = bulkinfo;
		}

		public void setLabel(String newLabel) {
			label = newLabel;
		}

		public void setName(String newName) {
			name = newName;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getOverride() {
			return override;
		}

		public void setOverride(String override) {
			this.override = override;
		}
	}
}
