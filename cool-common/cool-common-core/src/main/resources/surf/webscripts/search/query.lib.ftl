<#macro generatePropertyValue queryResult property>
<#compress>
<#if queryResult.getPropertyValueById(property.id)??>
	<#assign values = queryResult.getPropertyMultivalueById(property.id)/>
	<#assign value = queryResult.getPropertyValueById(property.id)/>
	<#if values?size gt 1>
	[
	<#list values as propertyValue>
	  <#if propertyValue?is_boolean>
		${propertyValue?string}
	  <#elseif propertyValue?is_number>
		${propertyValue?string('0')}
	  <#elseif calendarUtil.isGregorianCalendar(propertyValue)>
		"${xmldate(propertyValue.time)}"
	  <#elseif propertyValue?is_string>
		"${jsonUtils.encodeJSONString(propertyValue?string)}"
	  </#if>
	<#if propertyValue_has_next>,</#if></#list>
	]
	<#else>
	  <#if value?is_boolean>
		${value?string}
	  <#elseif value?is_number>
		${value?string('0')}
	  <#elseif calendarUtil.isGregorianCalendar(value)>
		"${xmldate(value.time)}"
	  <#elseif value?is_string>
		"${jsonUtils.encodeJSONString(value?string)}"
	  </#if>	
	</#if>
<#else>
""
</#if>
</#compress>

</#macro>
<#macro generatePropertyCMISValue cmisObject prop>
<#compress>
	<#if prop.isMultiValued()>
		<#if cmisObject.getPropertyValue(prop.definition.id)??>
			<#list cmisObject.getPropertyValue(prop.definition.id) as value>
				<#if prop.type.value() == "string" || prop.type.value() == "id">
		"${jsonUtils.encodeJSONString(value?string)}"
				<#elseif prop.type.value() == "datetime">
		"${xmldate(value.time)}"
				<#elseif prop.type.value() == "integer">
		${value?string('0')}
		        <#elseif prop.type.value() == "decimal">
        "${value}"
				<#elseif prop.type.value() == "boolean">
		${value?string}
				</#if>
			<#if value_has_next>,</#if></#list>	  
		</#if>	
	<#else>
		<#if cmisObject.getPropertyValue(prop.definition.id)??>
			<#if prop.type.value() == "string" || prop.type.value() == "id">
	"${jsonUtils.encodeJSONString(cmisObject.getPropertyValue(prop.definition.id)?string)}"
			<#elseif prop.type.value() == "datetime">
	"${xmldate(cmisObject.getPropertyValue(prop.definition.id).time)}"
			<#elseif prop.type.value() == "integer">
	${cmisObject.getPropertyValue(prop.definition.id)?string('0')}
			<#elseif prop.type.value() == "decimal">
	"${cmisObject.getPropertyValue(prop.definition.id)}"
			<#elseif prop.type.value() == "boolean">
	${cmisObject.getPropertyValue(prop.definition.id)?string}
			</#if>  
		<#else>
			""
		</#if>
	</#if>	
</#compress>
</#macro>
<#function getPropertyValue source type key>
	<#if type=="query">
		<#return source.getPropertyValueById(key)>
	<#else>
		<#return source.getPropertyValue(key)>
	</#if>	
</#function>
<#macro generateRelationships cmisObjectSource type>
	<#if relationships??>
	,"relationships":{
		<#list relationships?keys as rel>
			<#assign result = relationships[rel]>
			<#if result[getPropertyValue(cmisObjectSource, type,"cmis:objectId")]??>
				"${rel}":[				
				<#assign childs = result[getPropertyValue(cmisObjectSource, type, "cmis:objectId")]>
				<#list childs as children>{		
					<#list children.properties as prop>
						"${prop.id}":
							<#if prop.isMultiValued()>[</#if>
							<@generatePropertyCMISValue children prop/>
							<#if prop.isMultiValued()>]</#if>
							<#if prop_has_next>,</#if>
					</#list><@generateRelationships children "object"/>}<#if children_has_next>,</#if>
				</#list>
				]<#if rel_has_next && result[getPropertyValue(cmisObjectSource, type,"cmis:objectId")]??>,</#if>	
			</#if>
		</#list>
	}
	</#if>
</#macro>
<#macro main>
<@compress single_line=true>
{
	"totalNumItems":${totalNumItems?string('0')},
	"maxItemsPerPage":${maxItemsPerPage?string('0')},
	"activePage":${activePage?string('0')},
	"hasMoreItems": ${hasMoreItems?string},
	"items":
	[
		<#list models as queryResult>
		{
		  <#assign qr = (queryResult.getClass().getSimpleName() == "QueryResultImpl")>
			<#list queryResult.properties as property>
				"${property.id}":
					<#if qr>
						<@generatePropertyValue queryResult property/>
					<#else>
						<#if property.isMultiValued()>[</#if>
						<@generatePropertyCMISValue queryResult property/>
						<#if property.isMultiValued()>]</#if>
					</#if>
					<#if property_has_next>,</#if>
			</#list>
			<#if queryResult.allowableActions??>
				,"allowableActions":[
				<#list queryResult.allowableActions.allowableActions as action>
					"${action}"<#if action_has_next>,</#if>
				</#list>
				]
			</#if>
			<#if queryResult.aspects??>
				,"aspect" : [
				<#list queryResult.aspects as aspect>
					"${aspect.id}"<#if aspect_has_next>,</#if>
				</#list>					
				]
			</#if>
			<#if qr> 	
				<@generateRelationships queryResult "query"/>
			<#else>
				<@generateRelationships queryResult "object"/>
			</#if>				
		}<#if queryResult_has_next>,</#if>
		</#list>
	]
	<#if facets?? && facets?keys?size &gt; 0>
	<#compress>,"facets":
	[
	<#list facets?keys as facetName>
	{
		<#assign result=facets[facetName]>
		"${jsonUtils.encodeJSONString(facetName)}":[<#list result?keys as key>{"key":"${jsonUtils.encodeJSONString(key)}","value":"${result[key]?string('0')}"}<#if key_has_next>,</#if></#list>]
	}<#if facetName_has_next>,</#if>
	</#list>
	]</#compress>
	</#if>
}
</@compress>
</#macro>

<@main />
