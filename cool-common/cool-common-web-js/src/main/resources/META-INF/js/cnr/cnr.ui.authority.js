/* javascript closure providing all the authority functionalities */
define(['cnr/cnr.url'], function (URL) {
  "use strict";

  var minLength = 3, maxItems = 10;

  /* private methods */
  function getAssignee(assigneeType, query, cb, isValidation) {
    isValidation = isValidation || false;
    var commonSettings = {
      queue: isValidation ? 'validation' : 'search', // we need two different XHRs queues
      dataType: "json",
      error: function (jqXHR, textStatus, errorThrown) {
        cb([]);
      }
    }, specificSettings;

    if (query.length < minLength) {
      cb([]);
    } else if (assigneeType === "users") {
      specificSettings = {
        data: {
          filter: '*' + query + '*',
          maxItems: maxItems
        },
        success: function (users) {
          var map = $.map(users.people, function (n) {
            return isValidation ? n.userName : n.userName + ' (' + n.firstName + ' ' + n.lastName + ' - ' + n.email + ')';
          });
          cb(map);
        }
      };
      URL.Data.proxy.person($.extend({}, commonSettings, specificSettings));
    } else {
      specificSettings = {
        data: {
          filter: '*' + query + '*',
          maxItems: maxItems
        },
        success: function (data) {
          var map = $.map(data.groups, function (n) {
            return n.authorityName;
          });
          cb(map);
        }
      };
      URL.Data.proxy.groups($.extend({}, commonSettings, specificSettings));
    }
  }

  function validateAssignee(assigneeType, query, controlGroup) {
    query = query.replace(/\(.*\)/g, '').trim();
    getAssignee(assigneeType, query, function (data) {
      var errorClass = "error", successClass = "success", isValid = data.indexOf(query) >= 0;
      if (isValid) {
        controlGroup.find('input').trigger('blur');
      }
      controlGroup
        .addClass(isValid ? successClass : errorClass)
        .removeClass(isValid ? errorClass : successClass)
        .data('value', isValid ? query : null);
    }, true);
  }

  function init(input, type, controlGroup) {
    var defaultType = 'users';
    // change between user / group
    if (type) {
      type.find("button").on("click", function () {
        var query = input.val();
        console.log(query);
        validateAssignee($(this).data("type"), query, controlGroup);
      });
    }
    // input change and auto-completion
    input.
      bind("reset", function () {
        input.val('');
        input.trigger("change");
      })
      .on("keyup change", function () {
        var query = input.val();
        console.log(query);
        validateAssignee(type ? type.find(".active").data("type") : defaultType, query, controlGroup);
      })
      .typeahead({
        minLength: 3,
        hint: true,
        highlight: true
      }, {
        updater: function (item) {
          return item.replace(/\(.*\)/g, '').trim();
        },
        name: 'authority',
        source: function (query, process) {
          console.log(arguments);
          var assigneeType = type ? type.find(".active").data("type") : defaultType;
          getAssignee(assigneeType, query, function (map) {
            //process([{value: "fracesco.uliana"}, {value: "francesco.totti"}]);
            var xx = $.map(map, function (el) {
              return {
                value: el
              };
            })
            process(xx);
          });
        },
        items: maxItems
      });
  }

  /* public methods */
  function widget(id, labelText, bulkItem) {
    bulkItem = bulkItem || {};

    var assigneeType,
      value = bulkItem.val || bulkItem['default'],
      assignee = $('<input type="text" class="form-control" data-provide="typeahead" />').addClass((bulkItem || {})['class'] || 'input-medium').attr('id', id).attr('name', id).val(value),
      controls = $('<div class="controls"></div>').append(assignee).append(' '),
      label = $('<label class="control-label"></label>').attr('for', id).text(labelText || ''),
      item = $('<div class="form-group authority widget"></div>'),
      buttonUsers = $('<button type="button" class="btn btn-mini" data-type="users">Utenti</button>'),
      buttonGroups = $('<button type="button" class="btn btn-mini" data-type="groups">Gruppi</button>'),
      settings = bulkItem.jsonsettings || {};

    assigneeType = $('<div class="btn-group" data-toggle="buttons-radio"></div>')
      .append(buttonUsers)
      .append(' ')
      .append(buttonGroups)
      .appendTo(controls);

    if (settings.groupsOnly) {
      buttonGroups.addClass('active');
    } else {
      buttonUsers.addClass('active');
    }

    if (settings.usersOnly || settings.groupsOnly) {
      assigneeType.hide();
    }

    item
      .data('id', id)
      .data('value', value || null)
      .append(label)
      .append(controls);

    init(assignee, assigneeType, item);

    return item;
  }

  return {
    Widget: widget
  };
});