define(['cnr/cnr.ui.widgets', 'jquery', 'cnr/cnr', 'cnr/cnr.style', 'handlebars', 'i18n', 'cnr/cnr.ui', 'cnr/cnr.ui.priority', 'cnr/cnr.ui.duedate', 'cnr/cnr.validator', 'cnr/cnr.url', 'json!common', 'json!cache', 'datepicker-i18n', 'datetimepicker-i18n'], function (Widgets, $, CNR, Style, Handlebars, i18n, UI, Priority, DueDate, Validator, URL, common, cache) {
  "use strict";

  /**
   * Esempio d'uso:
   * var target = $('#myDiv');
   *
   * var bulkinfo = new BulkInfo({
   *  target: target,
   *  path: 'nomeTipo',
   *  kind: 'form',
   *  name: 'default',
   * });
   *
   * bulkinfo.render();
   *
   *
   * Lista complete delle opzioni del BulkInfo:
   * version: versione di bootstrap con cui essere compatibili (attualmente 2 (default o 3)
   * path/kind/name: la vista da recuperare dal server (default: cmis:document/form/default)
   * objectId: eventuale oggetto CMIS con cui valorizzare la vista
   * metadata: (?)
   * handlebarsId: (?)
   * handlebarsSettings: (?)
   * target: JqueryObject in cui andrà renderizzata la vista
   * formclass: Stile da dare alla vista (default: 'form-horizontal' vedere documentazione bootstrap)
   * classes: list of custom classes (?)
   * callbacks: (?)
   *
   */

  return function (options) {
    var classes = new Style(options.version).classes,
      hm = {},
      formItems = [],
      fieldProperties = [],
      form,
      validator,
      defaults = {
        target: null,
        formclass: 'form-horizontal',
        path: 'cmis:document',
        kind: 'form',
        name: 'default',
        objectId: null,
        metadata: null,
        handlebarsId: null,
        handlebarsSettings: {},
        callback: {
          beforeCreateElement: function (item) {
          },
          afterCreateElement: function (formItem, item) {
          },
          afterCreateForm: function (form) {
          },
          afterCreateSection: function (section) {
          }
        }
      },
      settings = $.extend(true, {}, defaults, options);

    /* private utility functions */
    function createWidget(name, args) {
      var Widget = Widgets[name].Widget;
      function F(args) {
        return Widget.apply(this, args);
      }
      F.prototype = Widget.prototype;
      return new F(args);
    }

    function getLabelText(item, metadata) {
      var labelText,
        labelParams = [];
      if (item.label) {
        labelText = i18n.prop(item.label, item.label);
      }
      if (item.jsonlabel) {
        if (item.jsonlabel.params && metadata) {
          $.each(item.jsonlabel.params, function (index, el) {
            labelParams.push(metadata[el]);
          });
          labelText = i18n.prop(item.jsonlabel.key, labelParams);
        } else {
          labelText = i18n.prop(item.jsonlabel.key, item.jsonlabel['default']);
        }
      }
      return labelText;
    }

    function getInputByType(item) {
      var input = null;
      if (item.inputType === "TEXT") {
        input = $('<input type="text" />');
      } else if (item.inputType === "PASSWORD") {
        input = $('<input type="password" />');
      } else if (item.inputType === "CURRENCY") {
        input = $('<input type="text" />');
      } else if (item.inputType === "FILE") {
        input = $('<input type="file" />');
      } else if (item.inputType === "ROTEXT") {
        input = $('<input type="text" disabled />');
      } else if (item.inputType === "BUTTON") {
        input = $('<button class="btn" type="button">' + i18n.prop(item.label, item.label) + '</button>');
      } else if (item.inputType === "SUBMIT") {
        input = $('<input type="submit">');
      } else if (item.inputType === "HIDDEN") {
        input = $('<input type="hidden">');
      } else if (item.inputType === "RADIO") {
        //TODO: append to RADIOGROUP
        //TODO: checked ?
        input = $('<input type="radio" value="' + item.name + '" name="' + item.id + '" >');
      } else if (item.inputType === "TEXTAREA") {
        input = $('<textarea></textarea>');
      } else if (item.inputType === "DIV") {
        input = $('<div>' +  i18n.prop(item.text, item.text) + '</div>');
      } else if (item.inputType === "SELECT") {
        input = $('<select></select>');
        if (item.val) {
          $.each(item.val, function (index, item) {
            input.append($("<option></option>")
                .attr("value", item)
                .text(item));
          });
        }
      } else {
        UI.alert("tipo sconosciuto: " + item.inputType);
      }

      // setup other properties
      if (input) {
        input.attr("id", item.name);
        input.attr("name", item.property);
        input.addClass(item['class']);
        input.addClass(classes['form-control']);

        if (item.rows) {
          input.attr("rows", item.rows);
        }
        if (item.multiple) {
          input.attr("multiple", item.val);
        }
        if (item.type && item.type === "date") {
          input.datepicker({
            language: i18n.locale,
            autoclose: true,
            todayHighlight: true,
            todayBtn: 'linked'
          });
        }

        if (typeof input.val === 'function') {
          input.val(item.val);
        }

        if (item.placeholder) {
          input.attr("placeholder", i18n.prop(item.placeholder, item.placeholder));
        }

        input.data("property", item.property);
        input.data("validator", item.jsonvalidator);

        if (item.className) {
          input.addClass(classes[item.className] || item.className);
        }
        if (item.inputType === "FILE") {
          input.off('change').on('change', function () {
            $(this)
              .parents('div.input-append')
              .find('input:text')
              .val($(this).val().replace(/C:\\fakepath\\/i, ''));
          });
          input.css("display", "none");
          return $('<div class="input-append">')
            .append($('<input type="text" class="form-control input-xlarge" disabled id="name-' + item.name + '">'))
            .append($('<label class="btn btn-primary" for="' + item.name + '">')
              .append(input)
              .append($('<i class="icon-upload">  Upload</i>')));
        }
      }
      return input;
    }

    function getFormControlObject(item, labelText, label, input) {
      var obj = $("<div></div>").addClass(classes.group).addClass(item.parentGroupClass),
        currency = $('<div class="input-prepend input-append">').
          append('<span class="add-on">&euro;</span>').
          append(input).
          append(item.decimal ? '' : '<span class="add-on">.00</span>');
      if (labelText) {
        obj.append(label);
      }
      if (item['class'] && item['class'].indexOf('no-controls') > -1) {
        obj.append(input);
      } else {
        if (item.inputType === "CURRENCY") {
          $("<div class='controls'></div>").addClass(item.parentClass).appendTo(obj).append(currency);
        } else {
          $("<div class='controls'></div>").addClass(item.parentClass).appendTo(obj).append(input);
        }
      }

      // E' stato aggiunto nella versione 3, e per adesso funziona solo con la 3
      if ((item.visible === "false" || item.visible === false) && options.version === 3) {
        obj.css("display", "none");
      }

      if (options.version === 3) {
        label.addClass("col-sm-3");
        obj.children('.controls').addClass("col-sm-9");
      }

      return obj;
    }

    function createElement(item, metadata) {
      var obj = null,
        input = null,
        label = $('<label for="' + item.name + '"/>'),
        labelText,
        valueFunction,
        widgetArgs;

      // set up the label
      label.attr("class", item.labelClass || "control-label");
      labelText = getLabelText(item, metadata);
      label.append(labelText);

      if (typeof item.jsonlist === "string") {
        item.jsonlist = eval(item.jsonlist);
      }

      // if the field has a widget add it here
      if (item.widget) {
        // you can add additional widget arguments in place of the empty array
        widgetArgs = [item.name, labelText].concat(item).concat(label);
        obj = createWidget(item.widget, widgetArgs);
        return {
          element: obj,
          val: function () {
            return obj.data('value');
          }
        };
      }
      // set up the actual input field
      input = getInputByType(item);
      // 3 hacks for special values
      if (item.inputType === "BUTTON") {
        label = null;
      } else if (item.inputType === "RADIO") {
        obj = $('<label class="radio"> ' + label.text() + '</label>').prepend(input);
      } else if (item.type && item.type === "date") {
        valueFunction = function () {
          var d = input.data().datepicker.date;
          return d ? d.toISOString() : undefined;
        };
      }

      // put things together
      if (obj === null) {
        obj = getFormControlObject(item, labelText, label, input);
      }

      // default value-retrieval function
      valueFunction = valueFunction || function () {
        return input.val();
      };

      return {
        element: obj,
        val: valueFunction
      };
    }

    /* "public" methods */
    function getData() {

      var extraProperties = $.map($('[data-extraProperty]'), function (el) {
        var $el = $(el),
          id = $el.attr('data-extraProperty');
        return {
          id: id,
          name: id,
          val: function () {
            return $el.data('value');
          }
        };
      });

      return $.map([].concat(extraProperties).concat(formItems), function (el) {
        if (el.val && $.isArray(el.val())) {
          var multiVal = [];
          $.each(el.val(), function (index, elMulti) {
            multiVal.push({'id': el.id, 'name': el.name, 'value': elMulti});
          });
          return multiVal;
        } else {
          return {'id': el.id, 'name': el.name, 'value': el.val()};
        }
      });
    }

    function getDataJSON() {

      var result = {};
      $.each(formItems, function (index, el) {
        result[el.name] = el.val();
      });

      return result;
    }

    function getDataValueByName(name) {
      var filtered = getData().filter(function (el, index) {
        return el.name === name;
      });
      return $.map(filtered, function (el) {
        return el.value;
      });
    }

    function getDataValueById(id) {
      var filtered = getData().filter(function (el, index) {
        return el.id === id;
      });
      return filtered[0] ? filtered[0].value : null;
    }


    function addFormItem(name, value, id) {
      formItems.push({id: id, name: name, val: function () {
        return value;
      }});
    }

    function validate() {
      return form.valid();
    }

    function resetForm() {
      return validator.resetForm();
    }

    function renderElement(divContainer, rules, index, item) {
      if (settings.metadata) {
        item.val = settings.metadata[item.property];
      }
      settings.callback.beforeCreateElement(item);
      var formItem = createElement(item, settings.metadata), obj;
      fieldProperties.push(item);
      settings.callback.afterCreateElement(formItem, item);
      formItems.push({id: item.name, name: item.property, val: formItem.val});
      divContainer.append(formItem.element);

      if (item.widget) {
        formItem.element.bind('changeData', function (event, key, value) {
          // trigger validation
          if (key === "value") {
            formItem.element.valid();
          }
        });

        obj = formItem.element[0];
        obj.form = form[0];
        obj.name = item.name;
      }
      if (item.jsonvalidator) {
        rules[item.widget ? item.name : item.property] = item.jsonvalidator;
      }
    }
    function renderView(formName, form, data, rules) {
      var section = $('<section id=' + formName + '></section>').appendTo(form),
        divContainer = $('<div></div>').appendTo(section);
      if (settings.callback.afterCreateSection) {
        settings.callback.afterCreateSection(section);
      }
      $.each(data, function (index, item) {
        renderElement(divContainer, rules, index, item);
      });
    }

    function renderData(data) {
      var rules = {}, visibilityToggles;
      form = $('<form></form>')
        .addClass(settings.formclass)
        .attr('id', data.id);

      if (settings.bulkinfoLabel) {
        form.text(settings.bulkinfoLabel);
      }

      settings.target.prepend(form);

      addFormItem('cmis:objectTypeId', data.cmisObjectTypeId);
      if (settings.metadata && settings.metadata['cmis:objectId']) {
        addFormItem('cmis:objectId', settings.metadata['cmis:objectId']);
      }
      formItems.push({name: 'aspect', val: function () {
        return data.aspect;
      }});

      $.each(data.forms || data.freeSearchSets, function (index, formName) {
        renderView(formName, form, data[formName], rules);
      });

      // manage visibility of input
      visibilityToggles = $(fieldProperties).filter(function (index, el) {
        return el.inputType === "RADIOGROUP" || el.inputType === "CHECKBOX" || el.inputType === "SELECT";
      }).map(function (index, el) {
        return el.name;
      });

      visibilityToggles = $.makeArray(visibilityToggles);

      $(getData()).filter(function (index, el) {
        return visibilityToggles.indexOf(el.id) >= 0;
      }).each(function (index, el) {
        if (el.id) {
          settings.target.find('[class*="' + el.id + '"]').parents('.' + classes.group).hide();
          settings.target.find('.' + el.id + '_' + (el.value ? String(el.value).match(/[a-zA-Z0-9-_]+/, '') : '')).parents('.' + classes.group).show();
        }
      });
      validator = Validator.validate(form, {
        rules: rules
      });
      settings.callback.afterCreateForm(form);
    }

    function render() {
      var request = URL.Data.bulkInfo({
        placeholder: {
          path: settings.path,
          kind: settings.kind,
          name: settings.name
        },
        data: {
          "cmis:objectId": settings.objectId,
          "guest": true
        },
        success: renderData
      });
      return request;
    }

    // init handlebar's helpers
    function init() {

      function toHTML(el) {
        return $('<div>').append(el).html();
      }

      function displayList(value) {
        return [].concat(value).join(', ');
      }

      Handlebars.registerHelper('displayValue', function (value) {
        if (this.prop.widget === 'ui.datepicker') {
          return Handlebars.helpers.date.call(this, value, this.prop.format || 'DD/MM/YYYY');
        } else if (this.prop.jsonlist && $.isArray(this.prop.jsonlist)) {
          var filtered = this.prop.jsonlist.filter(function (el, index) {
            return el.key === String(value);
          })[0];
          return filtered ? i18n.prop(filtered.label, filtered.defaultLabel) : value;
        } else {
          return displayList(value);
        }
      });

      Handlebars.registerHelper('displayList', displayList);

      Handlebars.registerHelper('date', function date(d, format) {
        return CNR.Date.format(d, "-", format);
      });

      Handlebars.registerHelper('dueDate', function dueDate(date) {
        return toHTML(new DueDate.Widget(null, null, date));
      });

      Handlebars.registerHelper('priority', function priority(p) {
        return toHTML(new Priority.Widget(null, null, p));
      });

      Handlebars.registerHelper('plain', function plain(html) {
        return $('<div>').append(html).text();
      });

      Handlebars.registerHelper('declare', function declare(key, value) {
        hm[key] = value;
      });

      Handlebars.registerHelper('datify', function datify(key, format) {
        hm[key] = CNR.Date.format(hm[key], null, format);
      });

      Handlebars.registerHelper('i18n', function i18nFn(text) {
        var args = $(arguments), values = $.makeArray(args.filter(function (index, el) {
          return index > 0 && index < args.length - 1;
        }).map(function (index, el) {
          return hm[el] || el;
        }));

        return i18n.prop.apply(i18n, [text].concat(values));
      });
      Handlebars.registerHelper('ifCond', function (v1, v2, options) {
        if (v1 === v2) {
          return options.fn(this);
        }
        return options.inverse(this);
      });
    }

    function handlebars() {
      var xhrHandlebars, xhrBulkinfo;
      init();
      xhrHandlebars = URL.Data.handlebars({
        dataType: 'html',
        placeholder: {
          id: settings.handlebarsId + '.handlebars',
          v: common.artifact_version
        }
      });

      xhrBulkinfo = URL.Data.bulkInfo({
        placeholder: {
          path: settings.path,
          kind: 'column',
          name: settings.name
        },
        data: {
          guest: true
        }
      });

      return $.when(xhrHandlebars, xhrBulkinfo).pipe(function (source, bulkinfo) {

        var html,
          templateCode = source[0],
          columnSet = bulkinfo[0][bulkinfo[0].columnSets[0]],
          bulkinfoMap = [],
          bulkinfos = [];

        $.each([].concat(settings.metadata), function (index, properties) {
          var itemPropertiesMap = {raw: properties}, itemPropertiesArray;

          itemPropertiesArray = $.map(columnSet, function (prop) {
            var value = properties[prop.property], key;
            itemPropertiesMap[prop.name.replace(/[^\w]/g, '-')] = value;

            if (prop.label) {
              key = i18n.prop(prop.label, prop.label);
            } else if (prop.jsonlabel) {
              key = i18n.prop(prop.jsonlabel.key, prop.jsonlabel['default']);
            } else if (prop.text) {
              key = i18n.prop(prop.text, prop.text);
            } else {
              key = prop.name;
            }

            return {
              key: key,
              value: value,
              prop: prop
            };
          });

          bulkinfos.push(itemPropertiesArray);
          bulkinfoMap.push(itemPropertiesMap);
        });



        function flat(obj) {
          var re = /^[a-zA-Z0-9_]+$/;

          $.each(obj || {}, function (index, el) {
            if (!re.test(index)) {
              delete obj[index];
              obj[index.replace(/[^a-z0-9]/gi, '_')] = el;
            }

            if (typeof el === 'object') {
              flat(el);
            }
          });
        }

        flat(bulkinfoMap);

        html = Handlebars.compile(templateCode)({
          bulkinfo: bulkinfos,
          bulkinfoMap: bulkinfoMap,
          settings: settings.handlebarsSettings || {}
        });

        // postprocessing dell'html secondo i modificatori contenuti in bulkinfo

        if (settings.target) {
          settings.target.append(html);
        }

        return html;
      });
    }

    /* Revealing Module Pattern */
    return {
      render: render,
      renderData: renderData,
      renderView: renderView,
      handlebars: handlebars,
      getData: getData,
      getDataJSON: getDataJSON,
      getDataValueByName: getDataValueByName,
      getDataValueById: getDataValueById,
      getFieldProperties: function () {
        return fieldProperties;
      },
      addFormItem: addFormItem,
      validate: validate,
      resetForm: resetForm
    };
  };
});