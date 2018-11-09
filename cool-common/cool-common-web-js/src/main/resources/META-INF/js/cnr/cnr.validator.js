define(['jquery', 'bootstrap', 'i18n', 'validate', 'json!common'], function ($, bootstrap, i18n, jqvalidate, common) {

  "use strict";
  var classes = {success: 'success', error: 'error'};

  function validate(target, settings) {
    $.validator.prototype.showLabel = function (element, message) {
      //var label = this.errorsFor( element );
      var el = $(element),
        valid = message === undefined || message.length === 0,
        container = el.closest('.control-group');

      container
        .tooltip('destroy')
        .toggleClass(classes.success, valid)
        .toggleClass(classes.error, !valid);

      if (!valid && el.is(":visible")) {
        el
          .closest('.control-group')
          .tooltip({
            animation: false,
            title: message,
            trigger: "manual",
            placement: "right",
            container: container.find('.controls')
          })
          .tooltip('show')
          .data('tooltip').$tip.addClass('relative');
      }
    };


    // custom validators
    var requiredMsg = i18n.prop('message.required.field', 'required'),
      validator,
      defaults = {
        success: function (label, element) {}
      },
      numberMsg = i18n.prop('message.number', 'number'),
      minlengthMsg = $.validator.format(i18n.prop('message.minlength')),
      maxlengthMsg = $.validator.format(i18n.prop('message.maxlength'));
    $.validator.addMethod('controlloCodicefiscale',
        function (value) {
        value = value.toUpperCase();
        if (value !== "") {
          var validi, i, s, set1, set2, setpari, setdisp;
          if (value.length !== 16) {
            return false;
          }
          validi = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
          for (i = 0; i < 16; i++) {
            if (validi.indexOf(value.charAt(i)) === -1) {
              return false;
            }
          }
          set1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
          set2 = "ABCDEFGHIJABCDEFGHIJKLMNOPQRSTUVWXYZ";
          setpari = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
          setdisp = "BAKPLCQDREVOSFTGUHMINJWZYX";
          s = 0;
          for (i = 1; i <= 13; i += 2) {
            s += setpari.indexOf(set2.charAt(set1.indexOf(value.charAt(i))));
          }
          for (i = 0; i <= 14; i += 2) {
            s += setdisp.indexOf(set2.charAt(set1.indexOf(value.charAt(i))));
          }
          if (s % 26 !== value.charCodeAt(15) - 'A'.charCodeAt(0)) {
            return false;
          }
          return true;
        }
        return true;
      }, i18n['message.codicefiscale.valido']
      );

    $.validator.addMethod('controlloUserId',
      function (value) {
        if (value !== "") {
          var regex = /[a-z0-9\._\-]+[^\.^*^\s]$/gmi;
          return regex.test(value);
        }
        return true;
      }, i18n['message.userid.valido']
      );

    $.validator.addMethod('currency',
      function (value) {
        if (value !== "") {
          var regex = /^\d+(\.\d{3})*,\d{2}$/;
          return regex.test(value);
        }
        return true;
      }, i18n['message.currency.valid']
      );

    $.validator.addMethod('requiredWidget', function (value, element, enabled) {
      function isValid(e) {
        var widgetValue = e.data('value');
        return enabled && widgetValue !== undefined && widgetValue !== null;
      }

      var e = $(element);
      return isValid(e.hasClass('widget') ? e : e.parents('.widget'));
    }, requiredMsg);

    $.validator.messages.required = requiredMsg;
    $.validator.messages.number = numberMsg;
    $.validator.messages.minlength = minlengthMsg;
    $.validator.messages.maxlength = maxlengthMsg;

    $.validator.addMethod('maxlengthAlfresco', function (value, element, param) {
      // do not trim the value
      var length = typeof value === 'string' ? value.length : 0;
      return this.optional(element) || length <= param;
    }, $.validator.format(i18n.prop('message.max.length')));

    $.validator.addMethod('minlengthAlfresco', function (value, element, param) {
      // do not trim the value
      var length = typeof value === 'string' ? value.length : 0;
      return this.optional(element) || length >= param;
    }, $.validator.format(i18n.prop('message.min.length')));


    $.validator.addMethod('digitsAlfresco', function (value, element, param) {
      // do not allow spaces
      return typeof value === 'string' && (value.length === 0 || /^\d+$/.test(value));
    }, $.validator.format(i18n.prop('message.digits')));
     // check controlloAltroCampo
    $.validator.addMethod('controlloAltroCampo', function (value, element, param) {
      var elem_id_1, elem_id_2;
      elem_id_1 = $.find('[name*="' + param + '"]').length - 1;
      elem_id_2 = $.find('[name*="' + element.name + '"]').length - 1;
      if ($.find('[name*="' + param + '"]')[elem_id_1].value.length > 0) {
        return ($.find('[name*="' + element.name + '"]')[elem_id_2].value.length > 0);
      }
      if ($.find('[name*="' + param + '"]')[elem_id_1].value.length === 0) {
        return true;
      }
    }, $.validator.format(i18n.prop('message.required.field')));
    $.validator.methods.equalsTo = function (value, element, param) {
      var value2 = $(param).val();
      return value === value2;
    };
    validator = target.validate($.extend({}, defaults, settings));

    $.fn.resetForm = function () {
      validator.elements().tooltip('destroy');
      validator.elements().closest('.control-group').tooltip('destroy');
      validator.elements().closest('.control-group').removeClass(
        $.map(classes, function (val) {
          return val;
        }).join(' ')
      );
    };

    return validator;
  }

  return {
    validate: validate,
    addMethod: function (name, func, message) {
      $.validator.addMethod(name, func, message);
    }
  };

});