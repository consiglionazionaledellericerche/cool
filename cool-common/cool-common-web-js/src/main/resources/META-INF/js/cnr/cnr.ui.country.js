define(['jquery', 'cnr/cnr.url'], function ($, URL) {
  "use strict";

  var maxItems = 12;

  function widget(id, labelText, bulkitem) {

    var input = $('<input type="text" class="input-medium" autocomplete="off" data-provide="typeahead" />').addClass(bulkitem['class']).attr('id', id).attr('name', id),
      controls = $('<div class="controls"></div>').append(input).append(' '),
      label = $('<label class="control-label"></label>').attr('for', id).text(labelText || ''),
      item = $('<div class="control-group country widget"></div>');

    item
      .data('id', id)
      .append(label)
      .append(controls);
    URL.Data.countries().done(function (data) {
      var countries = $.map(data, function (el) {return el.toLowerCase(); });
      // input change and auto-completion
      input
        .on('keyup change', function () {
          var query = input.val().toLowerCase(),
            countryIndex = countries.indexOf(query);
          item.data('value', countryIndex >= 0 ? data[countryIndex] : null);
        })
        .typeahead({
          source: data,
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