define(['i18n'], function (i18n) {
  "use strict";

  function widget(id, labelText, item, label) {

    var controls = $('<div class="controls"></div>'),
      parent = $('<div class="form-group widget"></div>').addClass(item.parentGroupClass)
        .append(labelText ? label : null)
        .append(controls),
      input = $('<div class="btn-group" data-toggle="buttons"></div>').attr('id', id).appendTo(controls);

    if (item.jsonlist) {
      $.each(item.jsonlist, function (index, fieldvalue) {
        var btn = $('<button type="button" data-value="' + fieldvalue.key +
          '" data-id="' + fieldvalue.id + '" class="btn btn-default" >' +
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
        // $('[class*="' + id + '"]').parents('.form-group').hide();
        // $('[class*="' + id + '"]:not(".' + id + '_' + parsedValue + '")').val('').trigger('change');
        // $('.' + id + '_' + parsedValue).parents('.form-group').show();
      }
    });

    return parent;
  }

  return {
    Widget: widget
  };

});