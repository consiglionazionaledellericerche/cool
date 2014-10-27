{
	<#if publicCaches??>
		<#list publicCaches as cache>
			<#if cache.second??>
			"${cache.first}": ${cache.second},
			</#if>
		</#list>
	</#if>
	"baseUrl": "${url.context}",
	"redirectUrl": "${url.context}",
	<#if dataDictionary??>
	"dataDictionary": "${dataDictionary}",
	</#if>
	"debug": ${(!isProduction)?string}
}