define(['jquery', 'i18n'], function ($, i18n) {
  "use strict";

  function widget(id, labelText, item, label) {

    var controls = $('<div class="controls"></div>'),
//      label = $('<label/>').attr("class", item.labelClass || "control-label").append(labelText),
//      parent = $('<div class="control-group widget"></div>').append(label).append(controls),
      parent = $('<div class="control-group widget"></div>').addClass(item.parentGroupClass)
        .append(labelText ? label : null)
        .append(controls),
      input = $('<div class="btn-group radio-tick" data-toggle="buttons-radio"></div>').attr('id', id).appendTo(controls);

    if (item.jsonlist) {
      $.each(item.jsonlist, function (index, fieldvalue) {
        var btn = $('<button type="button" data-value="' + fieldvalue.key +
          '" data-id="' + fieldvalue.id + '" class="btn" >' +
          i18n.prop(fieldvalue.label, fieldvalue.defaultLabel) +
          '</button>').appendTo(input);
        if (item['class']) {
          btn.addClass(item['class']);
        }
        if (String(item.val) === String(fieldvalue.key) || (!item.val && item['default'] && item['default'] === fieldvalue.key)) {
          btn.addClass('active default');
          parent.data('value', fieldvalue.key);
        }
      });
    }

    input.on('click', 'button', function (event) {
      var item = $(event.target),
        value = item.data('value'),
        parsedValue = value ? String(value).match(/[a-zA-Z0-9-_]+/, '') : value;
      parent.data('value', value);
      if (parsedValue !== undefined) {
        $('[class*="' + id + '"]').parents('.control-group').hide();
        $('[class*="' + id + '"]:not(".' + id + '_' + parsedValue + '")').val('').trigger('change');
        $('.' + id + '_' + parsedValue).parents('.control-group').show();
      }
    });

    return parent;
  }

  return {
    Widget: widget
  };

});