define(['jquery', 'json!common', 'i18n', 'bootstrapTemplate'], function ($, common, i18n, bootstrapTemplate) {
  "use strict";

  function addMenu(btn, what, urlPrefix) {
    if (!common.User.isGuest) {
      var list = btn.find("ul");
      if (what.length) {
        $.each(what, function (key, el) {
          $('<li><a href="' + urlPrefix + el.id + '">' + i18n.prop(el.id, el.title) + '</a></li>').appendTo(list);
        });
        btn.removeClass('hide');
      }
    }
  }

  function resizeNavbar(gap) {
    // il margine di tolleranza "defaultGap" consente, allo stato attuale, di mostrare correttamente tutti i componenti della navbar
    var defaultGap = 50;
    bootstrapTemplate.resizeNavbar(gap || defaultGap);
  }

  $(window).bind('resize', function () {
    resizeNavbar();
  });

  function searchAnimation() {
    var defaults = {
      'opacity': 0.3
    };
    $("#search")
      .find('.search-query')
      .css(defaults)
      .on('focus', function () {
        $(this).animate({
          opacity: 1
        });
      })
      .on('blur', function () {
        $(this).delay(1000).animate(defaults);
      });
  }

  function arrangeSubMenus(navbar) {

    var lis = navbar.find('li'),
      target = navbar.find('.nav .divider-vertical'),
      submenus;

    submenus = $.map(lis, function (el) {
      return $(el).data('submenu');
    });

    $.each($.unique(submenus), function (index, entry) {

      var ul, entries = $.grep(lis, function (el) {
        return $(el).data('submenu') === entry;
      });

      ul = $('<ul class="dropdown-menu"></ul>')
        .append('<li class="nav-header">' + entry + '</li>')
        .append(entries);

      $('<li class="dropdown page"></li>')
        .append('<a href="#" class="dropdown-toggle" data-toggle="dropdown">' + entry + ' <b class="caret"></b></a>')
        .append(ul)
        .insertBefore(target);
    });
  }

  searchAnimation();

  return {
    resizeNavbar: resizeNavbar,
    addMenu: addMenu,
    arrangeSubMenus: arrangeSubMenus
  };

});