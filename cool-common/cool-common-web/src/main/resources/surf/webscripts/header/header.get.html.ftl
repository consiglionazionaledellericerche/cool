<#function mainPage page>
  <#if page.properties?? && page.properties['main-page']??>
    <#return page.properties['main-page']>
  <#else>
    <#return page.id>
  </#if>
</#function>
<div class="navbar navbar-fixed-top container">
  <div class="navbar-inner">
    <div class="container-fluid">
      <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </a>
      <a class="logo" href="${url.context}"></a>
      <div class="nav-collapse collapse">
        <p id="userInfo" class="navbar-form pull-right">
          <#if !context.user.isGuest>
          <a href="#" class="navbar-link">${user.fullName}</a>
          <a href="${url.context}/dologout" class="btn btn-inverse btn-mini" id="logout">logout</a>
          <#else>
          anonymous
          </#if>
        </p>
        <ul class="nav hidden-important">
          <#list pages as page>
            <#if ! context.user.isGuest >
              <#assign currentUser = context.user.nativeUser>
            </#if>
            <#if permission.isAuthorized(page.id, "GET", currentUser) >
              <#assign submenu = page.properties['format-id']?string?split("/")>
              <li class="page<#if (context.page.id = page.id||mainPage(context.page) = page.id)> active</#if>" <#if (submenu?size > 1)>data-submenu="${submenu[1]}"</#if>>


                <a id="${page.id}" href="${url.context}/${page.id}">${message('page.'+page.id)}</a>

              </li>
            </#if>
          </#list>
          <#if !context.user.isGuest>
          <li class="hide dropdown page<#if context.page.id = "workflow" > active</#if>" id="workflow">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">${msg("workflow")} <b class="caret"></b></a>
            <ul class="dropdown-menu">
              <li class="nav-header">${msg("workflows")}</li>
            </ul>
          </li>
          <li class="divider-vertical"></li>
          <li>
            <form id="search" method="POST" action="${url.context}/search" class="form-search">
              <div class="input-append">
                <input type="text" name="query" class="search-query input-small">
                <button type="submit" class="btn"><i class="icon-search"></i> ${msg("button.freesearch.find")}</button>
              </div>
            </form>
          </li>
          </#if>
        </ul>
      </div><!--/.nav-collapse -->
    </div>
  </div>
</div><!--/.navbar-fixed-top -->