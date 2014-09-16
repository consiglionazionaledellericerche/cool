<#macro generatePropertyCMISValue cmisObject prop>
<#compress>
	<#if prop.isMultiValued()>[
		<#if cmisObject.getPropertyValue(prop.definition.id)??>
			<#list cmisObject.getPropertyValue(prop.definition.id) as value>
				<#if prop.type.value() == "string" || prop.type.value() == "id">
		"${jsonUtils.encodeJSONString(value?string)}"
				<#elseif prop.type.value() == "datetime">
				<#if args.dateFormat??>
		"${value.time?string(args.dateFormat)}"
				<#else>
		"${xmldate(value.time)}"
				</#if>
				<#elseif prop.type.value() == "integer">
		${value?string('0')}
				<#elseif prop.type.value() == "decimal">
		"${jsonUtils.encodeJSONString(value)}"
				<#elseif prop.type.value() == "boolean">
		${value?string}
				</#if>
			<#if value_has_next>,</#if></#list>	  
		]</#if>
	<#else>
		<#if cmisObject.getPropertyValue(prop.definition.id)??>
			<#if prop.type.value() == "string" || prop.type.value() == "id">
	"${jsonUtils.encodeJSONString(cmisObject.getPropertyValue(prop.definition.id)?string)}"
			<#elseif prop.type.value() == "datetime">
				<#if args.dateFormat??>
	"${cmisObject.getPropertyValue(prop.definition.id).time?string(args.dateFormat)}"
				<#else>
	"${xmldate(cmisObject.getPropertyValue(prop.definition.id).time)}"
				</#if>
			<#elseif prop.type.value() == "integer">
	${cmisObject.getPropertyValue(prop.definition.id)?string('0')}
			<#elseif prop.type.value() == "decimal">
		"${jsonUtils.encodeJSONString(cmisObject.getPropertyValue(prop.definition.id))}"
			<#elseif prop.type.value() == "boolean">
	${cmisObject.getPropertyValue(prop.definition.id)?string}
			</#if>
		<#else>
		""	  
		</#if>
	</#if>	
</#compress>
</#macro>
{
	<#list cmisObject.properties as property>
		"${property.id}":<@generatePropertyCMISValue cmisObject property/><#if property_has_next>,</#if>
	</#list>
	,"aspects":[<#list cmisObject.aspects as aspect>"${aspect.id}"<#if aspect_has_next>,</#if></#list>]
}