<@compress single_line=true>
{
"people" : [
		<#if members??>
			<#list members as member>
				"${member.properties['cm:userName']}"
			<#if member_has_next >,</#if>	
			</#list>	
		</#if>
	]
}
</@compress>