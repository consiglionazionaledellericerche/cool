define(['jquery', 'i18n', 'select2', 'select2-i18n', 'cnr/cnr'], function ($, i18n, CNR) {
  "use strict";

  function init(item, id, parent, controls) {

    var select = $('<select></select>').attr("id", id).attr("name", item.property),
      options,
      optionsGroup = [],
      s2,
      select2opts = {
        allowClear: true,
        placeholder: item.placeholder || ""
      },
      keys,
      optgroup = [];

    if (item.jsonlist) {

      keys = $.map(item.jsonlist, function (el) {
        return el.key || el;
      });
      if (item.val && item.multiple) {
        item.jsonlist.sort(function (a, b) {
          function orderValue(a) {
            var p = item.val.indexOf(a.key);
            return p >= 0 ? p : (keys.length + keys.indexOf(a));
          }
          return orderValue(a) - orderValue(b);
        });
      }
      options = $.map([].concat(item.jsonlist), function (el) {
        var label = el.label || el,
          key = el.key || el,
          opt = $('<option data-title="' + (i18n.prop(key + '.title', i18n.prop(label, el.defaultLabel)))  + '" value="' + key + '">' + i18n.prop(label, el.defaultLabel) + '</option>');
        if (item.val && key !== "" && [].concat(item.val).indexOf(key) >= 0) {
          opt.attr('selected', 'true');
        }
        if (item.val && key !== "" && [].concat(item.val).indexOf(key) >= 0) {
          opt.attr('selected', 'true');
        }
        if (el.group) {
          if ($.inArray(el.group, optgroup) === -1) {
            optgroup.push(el.group);
          }
          opt.attr('data-optgroup', el.group);
        }
        return opt;
      });
      if (optgroup.length > 0) {
        $.each(optgroup, function (index) {
          optionsGroup.push($('<optgroup>').attr('label', optgroup[index]).append(options.filter(function (opt) {
            return opt[0].attributes['data-optgroup'] && opt[0].attributes['data-optgroup'].value === optgroup[index];
          })));
          optionsGroup.push(options.filter(function (opt) {
            return opt[0].attributes['data-optgroup'] === undefined;
          }));
        });
        options = [$('<option></option>')].concat(optionsGroup);
      } else {
        options = [$('<option></option>')].concat(options);
      }
    }


    if (item.multiple) {
      select.attr('multiple', 'multiple');
    }
    function setData() {
      var value = $.map([].concat(s2.data('select2').data()), function (el) {
        return (el && el.text) ? el.id : undefined;
      }), textValue = $.map([].concat(s2.data('select2').data()), function (el) {
        return (el && el.text) ? el.text : undefined;
      }),
        parsedValue = value ? String(value).match(/[a-zA-Z0-9-_]+/, '') : value;
      parent.data('value', value.length ? (item.multiple ? value : value[0]) : null);
      if (item.ghostName) {
        $('#' + item.ghostName).parents('.control-group').data('value', textValue.length ? textValue : null);
      }
      if (parsedValue) {
        $('[class*="' + id + '"]').parents('.control-group').hide();
        $('[class*="' + id + '"]:not(".' + id + '_' + parsedValue.join(',') + '")').val('').trigger('change');
        $('.' + id + '_' + parsedValue.join(',')).parents('.control-group').show();
      }
    }

    if (item['class']) {
      select.addClass(item['class']);
    }

    if (item.width) {
      select2opts.width = item.width;
    }
    if (item.maximumSelectionSize) {
      select2opts.maximumSelectionSize = item.maximumSelectionSize;
    }
    if (item.visible === "false" || item.visible === false) {
      parent.css("display", "none");
    }

    select2opts.dropdownAutoWidth = true;

    s2 = select
      .append(options)
      .appendTo(controls)
      .select2(select2opts)
      .on('change', setData);
    setData();
    return s2;
  }

  function internalWidget(id, labelText, item, async) {

    var controls = $('<div class="controls"></div>'),
      parent = $('<div class="control-group widget"></div>').append('<label class="control-label">' + labelText + '</label>').append(controls);

    if (item.parentGroupClass) {
      parent.addClass(item.parentGroupClass);
    }

    if (!async) {
      init(item, id, parent, controls);
    }

    return {
      parent: parent,
      controls: controls
    };
  }
  return {
    Widget: function (id, labelText, item) {
      return internalWidget(id, labelText, item).parent;
    },
    CustomWidget: function (id, labelText, item) {
      var obj = internalWidget(id, labelText, item, true),
        parent = obj.parent;
      return {
        emptyWidget: parent,
        setOptions: function (options) {
          item.jsonlist = options;
          return init(item, id, parent, obj.controls);
        }
      };
    }
  };
});