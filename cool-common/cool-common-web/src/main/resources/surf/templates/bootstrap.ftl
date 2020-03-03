<!DOCTYPE html>
<html lang="it">
  <head>
    <meta charset="utf-8">
    <meta name="google" content="notranslate" />
  <title>
    ${message("main.title")}
    <#if message("page."+page.id)??>
      - ${message("page."+page.id)}
    </#if>
  </title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="${message("main.title")}">
    <meta name="author" content="">

    <!-- custom style -->
    <link href="${url.context}/res/css/cool/cool.css?v=${artifact_version}" rel="stylesheet">
    <link href="${url.context}/res/css/style.css?v=${artifact_version}" rel="stylesheet">
    <link href="${url.context}/res/css/custom.css?v=${artifact_version}" rel="stylesheet">

    <!-- Fav and touch icons -->
    <link rel="shortcut icon" href="${url.context}/res/img/favicon.ico">
    <link rel="apple-touch-icon" sizes="144x144" href="${url.context}/res/img/apple-touch-icon-144x144.png">
    <link rel="apple-touch-icon" sizes="114x114" href="${url.context}/res/img/apple-touch-icon-114x114.png">
    <link rel="apple-touch-icon" sizes="72x72" href="${url.context}/res/img/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" href="${url.context}/res/img/apple-touch-icon-57x57.png">

    <!-- add support for ECMAScript 5 functionalities -->
    <!--[if lt IE 9]>
    <script src="${url.context}/res/js/thirdparty/fallback/json2.js"></script>
    <script src="${url.context}/res/js/thirdparty/fallback/html5.js"></script>
    <script src="${url.context}/res/js/thirdparty/fallback/es5-shim.js"></script>
    <script src="${url.context}/res/js/thirdparty/fallback/es5-sham.js"></script>
    <script src="${url.context}/res/js/thirdparty/fallback/respond.src.js"></script>
    <![endif]-->

    <!-- add support for FormData and XHR2 (FileUpload) -->
    <!--[if lt IE 10]>
    <script src="${url.context}/res/js/thirdparty/fallback/form_data.js"></script>
    <![endif]-->

    <script>
    // you can register settings like this before require.js is loaded
    var require = {
      baseUrl: '${url.context}/res/js',
      urlArgs: "v=" + "${artifact_version}",
      waitSeconds: 90,
      paths: {
        'jquery.xmleditor': 'thirdparty/xmleditor/jquery.xmleditor-cnr',
        'jquery-ui': 'thirdparty/xmleditor/lib/jquery-ui.min',
        cycle: 'thirdparty/xmleditor/lib/cycle',
        vkbeautify: 'thirdparty/xmleditor/lib/vkbeautify',
        xsd2json: 'thirdparty/xmleditor/xsd/xsd2json-cnr',
        'ace/range': 'thirdparty/xmleditor/ace/range',
        'exceptionParser': 'ws/modelDesigner/exceptionParser',
        ace: 'thirdparty/ace/ace',
        analytics: 'ws/templates/analytics',
        behave: 'thirdparty/behave',
        bootstrap: 'thirdparty/bootstrap-cnr',
        'bootstrap-fileupload': 'thirdparty/bootstrap-fileupload-cnr',
        bootstrapTemplate: 'ws/templates/bootstrap',
        cache: '../../rest/cache',
        ckeditor: 'thirdparty/ckeditor/ckeditor',
        'ckeditor-jquery': 'thirdparty/ckeditor/adapters/jquery',
        common: '../../rest/common?pageId=${page.id}',
        datepicker: 'thirdparty/datepicker/bootstrap-datepicker-cnr',
        'datepicker-i18n': 'thirdparty/datepicker/locales/bootstrap-datepicker.${locale_suffix}',
        datetimepicker: 'thirdparty/datetimepicker/bootstrap-datetimepicker',
        'datetimepicker-i18n': 'thirdparty/datetimepicker/locales/bootstrap-datetimepicker.${locale_suffix}',
        fileupload: 'thirdparty/jquery.fileupload',
        handlebars: 'thirdparty/handlebars',
        header: 'ws/header',
        'i18n-data': '../../rest/i18n?method=${Request.requestContext.requestMethod}&uri=${page.id}&lang=${locale_suffix}',
        jquery: 'thirdparty/jquery',
        'jquery.ui.widget': 'thirdparty/jquery.ui.widget',
        json: 'thirdparty/require/json-cnr',
        jstree: 'thirdparty/jquery.jstree-cnr',
        moment: 'thirdparty/moment/moment',
        modernizr: 'thirdparty/modernizr-custom',
        'moment-i18n': 'thirdparty/moment/lang/${locale_suffix}',
        noty: 'thirdparty/noty/jquery.noty',
        'noty-layout': 'thirdparty/noty/layouts/topRight',
        'noty-theme': 'thirdparty/noty/themes/default',
        searchjs: 'thirdparty/jquery.search',
        'select2': 'thirdparty/select2-cnr',
        'select2-i18n': 'thirdparty/select2/locales/select2_locale_${locale_suffix}',
        text: 'thirdparty/require/text',
        validate: 'thirdparty/jquery.validate-cnr',
        list: 'thirdparty/list'
      },
      shim: {
        searchjs: {
          exports: 'searchjs',
          deps: ['jquery', 'thirdparty/search', 'thirdparty/stemmer']
        },
        'bootstrap-fileupload': {
          exports: 'bootstrap-fileupload',
          deps: ['bootstrap']
        },
        'select2': {
          exports: 'select2',
          deps: ['bootstrap']
        },
        bootstrap: {
          exports: 'bootstrap',
          deps: ['jquery']
        },
        fileupload: {
          exports: 'fileupload',
          deps: ['jquery', 'jquery.ui.widget', 'thirdparty/jquery.iframe-transport']
        },
        list: {
          exports: 'list',
          deps: ['jquery']
        },
        'validate': {
          exports: 'validate',
          deps: ['jquery']
        },
        'datepicker': {
          exports: 'datepicker',
          deps: ['jquery']
        },
        'datepicker-i18n': {
          exports: 'datepicker-i18n',
          deps: ['jquery', 'datepicker']
        },
        'datetimepicker': {
          exports: 'datetimepicker',
          deps: ['jquery']
        },
        'datetimepicker-i18n': {
          exports: 'datetimepicker-i18n',
          deps: ['jquery', 'datetimepicker']
        },
        'moment-i18n': {
          exports: 'moment-i18n',
          deps: ['moment']
        },
        'select2-i18n': {
          exports: 'select2-i18n',
          deps: ['select2']
        },
        'ace': {
          exports: 'ace'
        },
        'handlebars': {
          exports: 'Handlebars'
        },
        'modernizr': {
          exports: 'Modernizr'
        },
        'noty': {
          exports: 'noty',
          deps: ['jquery']
        },
        'noty-layout': {
          exports: 'noty-layout',
          deps: ['noty']
        },
        'noty-theme': {
          exports: 'noty-theme',
          deps: ['noty']
        },
        'ckeditor-jquery': {
          exports: 'ckeditor-jquery',
          deps: ['ckeditor']
        },
        'jquery-ui' : {
          deps: ['jquery']
        },
        'jquery.xmleditor': {
          deps: ['jquery', 'jquery-ui']
        }
      }
    };

    var params = {
      <#list RequestParameters?keys as key>
        "${key}": "${RequestParameters[key]}"
        <#if key_has_next>,</#if>
      </#list>
      };
    </script>
    ${head}
  </head>
  <body>
    <div id="wrap">
			<@region id="header" scope="template" />

	    <div id="content" class="container">
	      <@region id="main" scope="page" />
	    </div> <!-- /.container -->

	    <div id="push"></div>
    </div><!-- /#wrap -->

    <@region id="footer" scope="template" />

  </body>
</html>