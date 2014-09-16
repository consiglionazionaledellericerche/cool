<@compress single_line=true>
{
"groups" : [
	<#list grouplist as group>
	{<@groupJSON group=group/>}
	<#if args.maxItems?? && (group_index + 1)?string = args.maxItems><#break></#if>
	<#if group_has_next>,</#if>
	</#list>
]
}
</@compress>
<#macro groupJSON group>
<#local p=group.properties>
<#escape x as jsonUtils.encodeJSONString(x)>
	"authorityName": "${p.authorityName}",
	"authorityDisplayName": <#if p.authorityDisplayName??>"${p.authorityDisplayName}"<#else>null</#if>,
	"nodeRef": <#if group.nodeRef??>"${group.nodeRef}"<#else>null</#if>
</#escape>
</#macro>
