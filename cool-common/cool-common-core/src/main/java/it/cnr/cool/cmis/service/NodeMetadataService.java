package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.model.PolicyType;
import it.cnr.cool.exception.CoolClientException;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.util.StringUtil;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIdDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeMetadataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeMetadataService.class);

	public static final String RE_XML_PROP = "[^\\x09\\x0A\\x0D\\x20-\\xD7FF\\xE000-\\xFFFD\\x10000-x10FFFF\\u2018\\u2019\\u201c\\u201d]";

	private String datePattern;
	@Autowired
	private ACLService aclService;
	@Autowired
	private CMISService cmisService;

	private Map<String, Object> internalPopulateMetadataFromRequest(
			Map<String, ?> reqProperties,
			List<PropertyDefinition<?>> propertyDefinitions,
			HttpServletRequest request) throws ParseException {
		final boolean debug = LOGGER.isDebugEnabled();
		DecimalFormat nf = (DecimalFormat) NumberFormat
				.getInstance(CMISService.DEFAULT_LOCALE);
		nf.setParseBigDecimal(true);
		Map<String, Object> properties = new HashMap<String, Object>();
		for (PropertyDefinition<?> propertyDefinition : propertyDefinitions) {
			if (propertyDefinition.getCardinality().equals(Cardinality.MULTI)) {
				String[] propertyValue = getParameterValues(
						propertyDefinition.getId(), reqProperties, request);
				if (isParameterPresent(propertyDefinition.getId(),
						reqProperties, request)) {
					if (debug)
						LOGGER.debug(propertyDefinition.getId() + " has value "
								+ propertyValue);
					List<Serializable> value = new ArrayList<Serializable>();
					for (int i = 0; i < propertyValue.length; i++) {
						if (propertyValue[i] != null
								&& propertyValue[i].length() > 0) {
							if (propertyDefinition instanceof PropertyDateTimeDefinition) {
								value.add(parseDate(propertyValue[i]));
							} else if (propertyDefinition instanceof PropertyBooleanDefinition) {
								value.add(Boolean.valueOf(propertyValue[i]
										.equals("on")
										|| propertyValue[i].equals("true")));
							} else if (propertyDefinition instanceof PropertyIntegerDefinition) {
								value.add(Integer.valueOf(propertyValue[i]));
							} else if (propertyDefinition instanceof PropertyStringDefinition || 
									propertyDefinition instanceof PropertyIdDefinition) {
								value.add(propertyValue[i].replaceAll(
										RE_XML_PROP, " "));
							} else if (propertyDefinition instanceof PropertyDecimalDefinition) {
								value.add(nf.parse(propertyValue[i],
										new ParsePosition(0)));
							} else {
								throw new CoolException(
										"Failed to get parameter value:"
												+ propertyDefinition.getId());
							}
						}
					}
					properties.put(propertyDefinition.getId(), value);
				}
			} else {
				String propertyValue = getParameter(propertyDefinition.getId(),
						reqProperties, request);
				if (isParameterPresent(propertyDefinition.getId(),
						reqProperties, request)) {
					if (debug)
						LOGGER.debug(propertyDefinition.getId() + " has value "
								+ propertyValue);
					Serializable value = null;
					if (propertyValue != null && propertyValue.length() > 0) {
						if (propertyDefinition instanceof PropertyDateTimeDefinition) {
							value = parseDate(propertyValue);
						} else if (propertyDefinition instanceof PropertyBooleanDefinition) {
							value = Boolean.valueOf(propertyValue.equals("on")
									|| propertyValue.equals("true"));
						} else if (propertyDefinition instanceof PropertyIntegerDefinition) {
							if (propertyValue.length() > 0)
								value = Integer.valueOf(propertyValue);
						} else if (propertyDefinition instanceof PropertyStringDefinition
								|| propertyDefinition instanceof PropertyIdDefinition) {
							value = propertyValue.replaceAll(RE_XML_PROP, " ");
						} else if (propertyDefinition instanceof PropertyDecimalDefinition) {
							value = nf.parse(propertyValue,
									new ParsePosition(0));
						} else {
							throw new CoolException(
									"Failed to get parameter value:"
											+ propertyDefinition.getId());
						}
					}
					properties.put(propertyDefinition.getId(), value);
				}
			}
		}
		return properties;
	}

	private String[] getParameterValues(String key,
			Map<String, ?> reqProperties, HttpServletRequest request) {
		if (reqProperties != null) {
			if (reqProperties.get(key) == null)
				return null;
			if (reqProperties.get(key).getClass().isArray()) {
				return (String[]) reqProperties.get(key);
			} else
				return new String[] { (String) reqProperties.get(key) };
		}
		return request.getParameterValues(key);
	}

	private String getParameter(String key, Map<String, ?> reqProperties,
			HttpServletRequest request) {
		if (reqProperties != null) {
			if (reqProperties.get(key) != null
					&& reqProperties.get(key).getClass().isArray()) {
				for (String value : (String[]) reqProperties.get(key)) {
					if (value != null && value.length() > 0)
						return value;
				}
				return null;
			} else
				return (String) reqProperties.get(key);
		}
		String[] values = request.getParameterValues(key);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				if (value.length() > 0)
					return value;
			}
		}
		return null;
	}

	private Boolean isParameterPresent(String key,
			Map<String, ?> reqProperties, HttpServletRequest request) {
		if (reqProperties != null)
			return reqProperties.containsKey(key);
		return request.getParameterMap().containsKey(key);
	}

	private Serializable parseDate(String propertyValue) throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern,
				CMISService.DEFAULT_LOCALE);

		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(simpleDateFormat.parse(propertyValue));
		} catch (ParseException _ex) {
			cal.setTime(StringUtil.CMIS_DATEFORMAT.parse(propertyValue));
		}
		return cal;

	}

	public Map<String, Object> populateMetadataType(Session cmisSession,
			Map<String, ?> reqProperties, HttpServletRequest request)
			throws ParseException {
		return populateMetadataType(
				cmisSession,
				reqProperties,
				getParameter(PropertyIds.OBJECT_TYPE_ID, reqProperties, request),
				request);
	}

	public Map<String, Object> populateMetadataType(Session cmisSession,
			Map<String, ?> reqProperties, String typeId,
			HttpServletRequest request) throws ParseException {
		ObjectType type = cmisSession.getTypeDefinition(typeId);
		List<PropertyDefinition<?>> propertyDefinitions = new ArrayList<PropertyDefinition<?>>();
		propertyDefinitions.addAll(type.getPropertyDefinitions().values());
		return internalPopulateMetadataFromRequest(reqProperties,
				propertyDefinitions, request);
	}

	public Map<String, Object> populateMetadataAspectFromRequest(
			Session cmisSession, Map<String, ?> reqProperties, HttpServletRequest request)
			throws ParseException {
		return populateMetadataAspect(cmisSession, reqProperties, request);
	}
	
	public Map<String, Object> populateMetadataAspectFromRequest(
			Session cmisSession, HttpServletRequest request)
			throws ParseException {
		return populateMetadataAspect(cmisSession, null, request);
	}

	public Map<String, Object> populateMetadataTypeFromRequest(
			Session cmisSession, HttpServletRequest request)
			throws ParseException {
		return populateMetadataType(cmisSession, null,
				request.getParameter(PropertyIds.OBJECT_TYPE_ID), request);
	}

	public Map<String, Object> populateMetadataAspect(Session cmisSession,
			Map<String, ?> reqProperties, HttpServletRequest request)
			throws ParseException {
		List<String> aspectNames = new ArrayList<String>();
		List<PropertyDefinition<?>> propertyDefinitions = new ArrayList<PropertyDefinition<?>>();
		if (getParameterValues(PolicyType.ASPECT_REQ_PARAMETER_NAME,
				reqProperties, request) != null) {
			aspectNames.addAll(Arrays.asList(getParameterValues(
					PolicyType.ASPECT_REQ_PARAMETER_NAME, reqProperties,
					request)));

			String[] aspects = getParameterValues(PolicyType.ADD_REMOVE_ASPECT_REQ_PARAMETER_NAME, reqProperties,
					request);

			aspectNames.addAll(PolicyType.getAspectToBeAdd(aspects));
			if (aspectNames != null && !aspectNames.isEmpty()) {
				for (String aspectName : aspectNames) {
					propertyDefinitions.addAll(cmisSession
							.getTypeDefinition(aspectName)
							.getPropertyDefinitions().values());
				}
			}
			
		}
		Map<String, Object> result = internalPopulateMetadataFromRequest(reqProperties,
				propertyDefinitions, request);
		result.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspectNames);
		return result;
	}

	public Map<String, Object> populateMetadataFromRequest(Session cmisSession,
			String typeId, HttpServletRequest request) throws ParseException {
		return populateMetadataType(cmisSession, null, typeId, request);
	}

	public Map<String, Object> populateMetadataFromRequest(Session cmisSession,
			HttpServletRequest request) throws ParseException {
		return populateMetadataTypeFromRequest(cmisSession, request);
	}

	public CmisObject updateObjectProperties(Map<String, ?> reqProperties,
			Session cmisSession, HttpServletRequest request)
			throws ParseException {
			List<String> aspectNames = null;
			Map<String, Object> properties = populateMetadataType(cmisSession,
					reqProperties, request);
			Map<String, Object> aspectProperties = populateMetadataAspect(
					cmisSession, reqProperties, request);
			String objectId = getParameter(PropertyIds.OBJECT_ID,
					reqProperties, request);
			String objectParentId = getParameter(PropertyIds.PARENT_ID,
					reqProperties, request);
			String objectTypeId = getParameter(PropertyIds.OBJECT_TYPE_ID,
					reqProperties, request);
			String inheritedPermission = getParameter(ACLService.PARAM_INHERITED_PERMISSION,
					reqProperties, request);
			if (getParameterValues(PolicyType.ASPECT_REQ_PARAMETER_NAME,
					reqProperties, request) != null) {
				aspectNames = Arrays.asList(getParameterValues(
						PolicyType.ASPECT_REQ_PARAMETER_NAME, reqProperties,
						request));
			}

		return updateObjectProperties(cmisSession, cmisService.getCurrentBindingSession(request), objectId,
				objectTypeId, objectParentId, inheritedPermission,
				aspectNames, aspectProperties, properties);

	}

	private CmisObject updateObjectProperties(Session cmisSession, BindingSession bindingSession,
			String objectId, String objectTypeId, String objectParentId, String inheritedPermission,
			List<String> aspectNames, Map<String, Object> aspectProperties,
			Map<String, Object> properties) {

		try {
			CmisObject cmisObject = null;
			properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
			properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspectNames);
			properties.putAll(aspectProperties);
			if (objectId == null) {
				cmisObject = cmisSession.getObject(cmisSession.createDocument(properties,
						cmisSession.createObjectId(objectParentId), null, null));
			} else {
				cmisObject = cmisSession.getObject(objectId);
				cmisObject.updateProperties(properties);
			}
			if (inheritedPermission != null) {
				aclService.setInheritedPermission(bindingSession, 
						(String) cmisObject.getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), 
						Boolean.valueOf(inheritedPermission));
			}			
			return cmisObject;
		} catch (CmisContentAlreadyExistsException _ex) {
			throw new CoolClientException("message.file.alredy.exists", _ex);
		}
	}


	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

}
