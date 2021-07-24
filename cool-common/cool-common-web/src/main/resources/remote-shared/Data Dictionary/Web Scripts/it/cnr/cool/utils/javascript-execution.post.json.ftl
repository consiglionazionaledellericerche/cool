{
	"command": "${jsonUtils.encodeJSONString(command)}",
	"logs": ${jsonUtils.toJSONString(logs)},
	<#if error?? >
		"error": "${jsonUtils.encodeJSONString(error)}"
	<#else>
		"output": ${data}
	</#if>
}