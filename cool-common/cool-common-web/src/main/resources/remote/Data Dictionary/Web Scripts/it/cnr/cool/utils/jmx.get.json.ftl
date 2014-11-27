<@compress single_line=true>
	{
	<#list mbeans as mbean>
		"${mbean.name}" : {
		<#list mbean.attributes?values as attribute>
				"${attribute.name}" : "${attribute.value}"
		<#if attribute_has_next>,</#if>
		</#list>	
		}
	<#if mbean_has_next>,</#if>
	</#list>
	}
</@compress>