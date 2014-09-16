define(['jquery', 'header', 'cnr/cnr.explorer', 'cnr/cnr.actionbutton', 'cnr/cnr.url'], function ($, header, Explorer, ActionButton, URL) {
  "use strict";

  Explorer.init({
    dom: {
      explorerItems: $('.explorerItem'), // items to show only in file explorer mode
      tree: $('#collection-tree'),
      breadcrumb: $('.breadcrumb'),
      search: {
        table: $('#items'),
        pagination: $('#itemsPagination'),
        label: $('#emptyResultset')
      },
      buttons: {
        createGroup: $('#createGroup'),
        createAssociation: $('#createAssociation')
      }
    },
    tree: {
      rootFn: URL.Data.proxy.rootGroup,
      paramName: 'fullName',
      childrenFn: URL.Data.proxy.childrenGroup,
      customClass: 'groups'
    },
    search: {
      display: {
        row: function (el) {
          var item = {
            baseTypeId: el.attr.type,
            allowableActions: el.attr.allowableActions,
            nodeRef: el.attr.id,
            name: el.data,
            group: el.group,
            authorityId: el.attr.authorityId
          },
            isUser = el.attr.type === 'USER',
            btn = ActionButton.actionButton(item, null, null, null, Explorer.refresh),
            td = $('<td></td>'),
            row = $('<tr></tr>'),
            a = $('<a href="#">' + el.attr.displayName + '</a>').click(function () {
              Explorer.folderChange(item);
            });

          td.append('<i class="' + (isUser ? 'icon-user' : 'icon-group icon-blue') + '"></i> ')
            .append(isUser ? el.attr.displayName : a)
            .append('<span class="muted annotation">' + el.attr.shortName + '</span>');

          row
            .append(td)
            .append($('<td></td>').append(btn));

          return row;
        }
      },
      dataSource: function (page, settings) {
        var groupName = settings.lastCriteria.conditions ? settings.lastCriteria.conditions[0].what : null, xhr;
        xhr = URL.Data.proxy.childrenGroup({
          data: {
            fullName: groupName
          }
        });

        return xhr.pipe(function (data) {
          var enhancedData = $.map(data, function (el) {
            el.group = groupName;
            return el;
          });
          return enhancedData;
        });
      }
    }
  });
});