{
  <#-- Details of the response code -->
  "status" : 
  {
    "code" : "${status.code}",
    "name" : "${status.codeName}",
    "description" : "${status.codeDescription}"
  },  
  
  <#-- Exception details -->
  "message" : "${jsonUtils.encodeJSONString(status.message)}",  
  "exception" : "<#if status.exception??>${jsonUtils.encodeJSONString(status.exception.class.name)}<#if status.exception.message??> - ${jsonUtils.encodeJSONString(status.exception.message)}</#if></#if>",
  <#-- Details of the response code -->
  "keyMessage" : "<#if status.exception.keyMessage??>${status.exception.keyMessage}</#if>",
  
  <#-- Exception call stack --> 
  "callstack" : 
  [ 
  	  <#if status.exception??>""<@recursestack exception=status.exception/></#if> 
  ],
  
  <#-- Server details and time stamp -->
  "server" : "${server.edition?xml} v${server.version?xml} schema ${server.schema?xml}",
  "time" : "${date?datetime}"
  <#if status.exception.cause?? && status.exception.cause.errorContent??>
  ,"cmisErrorContent": "${jsonUtils.encodeJSONString(status.exception.cause.errorContent)}"
  </#if>
}

<#macro recursestack exception>
   <#if exception.cause??>
      <@recursestack exception=exception.cause/>
   </#if>
   <#if !exception.cause??>
      ,"${jsonUtils.encodeJSONString(exception?string)}"
      <#list exception.stackTrace as element>
      ,"${jsonUtils.encodeJSONString(element?string)}"
      </#list>
   <#else>
      ,"${jsonUtils.encodeJSONString(exception?string)}"
      ,"${jsonUtils.encodeJSONString(exception.stackTrace[0]?string)}"
   </#if>
</#macro>