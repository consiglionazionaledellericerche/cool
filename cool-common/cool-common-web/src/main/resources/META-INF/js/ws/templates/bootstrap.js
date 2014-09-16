define(['jquery', 'i18n', 'bootstrap', 'analytics'], function ($, i18n) {
  "use strict";

  /**
   * Adds a dropdown menu ("others") if there are too many pages to display.
   *
   * @param {number} tuningWidth: used to leave some blank space
   *
   */
  function resizeNavbar(tuningWidth) {
    var navbar = $('.navbar'),
      pageList = navbar.find('.nav'),
      availableWidth = navbar.width() - $('.logo').outerWidth() - ($('#search').outerWidth()) - $('#userInfo').outerWidth() - tuningWidth,
      pageListWidth = 0,
      dropdownItems = [],
      ul;

    // if navbar is not collapsed (e.g. mobile view)
    if (!navbar.find('a[data-toggle=collapse]:visible').length) {
      $.each(pageList.find('.page'), function (index, el) {
        var $el = $(el);
        if ($el.is(":visible")) {
          pageListWidth += $el.outerWidth();
          if (pageListWidth > availableWidth) {
            dropdownItems.push($el.clone());
            $el.remove();
          }
        }
      });

      if (pageListWidth > availableWidth) {

        ul = $('<ul class="dropdown-menu"></ul>');

        $.each(dropdownItems, function (index, el) {
          if (el.hasClass('dropdown')) {
            el.toggleClass('dropdown-submenu dropdown');
            el.find('.caret').remove();
          }
          ul.append(el);
        });

        $('<li class="dropdown page"></li>')
          .append('<a href="#" class="dropdown-toggle" data-toggle="dropdown">' + i18n.prop('navbar.more') + ' <b class="caret"></b></a>')
          .append(ul)
          .insertBefore(pageList.find('.divider-vertical'));
      }
    }
    pageList.removeClass('hidden-important');
  }
  return {
    resizeNavbar: resizeNavbar
  };
});