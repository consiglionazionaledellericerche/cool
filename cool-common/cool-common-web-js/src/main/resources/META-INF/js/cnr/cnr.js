/* CNR main object, implements Revealing Module Pattern */

define(['cnr/cnr.url', 'moment', 'json!cache', 'moment-i18n'], function (URL, moment, cache) {
  "use strict";
  /* utility functions */

  var DateUtils;

  function FormData() {

    var fallback = window.FormData ? false : true,
      data = fallback ? new window.FormDataCompatibility() : new window.FormData();

    return {
      getData: function () {
        return fallback ? data.buildBody() : data;
      },
      contentType: fallback ? data.contentType : false,
      data: data
    };
  }

  DateUtils = {
    format: function (date, defaultValue, dateformat) {
      var s;

      function format(d) {
        var m =  moment(d);
        if (dateformat && typeof dateformat === 'string') {
          return m.format(dateformat);
        }
        //return m.format("ddd DD MMM YYYY H:mm");
        return m.fromNow();
      }

      if (date instanceof Date) {
        s = format(date);
      } else if (typeof date === "string" && !isNaN(new Date(date).getTime())) {
        s = format(new Date(date));
      } else {
        s = defaultValue || "";
      }
      return s;
    },
    isPast: function (d) {
      return new Date().getTime() - d.getTime() > 0;
    },
    parse: function (d) {
      return d ? moment(d)._d : null;
    },
    yesterday: function () {
      return moment().subtract('days', 1)._d;
    }
  };


  // manage assertions depending on application mode (production/development)
  function assertFn(b, msg, obj) {
    if (window.console && window.console.assert) {
      if (cache.debug) {
        window.console.assert(b, msg, obj);
      } else {
        if (!b) {
          // write server log
          URL.addLog({
            typeDocument: 'log',
            codice: 4,
            testo: "Assertion failure: " + msg
          });
        }
      }
    }
  }

  /* "public" APIs */
  return {
    log: function (message) {
      if (window.console && cache.debug) {
        if (arguments.length === 1) {
          window.console.log(message);
        } else {
          window.console.log(arguments);
        }
      }
    },
    assert: {
      // assertion utilities
      equals: function (a, b) {
        assertFn(a === b, a + " diverso da " + b, [a, b]);
      },
      gt: function (a, b) {
        assertFn(a > b, a + " non maggiore di " + b);
      },
      value: function (el) {
        var b = el !== null && el !== undefined;
        if (typeof el === 'string') {
          b = b && el.trim().length > 0;
        } else if (Array.isArray(el)) {
          b = b && b.length > 0;
        } else if (el.jquery) {
          b = b && el.length > 0;
        }
        assertFn(b, '"' + el + '" is empty');
      }
    },
    Date: DateUtils,
    Storage: {
      get: function (key, defaultValue) {
        if (window.localStorage) {
          var stringified = window.localStorage.getItem(key),
            item = JSON.parse(stringified);
          return item ? item.value : defaultValue;
        }
      },
      set: function (key, value) {
        var stringified = JSON.stringify({value: value});
        if (window.localStorage) {
          window.localStorage.setItem(key, stringified);
        }
      },
      remove: function (key) {
        if (window.localStorage) {
          window.localStorage.removeItem(key);
        }
      }
    },
    mock: function (dataSource, mockSettings) {
      var promise;
      return function (opts) {
        if (mockSettings && (mockSettings.matching === undefined || mockSettings.matching(opts))) {
          var dfd = $.Deferred(), result = mockSettings.fn(opts);
          dfd.resolve(result);
          dfd.error = function () {};
          promise = dfd.promise();
        } else {
          promise = dataSource(opts);
        }
        return promise;
      };
    },
    nodeRefSelector: function (nodeRef) {
      return $('#' + nodeRef.replace(/^#/, "").replace(/\\\//g, "/").replace(/\//g, "\\\/").replace(/\\\./g, ".").replace(/\./g, "\\.").replace(/\:/g, "\\:"));
    },
    mimeTypeIcon: function (mimeType, name) {
      var icon, path, myClass, mapping = {
        pdf: 'application-pdf'
      }, extension = (name || '').toLowerCase().split('.').pop();

      if (mimeType === "folder") {
        myClass = "icon-folder-close icon-blue";
      } else if (name && mapping[extension]) {
        myClass = "mimetype mimetype-" + mapping[extension];
      } else if (mimeType) {
        path = mimeType.replace(/\/x-/g, '/').replace(/\//g, '-');
        myClass = "mimetype mimetype-" + path;
      } else {
        myClass = "mimetype";
      }
      return $('<i></i>').addClass(myClass);
    },
    getHandler: function (element, event) {
      event = event || "click";
      return $._data(element, "events")[event][0].handler;
    },
    html: function (el) {
      return $('<div />').append(el.clone()).html();
    },
    fileSize: function (bytes) {
      var suffix = ["bytes", "kb", "mb", "gb", "tb", "pb"], tier = 0;
      while (bytes >= 1024) {
        bytes = bytes / 1024;
        tier++;
      }
      return Math.round(bytes * 10) / 10 + " " + suffix[tier];
    },
    FormData: FormData
  };
});