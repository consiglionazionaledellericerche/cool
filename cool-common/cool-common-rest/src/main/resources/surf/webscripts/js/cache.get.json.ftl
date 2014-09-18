{
	<#if publicCaches??>
		<#list publicCaches as cache>
			"${cache.first}": ${cache.second},
		</#list>
	</#if>
	"baseUrl": "${url.context}",
	"redirectUrl": "${url.context}",
	<#if dataDictionary??>
	"dataDictionary": "${dataDictionary}",
	</#if>
	"debug": ${(!isProduction)?string}
}