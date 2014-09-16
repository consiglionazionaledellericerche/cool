<#assign bgImageUrl = url.context + "/res/images/logo.png">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>${page.title}</title>
</head>
<body>
<table width="100%" height="100%" border="0" style="background-image:url('${bgImageUrl}'); background-repeat:no-repeat;">
	<tr>
		<td valign="center" align="middle">
			This is an unconfigured page.
			<br/><br/>
			<b>${page.title}</b>
			<br/>
			(${page.id})
			<br/><br/>
			Please check the page configuration.
			<br/><br/>
			<a href="${url.context}">Go to the home page</a>
		</td>
	</tr>
</table>

</body>
</html>

