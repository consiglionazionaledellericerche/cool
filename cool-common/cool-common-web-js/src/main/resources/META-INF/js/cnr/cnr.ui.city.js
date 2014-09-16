define(['jquery', 'cnr/cnr.url'], function ($, URL) {
  "use strict";

  var maxItems = 12;

  function widget(id, labelText, bulkitem) {

    var input = $('<input type="text" class="input-medium" autocomplete="off" data-provide="typeahead" />').addClass(bulkitem['class']).attr('id', id).attr('name', id),
      prov = $('<input type="text" disabled/>').attr('data-extraProperty', bulkitem.extraProperty).addClass('input-small'),
      controls = $('<div class="controls"></div>').append(input).append(' ').append(prov),
      label = $('<label class="control-label"></label>').attr('for', id).text(labelText || ''),
      item = $('<div class="control-group city widget"></div>');

    item
      .data('id', id)
      .append(label)
      .append(controls);

    URL.Data.cities().done(function (data) {

      var cities = Object.keys(data), lowerCaseMap = {};
      $.each(data, function (key, val) {
        lowerCaseMap[key.toLowerCase()] = val;
      });
      // input change and auto-completion
      input
        .on('keyup change', function () {
          var query = input.val().toLowerCase(),
            isValid = lowerCaseMap[query],
            cityValue,
            provValue = isValid ? lowerCaseMap[query] : null;

          cityValue = $(cities).filter(function (index, el) {
            return el.toLowerCase() === query;
          })[0];

          item.data('value', isValid ? cityValue : null);
          prov
            .val(provValue)
            .data('value', provValue);
        })
        .typeahead({
          source: cities,
          items: maxItems
        });
      if (bulkitem.val) {
        input.val(bulkitem.val).trigger('change');
      }
    });

    return item;
  }

  return {
    Widget: widget
  };
});