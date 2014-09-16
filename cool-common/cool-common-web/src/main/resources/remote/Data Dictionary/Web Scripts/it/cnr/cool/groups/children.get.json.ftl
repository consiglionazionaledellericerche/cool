<#-- get children -->
<@compress single_line=true>
<#macro authorityJSON authority>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"attr": {
		"allowableActions": [
			<#if authority.allowableActions??>
				<#list authority.allowableActions as allowableAction>
					"${allowableAction}"
					<#if allowableAction_has_next>,</#if>	
				</#list>		
			</#if>
		],
		"id": "${authority.nodeRef.toString()}",
		"type": "${authority.authorityType}",
		"shortName": "${authority.shortName}",
		"fullName": "${authority.fullName}",
		"displayName": "${authority.displayName}",
		"authorityId": "<#if authority.authorityType == "USER">${authority.shortName}<#else>${authority.fullName}</#if>"
	},
	"data": "${authority.displayName}",
	"state": "closed"
}
</#escape>
</#macro>
<#import "../generic-paged-results.lib.ftl" as genericPaging />
<#if url.match == "/authority/groups/root">
{
	"id": "${group.nodeRef.toString()}",
	"allowableActions": [
			<#if group.allowableActions??>
				<#list group.allowableActions as allowableAction>
					"${allowableAction}"
					<#if allowableAction_has_next>,</#if>	
				</#list>		
			</#if>
		]	
}
<#else>
[
	<#list children as c>
		<@authorityJSON authority=c />
	<#if c_has_next>,</#if>
	</#list>
]
<#--<@genericPaging.pagingJSON />-->
</#if>
</@compress>