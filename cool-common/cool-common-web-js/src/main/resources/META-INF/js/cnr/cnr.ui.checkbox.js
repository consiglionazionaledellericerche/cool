define(['jquery'], function ($) {
  "use strict";

  function widget(id, labelText, item) {

    var controls = $('<div class="controls"></div>'),
      label = $('<label/>').attr("class", item.labelClass || "control-label").append(labelText),
      parent = $('<div class="control-group widget"></div>').append(label).append(controls),
      isChecked = item.val || item['default'] || false,
      options = {
        y: {
          label: '<i class="icon-ok icon-white"></i>'
        },
        n: {
          label: 'NO'
        }
      },
      myWidget = $('<div class="btn-group cnr-checkbox" data-toggle="buttons-radio"></div>').attr("id", id).attr("name", item.property).appendTo(controls);

    // checkbox state change
    function changeStatus(on) {

      var customClass = 'btn-success',
        selectedItem = options[on ? 'y' : 'n'].item,
        wid = $('.' + item.name + '_true').parents('.control-group');

      myWidget
        .removeClass("on off")
        .addClass(on ? "on" : "off")
        .find('.' + customClass)
        .removeClass(customClass);

      parent.data('value', on || null);

      if (on) {
        wid.show();
        selectedItem.addClass(customClass);
      } else {
        wid.hide();
      }
    }

    // create the two buttons (on / off)
    $.each(options, function (key, value) {

      var btn = $('<button type="button" data-value="' + key + '" class="btn">' + value.label + '</button>')
        .addClass('cnr-checkbox-' + key)
        .click(function () {
          changeStatus(key === 'y');
        })
        .appendTo(myWidget);

      if ((isChecked && key === "y") || (!isChecked && key === "n")) {
        btn.addClass('active');
      }

      value.item = btn;
    });

    changeStatus(isChecked);

    return parent;
  }

  return {
    Widget: widget
  };
});