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
  </#if>
</#macro>
{
"people" : [
    <#list peoplelist as person>
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
				"${propertyName?substring(startIndex)}" : <@displayValue propertyValue/>
				<#if property_has_next>,</#if>
			</#list>
		}
        <#if person_has_next>,</#if>
    </#list>
]
}
</@compress>