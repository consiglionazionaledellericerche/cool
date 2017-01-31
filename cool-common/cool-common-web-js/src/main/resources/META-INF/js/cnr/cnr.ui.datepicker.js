define(['jquery', 'i18n', 'moment', 'datepicker-i18n'], function ($, i18n, moment) {
  "use strict";

  function widget(id, labelText, item, label) {

    var controls = $('<div class="controls"></div>'),
      parent = $('<div class="control-group widget"></div>')
        .append(labelText ? label : null)
        .append(controls),
      input = $('<input type="text" class="input-small datepicker" />').attr('id', id),
      dateFormat = item.format || "DD/MM/YYYY";

    if (item.placeholder) {
      input.attr("placeholder", i18n.prop(item.placeholder, item.placeholder));
    }

    if (item.val) {
      input.val(moment(item.val).format(dateFormat));
    }

    if (item['class']) {
      input.addClass(item['class']);
    }

    input
      .datepicker($.extend({}, {
        language: i18n.locale,
        autoclose: true,
        todayHighlight: true,
        startDate: '-100y',
        todayBtn: 'linked'
      }, item.jsonsettings))
      .on('changeDate change', function (eventType) {
        var d = moment(input.val() + ' 12:00 +0000', dateFormat + ' HH:mm ZZ'); // hack to prevent wrong birth date using different time zones
        if (d && d.isValid()) {
          parent.data('value', d.format("YYYY-MM-DDTHH:mm:ss.SSSZ"));
        } else {
          parent.data('value', null);
        }
      })
      .appendTo(controls)
      .trigger('changeDate');

    return parent;
  }

  return {
    Widget: widget
  };

});