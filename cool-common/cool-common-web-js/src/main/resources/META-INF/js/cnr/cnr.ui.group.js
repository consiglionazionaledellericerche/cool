define(['json!common', 'i18n', 'cnr/cnr.url'], function (common, i18n, URL) {
  /* javascript closure providing all the group functionalities */
  "use strict";

  /* public methods */
  function widget(id, labelText, onSelect) {
    var btn = $('<div class="btn-group"></div>'),
      a = $('<button class="btn btn-default dropdown-toggle" data-toggle="dropdown"></button>').appendTo(btn),
      ul = $('<ul class="dropdown-menu"></ul>').appendTo(btn),
      label = $('<label class="control-label">' + (labelText || '') + '</label>').attr('for', id),
      empty = {displayName: i18n.prop('label.empty.group.name', 'nessuno')},
      controls = $('<div class="controls"></div>'),
      item = $('<div class="form-group widget"></div>');

    function select(btn, el, init) {
      btn
        .data('name', el.displayName)
        .data('id', el.fullName)
        .children('button')
        .text(i18n.prop(el.fullName, el.displayName))
        .append('<span class="caret"></span>');
      item.data('value', el.fullName || null);
      if (typeof onSelect === 'function') {
        onSelect(el);
      }

      if (!init) {
        // simulate an input change to trigger the validator
        btn
          .trigger('focusin')
          .trigger('keyup');
      }
    }

    function children(data, li, el) {
      if (el !== true) {
        li.attr("class", "dropdown-submenu");
        var ul = $('<ul class="dropdown-menu"></ul>').appendTo(li);
        $.each(el, function (index_child, el_child) {
          li.attr("class", "dropdown-submenu");
          var liChild = $('<li></li>'), a = $('<a href="#">' + i18n.prop(data.detail[index_child].fullName, data.detail[index_child].displayName) + '</a>').click(function (eventObject) {
            select(btn, data.detail[index_child]);
            eventObject.preventDefault();
          });
          liChild.append(a).appendTo(ul);
          children(data, liChild, el_child);
        });
      }
    }

    // load groups of users and then populate dropdown button
    URL.Data.proxy.myGroupsDescendant({
      placeholder: {
        user_id: common.User.id
      }
    }).done(function (data) {
      $.each(data.tree, function (index_tree, el_tree) {
        var li = $('<li></li>'), a = $('<a href="#">' + i18n.prop(data.detail[index_tree].fullName, data.detail[index_tree].displayName) + '</a>').click(function (eventObject) {
          select(btn, data.detail[index_tree]);
          eventObject.preventDefault();
        });
        li.append(a).appendTo(ul);
        children(data, li, el_tree);
      });

      // select by default the first "empty" choice
      select(btn, empty, true);

      btn
        .bind('reset', function () {
          select(btn, empty);
        });
    });
    controls
      .append(btn);
    item
      .append(label)
      .append(controls);

    return item;
  }

  return {
    Widget: widget
  };
});