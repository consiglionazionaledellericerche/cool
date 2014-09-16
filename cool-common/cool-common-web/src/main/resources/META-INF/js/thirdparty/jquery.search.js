/*jslint browser:true*/
/*global Search,jQuery*/
/*
 *  Project: search.js
 *  Description:
 *  Author: Francesco Uliana
 *  License:
 *
 *  depends on Search.js
 */
(function ($, window) {
  "use strict";

  var pluginName = 'searchjs',
    defaults = {
      engine: {
        logger: true,
        stopWords: ["and", "or", "if", "else"],
        minLength: 2, //minimum keyword length
        stem: true
      },
      display: function (data) {
        if (window.console) {
          window.console.log(data);
        }
      }
    };

  function Plugin(element, options) {
    this.element = element;

    this.options = $.extend(true, {}, defaults, options);

    this._defaults = defaults;
    this._name = pluginName;

    this.init();
  }

  Plugin.prototype.init = function () {

    var options = this.options,
      element = $(this.element),
      timeoutIds = [],
      s = new Search(options.engine, options.content, function () {
        element
          .attr("disabled", false)
          .show()
          .off('keyup')
          .on('keyup', function (event) {
            if (!event.altKey && !event.ctrlKey && ((event.keyCode >= 48 && event.keyCode <= 57) || (event.keyCode >= 65 && event.keyCode <= 90) || (event.keyCode >= 96 && event.keyCode <= 105) || event.keyCode === 8)) {
              var query = $(this).val(),
                timeoutId = timeoutIds.pop();
              while (timeoutId) {
                window.clearTimeout(timeoutId);
                timeoutId = timeoutIds.pop();
              }
              if (query.length < options.engine.minLength) {
                timeoutIds.push(window.setTimeout(function () {
                  options.display([]);
                }, 600));
              } else {
                s.search(query, function (results) {
                  options.display(results);
                });
              }
            }
          });
      });
  };

  $.fn[pluginName] = function (options) {
    return this.each(function () {
      $.data(this, 'plugin_' + pluginName, new Plugin(this, options));
    });
  };

}(jQuery, window));