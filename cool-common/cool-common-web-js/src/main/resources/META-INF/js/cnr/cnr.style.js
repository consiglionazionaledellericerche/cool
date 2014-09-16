define(['jquery', 'json!common'], function ($, common) {
  "use strict";

  return function (v) {

    var version = v || common.bootstrapVersion,
      classes,
      classes2,
      classes3,
      overrides;

    classes = {
    };

    classes2 = {
      "large-field": "input-xlarge",
      group: "control-group",
      hidden: "hide"
    };

    classes3 = {
      group: "form-group",
      "form-control": "form-control",
      hidden: "hidden"
    };

    overrides = {
      '2': classes2,
      '3': classes3
      // 4: classes4
    };

    return {
      classes: $.extend({}, classes, overrides[version]),
      version: version
    };
  };
});