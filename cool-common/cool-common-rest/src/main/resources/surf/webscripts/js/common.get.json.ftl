{
	<#-- bridge SpringSurf - javascript -->
	<#-- si puo' integrare in bootstrap.ftl ? -->
	"User": {
	  <#if !context.user.guest!>
	  "id": "${context.user.id}",
	  <#if context.user.lastName??>
	  "lastName": "${context.user.lastName}",
	  </#if>
	  <#if context.user.firstName??>
	  "firstName": "${context.user.firstName}",
	  </#if>
	  <#if context.user??>
	  "groups":[
	  <#list context.user.groups as group>
	   "${group.group_name}"
	   <#if group_has_next>,</#if>
	  </#list>
	  ],
	  <#if context.user.matricola??>
	  "matricola": "${context.user.matricola}",
	  </#if>
	  <#if context.user.sesso??>
	  "sesso": "${context.user.sesso}",
	  </#if>
	  <#if context.user.email??>
	  "email": "${context.user.email}",
	  </#if>	  
	  </#if>
	  <#if context.user.homeFolder??>
	  "homeFolder": "${context.user.homeFolder}",
	  </#if>
	  <#if context.user.codicefiscale??>
	  "codiceFiscale": "${context.user.codicefiscale}",
	  </#if>
	  "isAdmin": ${context.user.admin?string("true", "false")},
	  </#if>
	  "isGuest": ${context.user.guest?string("true", "false")}
	},
	"now": "${cmisDateFormat.format(.now)}",
	<#if ga??>
          "ga": "${ga}",
	</#if>
	<#if pageId??>
		"pageId": "${pageId}",
	</#if>	
	<#if caches??>
		<#list caches as cache>
			"${cache.first}": <#if cache.second??>${cache.second}<#else>""</#if>,
		</#list>
	</#if>
	"version": "${artifact_version}",
	"bootstrapVersion": "2"
}
