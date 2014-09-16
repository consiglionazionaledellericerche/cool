<#list facets?keys as facetName>
	<#assign result=facets[facetName]>
	<table class="facettable">
	<#list 0..result?keys?size as i>
		<#if i % 3 = 0>
			<tr>
				<#list 0..2 as colonne>
					<#if (i + colonne) &lt; result?keys?size>
						<td><a href='${url.context}/search?solrOpts={"facets":{"${args["facet.field"]}":{prefix:"${result?keys[(i + colonne)]?xml}"}}}' id="a_${i + colonne}">${result?keys[(i + colonne)]}</a> (${result?values[(i + colonne)]})</td>
					</#if>	
				</#list>	
			</tr>
		</#if>		
	</#list>		
	</table>
</#list>
