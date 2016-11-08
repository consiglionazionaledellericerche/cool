<@compress single_line=true>
{
  "tree" : ${jsonUtils.toJSONString(tree)},
  "detail" : {
	<#list detail?keys as groupName>
	"${groupName}" : {
		"shortName" : "${detail[groupName].shortName}",
		"fullName" : "${detail[groupName].fullName}",
		"displayName" : "${detail[groupName].displayName}",
		"groupNodeRef" : "${detail[groupName].groupNodeRef}"
	}
	<#if groupName_has_next >,</#if>	
	</#list>
   }
}
</@compress>