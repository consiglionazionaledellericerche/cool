<@compress single_line=true>
<#macro displayValue value>
  <#if value?is_boolean>
	${value?string}
  <#elseif value?is_number>
	${value?string('0')}
  <#elseif value?is_date>
	"${xmldate(value)}"	
  <#elseif value?is_string>
	"${jsonUtils.encodeJSONString(value?string)}"
  <#else>
	[
	<#list value as singleValue>
	"${jsonUtils.encodeJSONString(singleValue)}"<#if singleValue_has_next>,</#if>	
    </#list>
	]
  </#if>
</#macro>

{
	"enabled": ${people.isAccountEnabled(person)?string("true","false")},
	<#list person.properties?keys as property>
			<#if person.properties[property]??>
				<#assign propertyValue = person.properties[property]>
			<#else>
				<#assign propertyValue = "">
			</#if>
			<#assign propertyName = shortQName(property)>
			<#assign startIndex = (propertyName?index_of(":") + 1)>					
			"${propertyName?substring(startIndex)}" : <#if propertyName == "cm:homeFolder" && propertyValue??>"${propertyValue.nodeRef}"<#else><@displayValue propertyValue/></#if>
			<#if property_has_next>,</#if>
	</#list>
	<#if arggroups??>
		<#if arggroups?is_string>
			,"groups": [
			<#list groups as g>
				<#assign authName = g.properties["cm:authorityName"]>
				<#if authName?starts_with("GROUP_site")><#assign displayName = authName?substring(6)><#else><#assign displayName = g.properties["cm:authorityDisplayName"]!authName?substring(6)></#if>
			{
				"itemName": "${authName}",
				"displayName": "${displayName}"
			}<#if g_has_next>,</#if>
			</#list>]
		</#if>
	</#if>
	,"immutability":
	{
		<#list immutability?keys as key>
			"${key}":<@displayValue immutability[key]/><#if key_has_next>,</#if>
		</#list>
	}
	,"capabilities":
	{
		<#list capabilities?keys as key>
			"${key}":<@displayValue capabilities[key]/><#if key_has_next>,</#if>
		</#list>
	}
}
</@compress>