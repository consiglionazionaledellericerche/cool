{
	<#if group??>"nodeRef": "${group.nodeRef}",</#if>
	<#if group??>"displayName": "${group.properties['cm:authorityDisplayName']}",</#if>
	"success" : ${esito?string}
}