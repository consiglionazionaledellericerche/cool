<@compress single_line=true>
{
"people" : [
	<#list peoplelist as person>
	{<@personJSON person=person/>}
	<#if args.maxItems?? && (person_index + 1)?string = args.maxItems><#break></#if>
	<#if person_has_next>,</#if>
	</#list>
]
}
</@compress>
<#macro personJSON person>
<#local p=person.properties>
<#escape x as jsonUtils.encodeJSONString(x)>
	"userName": "${p.userName}",
	"firstName": <#if p.firstName??>"${p.firstName}"<#else>null</#if>,
	"lastName": <#if p.lastName??>"${p.lastName}"<#else>null</#if>,
	"email": 
	<#switch p.email>
	  <#case "nomail">
	     <#if p['cnrperson:emailesterno']??>
	     	"${p['cnrperson:emailesterno']}"
	     <#else>
		     <#if p['cnrperson:emailcertificatoperpuk']??>
		     	"${p['cnrperson:emailcertificatoperpuk']}"
		     <#else>
		     	""	
		     </#if>
		 </#if>    	     
	     <#break>
	  <#default>
	     "${p.email}"
	</#switch>,
	"nodeRef": <#if person.nodeRef??>"${person.nodeRef}"<#else>null</#if>
</#escape>
</#macro>
