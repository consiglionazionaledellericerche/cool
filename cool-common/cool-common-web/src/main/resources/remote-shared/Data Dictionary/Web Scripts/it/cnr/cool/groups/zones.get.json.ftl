<#-- get zones -->
<@compress single_line=true>
<#if zones??>
  ${jsonUtils.toJSONString(zones)}
</#if>
</@compress>
