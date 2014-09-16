<#macro mainJson>
{
  <#assign exception = Request["javax.servlet.error.exception"]/>
  "keyMessage" : "<#if message(exception.class.name) != exception.class.name>${message(exception.class.name)}<#else>${exception.getMessage()}</#if>"
}
</#macro>
<#macro mainHtml>
	<div id="error">
		<div class="alert alert-error span7 offset2">
		<center>
			<strong>
				<#if message(exception.class.name) != exception.class.name>
					${message(exception.class.name)}
				<#else>
					${exception.getMessage()}
				</#if>
			</strong>
		</center>
		</div>
	</div>
</#macro>
