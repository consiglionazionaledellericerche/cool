define(['jquery', 'cnr/cnr.tree', 'cnr/cnr.url'], function ($, Tree, URL) {
  "use strict";

  return {
    Widget: function (id, labelText, obj) {

      var defaults,
        opts,
        promise,
        element = $('<div></div>').attr('id', id),
        controls = $('<div class="controls"></div>').append(element).append(' '),
        label = $('<label class="control-label"></label>').attr('for', id).text(labelText || ''),
        item = $('<div class="control-group widget"></div>');

      item
        .append(label)
        .append(controls);

      if (obj && obj.settings && obj.settings.dataSource) {
        promise = {
          done: function (callback) {
            callback();
          }
        };
      } else {
        promise = URL.Data.search.rootFolder({});
      }

      promise.done(function (rootFolder) {

        defaults = {
          folderId: rootFolder ? rootFolder.id : undefined,
          dataSource: undefined, // see http://old.jstree.com/documentation/json_data
          selectNode: function (data) {
            item.data('value', data.id);
          },
          elements: {
            target: element
          }
        };

        opts = $.extend(true, {}, defaults, obj.settings);
        Tree.init(opts);
      });

      return item;
    }
  };
});