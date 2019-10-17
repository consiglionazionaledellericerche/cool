define(['jquery', 'cnr/cnr.ui', 'json!common', 'i18n', 'json!cache'], function ($, UI, common, i18n, cache) {
  "use strict";

  var xhrs = {},
    dataSource,
    urls = {
      proxy: {
        groups: "rest/proxy" + "?url=service/cnr/groups/autocomplete-group",
        myGroupsDescendant: "rest/proxy" + "?url=service/cnr/groups/my-groups-descendant/$user_id",
        person: "rest/proxy" + "?url=service/cnr/person/autocomplete-person",
        permissions: 'rest/proxy' + '?url=service/cnr/nodes/permissions/$node',
        people: 'rest/proxy' + '?url=service/cnr/person/person/$user_id',
        peopleSearch: 'rest/proxy' + '?url=service/cnr/person/people',
        disableAccount: 'rest/proxy' + '?url=service/cnr/person/disable-account',
        changePassword: 'rest/proxy' + '?url=service/api/person/changepassword/$userid',
        members: 'rest/proxy' + '?url=service/cnr/groups/$group_name/members',
        jsRemote: 'rest/proxy' + '?url=service/cnr/utils/javascript-execution',
        childrenGroup: 'rest/proxy' + '?url=service/cnr/groups/children',
        rootGroup: 'rest/proxy' + '?url=service/cnr/groups/root',
        group: 'rest/proxy' + '?url=service/cnr/groups/group',
        metadata: 'rest/proxy' + '?url=service/api/metadata',
        metadataNode: 'rest/proxy' + '?url=service/api/metadata/node/$store_type/$store_id/$id',
        version: 'rest/proxy' + '?url=service/cnr/nodes/version',
        dynamicProxy: 'rest/proxy'
      },
      security : {
        forgotPassword: 'rest/security/forgotPassword'
      },
      account: {
        changePassword: 'rest/security/change-password',
        createAccount: 'rest/security/create-account'
      },
      countries: 'rest/static/json/paesi.json',
      cities: 'rest/static/json/comuni.json',
      rbac: 'rest/rbac',
      bulkInfo: "rest/bulkInfo/view/$path/$kind/$name?v=" + common.artifact_version,
      bulkInfoForms: "rest/bulkInfo/structure/$path?v=" + common.artifact_version,
      frontOffice : {
        document: 'rest/frontOffice/$type_document',
        log: 'rest/frontOffice/log',
        doc: 'rest/frontOffice',
        nodeRef: 'rest/frontOffice/$store_type/$store_id/$id'
      },
      model: {
        models: 'rest/models',
        modelNodeRef: 'rest/models/$id/$version',
        activate: 'rest/models/activate/$id/$version',
        docsByPath: 'rest/models/docsByPath/$id/$version',
        docsByTypeName: 'rest/models/docsByTypeName',
        property: 'rest/models/property/$id/$version',
        generateTemplate: 'rest/models/generateTemplate'
      },
      typesTree: 'rest/typesTree/tree',
      exportVariazioni: 'rest/exportVariazioni/',
      search: {
        content: "rest/content",
        query: "rest/search?guest=true",
        version: "rest/search/document/version?guest=true",
        queryExcel: "rest/search/query.xls?guest=true",
        rootFolder : "rest/search/folder/root?guest=true",
        children: "rest/search/folder/children?guest=true",
        folderByPath: 'rest/search/folder/by-path'
      },
      node: {
        metadata: "rest/node/metadata",
        node: "rest/node",
        copy: "rest/copy",
        cut: "rest/cut"
      },
      folder: "rest/folder",
      root: "",
      login: "login",
      logout: "rest/security/logout",
      common: "rest/common",
      handlebars: 'res/js/handlebars/$id',
      sedi: 'rest/sedi'
    };

  function template(url, placeholder) {
    // allows you to replace placeholders in the URL.
    $.each(placeholder || {}, function (key, value) {
      var re = new RegExp("\\$" + key, 'ig');
      // qua
      if (url.match(re)) {
        url = url.replace(re, value);
      } else {
        url += (url.indexOf('?') >= 0 ? '&' : '?') + key + '=' + value;
      }
    });
    return url;
  }

  /* write a new persistent Logger - either creating a document on Alfresco OR writing a Log4j server log */
  function addLog(json) {
    return dataSource.frontOffice.log({
      type: "POST",
      data: {
        stackTrace: JSON.stringify($.extend(true, {}, {
          mappa: {
            user: common.User.id || 'guest',
            url: window.location.href,
            application: cache.baseUrl.replace(/^\/cool-/g, '').replace(/[^a-zA-Z\-]/g, '')
          }
        }, json))
      },
      error: function () {}
    });
  }

  function extractError(text) {
    var line = text.split('\n')[0],
      re = /.*ClientMessageException: (.*)/g,
      suffix = '';
    if (re.test(line)) {
      suffix = i18n.prop(line.replace(re, '$1'), line.replace(re, '$1'));
    }
    return suffix;
  }

  function errorFn(jqXHR, textStatus, errorThrown, context) {
    // default function for "error" event

    context = context || {};

    if (jqXHR.status === 403) {
      UI.error(i18n['message.access.denieded'], function () {
        window.location = urls.root;
      });
    } else if (jqXHR.status === 401) {
      UI.alert(i18n['message.session.expired'], null, function () {
        window.location = urls.logout;
      });
    } else if (jqXHR.status) {
      var jsonError, errorMessage, clientException;
      try {
        jsonError = JSON.parse(jqXHR.responseText);
      } catch (e) {
      }
      if (jqXHR.status === 303) {
        window.location = jsonError.location;
        return;
      }
      if (jsonError && jsonError.keyMessage) {
        errorMessage = JSON.parse(jqXHR.responseText).keyMessage;
        UI.error(i18n.prop(errorMessage, errorMessage), context.callbackErrorFn);
      } else if (jsonError && jsonError.message) {
        errorMessage = jsonError.message;
        UI.error(i18n.prop(errorMessage, errorMessage), context.callbackErrorFn);
      } else {
        clientException = extractError(jqXHR.responseText);
        if (clientException) {
          errorMessage = clientException;
        } else {
          errorMessage = i18n.prop('http.status.' + jqXHR.status, textStatus +  " " + errorThrown);
        }
        UI.error(errorMessage);
        addLog({
          typeDocument: 'log',
          codice: 1,
          testo: textStatus + ' - ' + errorThrown,
          mappa: {
            url: context.url,
            stackTraceAjax: jqXHR.responseText
          },
          ajax: {
            type: context.type,
            data: context.data,
            contentType: context.contentType,
            processData: context.processData
          }
        });
      }
    }
  }

  function cnrAjax(settings, url) {

    // queue can be undefined (replayed ajax will be allowed), a string (will prevent replayed ajax with the same "queue" id) or a boolean (will prevent ajax with the same URL)
    var idQueue = settings.queue ? (typeof settings.queue === 'string' ? settings.queue : url) : null,
      defaults,
      xhr,
      customFunctions = {};

    // XHR queue initialization
    if (idQueue && !xhrs[idQueue]) {
      xhrs[idQueue] = [];
    }

    url = template(url, $.extend({}, settings.placeholder, {ajax: true}));

    defaults = {
      url: url,
      beforeSend: function (jqXHR) {
        jqXHR.startTime = new Date().getTime();
        // abort all active XHRs for "idQueue"
        while (idQueue && xhrs[idQueue].length) {
          xhrs[idQueue].pop().abort();
        }
        // invokes the "custom" beforeSend function (if defined), acts as an interceptor
        if (customFunctions.beforeSend) {
          customFunctions.beforeSend.call(this);
        }
      },
      success: function (data, textStatus, jqXHR) {
        // default function for "success" event
      },
      callbackErrorFn: function (data, textStatus, jqXHR) {
        // default function for "callbackErrorFn" event
      },
      error: settings.errorFn || function (jqXHR, textStatus, errorThrown) {
        errorFn(jqXHR, textStatus, errorThrown, this);
      },
      complete: function (jqXHR) {
        // invokes the "custom" complete function (if defined), acts as an interceptor
        if (customFunctions.complete) {
          customFunctions.complete.call(this);
        }

        // remove current XHR from the XHRs queue
        if (idQueue) {
          xhrs[idQueue] =  $.map(xhrs[idQueue], function (value) {
            if (value !== jqXHR) {
              return value;
            }
          });
        }

        var elapsedTime = new Date().getTime() - jqXHR.startTime,
          context = this;

      }
    };

    // special functions
    $.each(['beforeSend', 'complete'], function (index, key) {
      if (typeof settings[key] === 'function') {
        var f = settings[key];
        customFunctions[key] = f;
        delete settings[key];
      }
    });

    xhr = $.ajax($.extend({}, defaults, settings));

    // add the current XHR to the XHRs queue
    if (idQueue) {
      xhrs[idQueue].push(xhr);
    }
    return xhr;
  }

  function initURL(urls) {
    var prefix = cache.baseUrl;
    // common
    function addPrefix(prefix, map) {
      $.each(map, function (k, value) {
        if (typeof value === "string") {
          map[k] = prefix + '/' + value;
        } else if (typeof value === "object") {
          addPrefix(prefix, value);
        }
      });
    }

    addPrefix(prefix, urls);

    function dataSourcify(item) {

      var r = {};

      $.each(item, function (key, el) {
        if (typeof el === 'string') {
          r[key] = function (opts) {
            return cnrAjax(opts || {}, el);
          };
        } else if (typeof el === 'object') {
          r[key] = dataSourcify(el);
        }
      });

      return r;
    }

    return dataSourcify(urls);
  }

  /* returns an hash containing all the GET Query String Parameters */
  function fromQuerystring() {

    var args = {},
      index,
      item,
      key,
      value,
      argList = window.location.search.replace(/^\?/g, "").split("&");

    for (index in  argList) {
      if (argList.hasOwnProperty(index) && argList[index].length) {
        item = argList[index].split("=");
        key = item[0];
        value = item[1];
        if (args.hasOwnProperty(key)) {
          args[key] = [].concat(value).concat(args[key]);
        } else {
          args[key] = value;
        }
      }
    }

    return args;
  }

  // convert a javascript object into a querystring
  function toQuerystring(obj) {
    return $.map(obj, function (val, key) {
      return $.map([].concat(val), function (val2, key2) {
        return key + "=" + val2;
      });
    }).join('&');
  }

  dataSource = initURL(urls);


  window.onerror = function (errorMsg, url, lineNumber) {
    addLog({
      typeDocument: 'log',
      codice: 3,
      testo: 'javascript error: ' + errorMsg + ' @ ' + url + '#' + lineNumber
    });
    return false;
  };


  return {
    errorFn: errorFn,
    Data: dataSource,
    querystring: {
      from: fromQuerystring(),
      to: toQuerystring
    },
    template: template,
    urls: urls,
    initURL: initURL,
    addLog: addLog
  };
});