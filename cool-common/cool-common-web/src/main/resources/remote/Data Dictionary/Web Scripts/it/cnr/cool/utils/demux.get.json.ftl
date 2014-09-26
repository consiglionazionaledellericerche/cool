<#-- MACROS inspired by atomentry.lib.atom.ftl -->
<#macro allowableactions node>
<#assign typedef = cmistype(node)>
<#assign flag = false>
[
<#list typedef.actionEvaluators?values as actionevaluator>
  <@allowableaction node actionevaluator/>
</#list>
]
</#macro>

<#macro allowableaction node actionevaluator>
	<#if actionevaluator.isAllowed(node.nodeRef)>
	<#if flag>,</#if>
		"${actionevaluator.action.name()}"
	<#assign flag = true>
	</#if>
</#macro>

{
	"totalNumItems": ${totalNumItems},
	"maxItemsPerPage": ${maxItemsPerPage},
	"activePage": ${activePage},
	"hasMoreItems": ${hasMoreItems?string},
	"items": [
		<#list models as node>
			{
				<#list node.json.properties?keys as prop>
					"${prop}": ${node.json.properties[prop]?string},
				</#list>
				"aspect": [
					<#list node.json.aspects as aspect>
						"${aspect}"
						<#if aspect_has_next>,</#if>
					</#list>
				],
				"allowableActions": <@allowableactions node.wrapped/>
			}
			<#if node_has_next>,</#if>
		</#list>
	]
}