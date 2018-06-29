define(['jquery', 'i18n', 'ckeditor-jquery'], function ($, i18n) {
  "use strict";

  var config = {
    toolbarGroups: [
      { name: 'clipboard', groups: ['clipboard'] },
      { name: 'basicstyles', groups: ['basicstyles'] },
      { name: 'paragraph', groups: ['list', 'align'] }
    ],
    removePlugins: 'elementspath'
  };

  function widget(id, labelText, item) {
    var controls = $('<div class="controls"></div>'),
      parent = $('<div class="control-group widget"></div>').append('<label class="control-label">' + labelText + '</label>').append(controls),
      ck = $('<textarea></textarea>').attr('id', id).appendTo(controls).ckeditor(config);

    if (item.parentGroupClass) {
      parent.addClass(item.parentGroupClass);
    }
    if (item.parentClass) {
      controls.addClass(item.parentClass);
    }
    ck.val(item.val);

    ck.editor.on('change', function () {
      var html = ck.val();
      parent.data('value', html || null);
    });

    ck.editor.on('setData', function (event) {
      var html = event.data.dataValue;
      parent.data('value', html || null);
    });

    return parent;
  }

  return {
    Widget: widget
  };

});