{
	<#if publicCaches??>
		<#list publicCaches as cache>
			"${cache.first}": ${cache.second},
		</#list>
	</#if>
	"baseUrl": "${url.context}",
	"redirectUrl": "<#if context.properties["alfRedirectUrl"]??>${context.properties["alfRedirectUrl"]}<#else>${url.context}</#if>",
	<#if dataDictionary??>
	"dataDictionary": "${dataDictionary}",
	</#if>
	"debug": ${(!isProduction)?string}
}