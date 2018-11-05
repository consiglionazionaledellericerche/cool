define(['jquery', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.ui.authority', 'i18n', 'cnr/cnr.node', 'cnr/cnr.url', 'cnr/cnr.bulkinfo'], function ($, CNR, UI, Authority, i18n, Node, URL, BulkInfo) {
  "use strict";

  var regex = /\{.*\}cmobject\.([a-zA-Z]+)/g,
    table,
    objectId,
    displayAcl,
    roleDescription = {
      Coordinator: "The coordinator gets all permissions and permission groups defined.",
      Collaborator: "Combines Editor and Contributor permission groups.",
      Contributor: "Includes the Consumer permission group and adds AddChildren and CheckOut. They will, by default own anything they create and have the ROLE_OWNER authority.",
      Editor: "Include the Consumer permission group and adds Write and CheckOut.",
      Consumer: "Includes Read"
    },
    allRoles;

  function addAcl() {

    var widget = new Authority.Widget('acl'),
      tr = $('<tr></tr>'),
      ul = $('<div class="btn-group" data-toggle="buttons-checkbox"></div>').css('line-height', '40px'),
      confirm = $('<button type="button" class="btn btn-mini btn-success">confirm</button>'),
      td = $('<td></td>');
    $('<td></td>').append(widget).appendTo(tr);
    $.each(allRoles, function (index, role) {
      var label = role.replace(regex, '$1');
      $('<button type="button" data-role="' + role + '" class="btn btn-mini">' + i18n.prop('acl.' + label, label) + '</button>')
        .appendTo(ul)
        .tooltip({
          title: roleDescription[label],
          placement: "bottom"
        });
    });
    confirm.on('click', function () {
      var principalId = widget.data('value'), permissions = $.map(ul.find('.active'), function (el) {
        return {
          role: $(el).data('role'),
          authority: principalId
        };
      });
      if (permissions.length === 0) {
        UI.error("Selezionare almeno un ruolo!");
        return;
      }
      if (!principalId) {
        UI.error("Selezionare un Utente o un Gruppo!");
        return;
      }
      URL.Data.proxy.permissions({
        type: 'POST',
        contentType: 'application/json',
        placeholder: {
          node: objectId.replace('://', '/').split(';')[0]
        },
        data: JSON.stringify({
          permissions: permissions
        }),
        success: function () {
          displayAcl();
        }
      });
    });

    td.appendTo(tr).append(ul);
    tr.append($('<td></td>').css('line-height', '40px').append(confirm));
    return tr;
  }

  function deleteAcl(authority) {

    var permissionsToDelete = $.map(allRoles, function (role) {
        return {
          remove: true,
          authority: authority,
          role: role
        };
      });

    URL.Data.proxy.permissions({
      type: 'POST',
      contentType: 'application/json',
      placeholder: {
        node: objectId.replace('://', '/').split(';')[0]
      },
      data: JSON.stringify({
        permissions: permissionsToDelete
      }),
      success: function () {
        displayAcl();
      }
    });
  }

  function showMetadata(authority, displayGroups) {
    var commonSettings = {
      data: {
        filter: authority,
        maxItems: 1
      }
    }, specificSettings;

    if (authority.indexOf("GROUP_") === -1) {
      URL.Data.proxy.people({
        type: 'GET',
        contentType: 'application/json',
        data: {
          groups: displayGroups
        },
        placeholder: {
          user_id: authority
        },
        success: function (data) {
          var dataPeopleUser = data,
            bulkInfo = 'accountBulkInfo';
          if (dataPeopleUser.email === 'nomail') {
            dataPeopleUser.email = dataPeopleUser.emailesterno || dataPeopleUser.emailcertificatoperpuk;
          }
          dataPeopleUser.email = '<a href="mailto:' + dataPeopleUser.email + '">' + dataPeopleUser.email + '</a>';
          new BulkInfo({
            handlebarsId: 'zebra',
            kind: 'column',
            name: 'displayUser',
            path: bulkInfo,
            metadata: dataPeopleUser
          }).handlebars().done(function (html) {
            var content = $('<div></div>').addClass('modal-inner-fix').append(html),
              title = i18n.prop("modal.title.view." + bulkInfo, 'Propriet&agrave;'),
              ol = $('<ol>'),
              a;
            if (displayGroups) {
              $.each(dataPeopleUser.groups, function (index, el) {
                a = $('<a>' + el.displayName + '</a>').attr('href', '#').click(function (eventObject) {
                  URL.Data.proxy.members({
                    placeholder: {
                      group_name: el.itemName
                    },
                    success: function (data) {
                      var contentMembers = $("<div>").addClass('modal-inner-fix'), olMembers = $('<ol>').appendTo(contentMembers);
                      $.each(data.people, function (index, el) {
                        $('<li>').append(el).appendTo(olMembers);
                      });
                      UI.modal(i18n.prop('label.members', el.displayName), contentMembers);
                    }
                  });
                });
                $('<li>').append(a).appendTo(ol);
              });
              content.append($('<h3>').append(i18n.prop('page.groups'))).append($('<hr>')).append(ol);
            }
            UI.modal(title, content);
          });
        },
        error: function () {
          UI.error(i18n['message.user.not.found']);
        }
      });
    } else {
      specificSettings = {
        success: function (groups) {
          if (groups.groups[0]) {
            Node.displayMetadata('cm:authorityContainer', groups.groups[0].nodeRef, false,
              function (div) {
                var tr = $('<tr></tr>'), tdAuthority = $('<td name="tdAuthority"></td>');
                URL.Data.proxy.members({
                  placeholder: {
                    group_name: authority
                  },
                  success: function (data) {
                    $.each(data.people, function (index, authority) {
                      $('<a href="#tdAuthority">' + authority + '</a><span> </span>').off('click').on('click', function () {
                        showMetadata(authority);
                      }).appendTo(tdAuthority);
                    });
                  }
                });
                tr.append('<td><strong>Members</strong></td>');
                tr.append(tdAuthority);
                div.find('table > tbody').append(tr);
              });
          } else {
            UI.alert("No information found for: " + authority);
          }
        }
      };
      URL.Data.proxy.groups($.extend({}, commonSettings, specificSettings));
    }
  }
  displayAcl = function () {
    URL.Data.proxy.permissions({
      placeholder: {
        node: objectId.replace('://', '/').split(';')[0]
      },
      success: function (data) {
        var tableBody = table.find('tbody'), types = ["direct"], permissionMap = {};
        tableBody.find('tr').remove();


        if (data.isInherited) {
          types.push("inherited");
        }

        $.each(types, function (index, key) {
          $.each(data[key], function (index, permission) {
            var authority = permission.authority.name;
            if (!permissionMap[authority]) {
              permissionMap[authority] = {direct: [], inherited: []};
            }
            permissionMap[authority][key].push(permission.role);
          });
        });

        $.each(permissionMap, function (authority, permission) {
          $.each(types, function (index, key) {
            if (permission[key].length) {
              var deleteBtn = $('<button type="button" class="btn btn-mini btn-danger"><i class="icon-remove icon-white"></i> delete</button>').click(function () {
                deleteAcl(authority);
              }), tr = $('<tr></tr>'), td = $('<td></td>'),
                a = $('<a href="#tdAuthority">' + authority + '</a>').off('click').on('click', function () {
                  showMetadata(authority);
                }),
                tdAuthority = $('<td name="tdAuthority"></td>').append(a);
              tr.append(tdAuthority);
              permission[key] = $.map(permission[key], function (el) {
                return i18n.prop('acl.' + el, el);
              });
              tr.append('<td>' + permission[key].join(', ') + '</td>');
              td.append(key === "direct" ? deleteBtn : '-');
              tr.append(td);
              tableBody.append(tr);
            }
          });
        });

        table.find('.create-acl').off('click').on('click', function () {
          var tr = addAcl();
          tableBody.append(tr);
        });

        if (data.isInherited) {
          table.find('.inherit-acl').addClass('active');
        }

        if (allRoles === undefined) {
          allRoles = data.settable;
        }
      }
    });
  };

  function init(mtable, mobjectId) {
    table = mtable;
    objectId = mobjectId;
    displayAcl();

    var suffix = objectId.replace('://', '/');

    table.find('.inherit-acl').click(function () {
      var active = $(this).hasClass('active');

      URL.Data.proxy.permissions({
        placeholder: {
          node: suffix.split(';')[0]
        },
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
          permissions: [],
          isInherited: !active
        }),
        success: function () {
          displayAcl();
        }
      });
    });
  }

  /* public */
  function show(objectId, setting) {
    var table = $('<table class="table table-striped ace"></table>'),
      tfoot = $('<tfoot><tr><td colspan="3"></td></tr></tfoot>');
    table.append('<thead><tr><th>Authority</th><th>Role</th><th>Action</th></tr></thead>');
    table.append('<tbody></tbody>');
    tfoot.appendTo(table);

    tfoot.find('td')
      .append('<button type="button" class="btn btn-mini btn-primary create-acl"><i class="icon-plus icon-white"></i> Crea nuova ACL</button>');
    if (setting.inheritButton) {
      tfoot.find('td')
        .append(' ')
        .append('<button type="button" class="btn btn-mini inherit-acl" data-toggle="button">Eredita autorizzazioni</button>');
    }
    if (setting.roles) {
      allRoles = setting.roles;
    }

    init(table, objectId);

    return table;
  }

  function showPanel(objectId, name, roles, inherit) {
    var setting = {
      name : name,
      roles : roles,
      inheritButton : inherit === undefined ? true : inherit
    }, content = $('<div><div>').addClass('modal-inner-fix');
    content.append(show(objectId, setting));
    UI.bigmodal('Modifica i permessi ' + (name ? ' di "' + name + '"' : ''), content);
  }

  return {
    panel: showPanel,
    showMetadata: showMetadata,
    show: show
  };
});