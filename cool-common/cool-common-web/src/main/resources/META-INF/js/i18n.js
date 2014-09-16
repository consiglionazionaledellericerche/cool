define(['json!i18n-data'], function (data) {
  "use strict";

  data.prop = function (k, defaultValue) {
    if (data[k]) {
      var s = data[k], i, re;
      for (i = 1; i < arguments.length; i++) {
        re = new RegExp("\\{" + (i - 1) + "\\}", "g");
        s = s.replace(re, arguments[i]);
      }
      return s;
    } else {
      return defaultValue !== undefined ? defaultValue : k;
    }
  };

  return data;

});