define(['jquery', "cnr/cnr", 'jstree', 'cnr/cnr.url', 'json!common'], function ($, CNR, jstree, URL, common) {
  // Folders tree
  "use strict";

  var settings,
    tree,
    css = 'style.css',
    theme = "apple",
    defaults = {
      paramName: 'parentFolderId',
      childrenFn: URL.Data.search.children,
      selectNode: function (data) {
        CNR.log("selected: " + data.id);
      }
    };

  function nodeTree(node, callback) {
    //TODO: scartare gli attributi allowableactions + refactoring CNR.nodeRefSelector(item.nodeRef).attr('allowableactions').split(',') in ws\home\main.get.js
    var value = node.attr ? node.attr("id").replace("node_", "") : settings.folderId,
      data = {authorityType: 'GROUP'};
    data[settings.paramName] = value;
    data.user = common.User.id;

    settings.childrenFn({
      data: data
    }).done(callback);
  }

  function displayTree(dataSource) {

    settings.elements.target
      .addClass(settings.customClass)
      .jstree({
        themes : {
          theme : theme,
          url: URL.urls.root + "res/css/jstree/" + theme + '/' + css,
          dots : false
        },
        plugins : ["themes", "json_data", "ui"],
        json_data : {
          data: dataSource
        }
      })
      .bind("select_node.jstree", function (event, data) {
        var node = data.rslt.obj,
          tree = data.inst,
          id = node.attr("id"),
          allowableactions = node.attr("allowableactions") ? node.attr("allowableactions").split(',') : [],
          name = tree.get_json()[0].data,
          json = tree.get_json(-1),
          current_path = tree.get_path();

        settings.selectNode({
          id: id,
          allowableactions: allowableactions,
          name: name,
          json: json,
          current_path: current_path
        }, node);
      })
      .bind('refresh.jstree', function (event, data) {
        tree.open_node(data.rslt.obj);
      })
      .bind('hover_node.jstree', function (event, data) {
        var node = data.rslt.obj,
          displayName = node.attr('displayName'),
          description = node.attr('description');

        if (description && displayName.length > 40) {
          node.tooltip({
            placement: "right",
            title: description
          }).mouseover();
        }

      });
  }

  function openNode(nodeRef) {
    var el = CNR.nodeRefSelector(nodeRef);
    tree.open_node(el);
  }

  function refresh() {
    var currentNode = tree._get_node(null, false);
    tree.refresh(currentNode || null);
  }

  function init(options) {
    settings = $.extend({}, defaults, options);

    if (options.folderId !== undefined) {
      displayTree(nodeTree);
    } else {
      displayTree(options.dataSource);
    }
    tree = $.jstree._reference(settings.elements.target);
  }

  return {
    init: init,
    refresh: refresh,
    openNode: openNode
  };
});