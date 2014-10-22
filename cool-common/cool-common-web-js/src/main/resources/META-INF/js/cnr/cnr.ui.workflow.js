define(['jquery', 'i18n', 'cnr/cnr.url'], function ($, i18n, URL) {
  "use strict";

  // workflow definitions selector
  function widget() {

    var btn = $('<div class="btn-group"></div>').append('<a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#"></a>'),
      ul = $('<ul class="dropdown-menu"></ul>').appendTo(btn),
      empty = {title: 'nessun filtro', id: null};

    function select(btn, el) {
      btn
        .data({
          id: el.id,
          name: el.name
        })
        .children('a')
        .text(i18n.prop(el.title.replace(":", "_") + '.workflow.title', el.title))
        .append('<span class="caret"></span>');
    }

    // load workflow definitions and then populate dropdown button
    URL.Data.proxy.processDefinitions({}).done(function (data) {

      $.each([empty].concat(data.data), function (index, el) {
        var li = $('<li></li>'), a = $('<a href="#">' + i18n.prop(el.title.replace(":", "_") + '.workflow.title', el.title) + '</a>').click(function () {
          select(btn, el);
        });
        li.append(a).appendTo(ul);
      });

      // select by default the first "empty" choice
      select(btn, empty);

      btn
        .bind('reset', function () {
          select(btn, empty);
        });
    });

    return btn;
  }

  return {
    Widget: widget
  };
});