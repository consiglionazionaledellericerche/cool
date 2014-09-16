{
	<#-- bridge SpringSurf - javascript -->
	<#-- si puo' integrare in bootstrap.ftl ? -->
	"User": {
	  <#if !context.user.isGuest>
	  "id": "${context.user.id}",
	  "lastName": "${context.user.lastName}",
	  "firstName": "${context.user.firstName}",
	  <#if context.user.nativeUser??>
	  "groups":[
	  <#list context.user.nativeUser.groups as group>
	   "${group.itemName}"
	   <#if group_has_next>,</#if>
	  </#list>
	  ],
	  <#if context.user.nativeUser.matricola??>
	  "matricola": "${context.user.nativeUser.matricola}",
	  </#if>
	  <#if context.user.nativeUser.sesso??>
	  "sesso": "${context.user.nativeUser.sesso}",
	  </#if>
	  </#if>
	  <#if context.user.nativeUser.email??>
	  "email": "${context.user.email}",
	  </#if>
	  <#if context.user.nativeUser.codicefiscale??>
	  "codiceFiscale": "${context.user.nativeUser.codicefiscale}",
	  </#if>
	  "isAdmin": ${context.user.isAdmin?string},
	  </#if>
	  "isGuest": ${context.user.isGuest?string}
	},
	"now": "${cmisDateFormat.format(.now)}",
	<#if caches??>
		<#list caches as cache>
			"${cache.first}": ${cache.second},
		</#list>
	</#if>
	"version": "${artifact_version}",
	"bootstrapVersion": "2"
}