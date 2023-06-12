define(['jquery', 'json!common', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.node', 'cnr/cnr.bulkinfo', 'cnr/cnr.ace', 'i18n', 'cnr/cnr.url', 'cnr/cnr.criteria', 'cnr/cnr.search' ], function ($, common, CNR, UI, Node, BulkInfo, Ace, i18n, URL, Criteria, Search) {
  "use strict";

  /**
   * Populates the split button's dropdown
   *
   * add to the dropdown menu all the actions contained in parameter "buttons"
   *
   * @param {object} dropdown the jQuery element representing the split button's dropdown menu
   * @param {object} buttons an object (key: action name, value: the function to be executed OR the object representing the submenu) containing the buttons to be added
   * @param {object} icons an hash containing the icons for the buttons
   *
   * @return {object} dropdownElements all the buttons added to the dropdown
   *
   */
  function populateDropdown(dropdown, buttons, icons, titles) {

    var dropdownElements = {};

    $.each(buttons, function (action, fn) {

      var simpleButton = typeof fn !== 'object',
        btn,
        ul,
        li = $('<li></li>'),
        title = titles[action] || i18n['actions.' + action];

      btn = $(simpleButton ? '<a href="#"></a>' : '<span class="dropdown-parent"></span>')
        .attr('data-name', action)
        .append('<i class="' + icons[action] + '"></i> ')
        .append(title)
        .appendTo(li);

      if (simpleButton) {
        dropdown.append(li);
        if (typeof fn === 'function') {
          btn.on('click', function () {
            fn($(this));
            return false;
          });
        }
      } else {

        if (fn && Object.keys(fn).length) {
          ul = $('<ul class="dropdown-menu"></ul>');

          ul.append('<li class="nav-header">' + title + '</li>');

          li
            .addClass('dropdown-submenu pull-left cool-pull-left')
            .append(ul);

          $.each(fn, function (label, fn) {
            var li = $('<li></li>'), a = $('<a href="#">' + label + '</a>');
            if (typeof fn === 'string') {
              a.attr('href', fn);
            } else if (typeof fn === 'function') {
              a.click(function () {
                fn();
                return false;
              });
            }
            li.append(a).appendTo(ul);
          });
          dropdown.append(li);
        }
      }

      dropdownElements[action] = btn;
    });

    return dropdownElements;
  }


  // update document content
  function updateDocument(nodeRef, mimeType, refreshFn, maxUploadSize) {
    // inline editor requires Blob object and a compatible mimetype
    if (window.Blob && ["application/json", "text/plain", "text/csv"].indexOf(mimeType) >= 0) {
      URL.Data.search.content({
        data: {
          'nodeRef': nodeRef
        },
        dataType: 'text'
      }).done(function (data) {
        Node.updateContentEditor(data, mimeType, nodeRef, refreshFn);
      });
    } else {
      Node.submission({
        nodeRef: nodeRef,
        crudStatus: 'UPDATE',
        requiresFile: false,
        success: refreshFn,
        maxUploadSize: maxUploadSize
      });
    }
  }

  // remove group/user association
  function removeAssociation(obj, refreshFn) {
    var group  = obj.group, authorityId = obj.authorityId;
    UI.confirm('Sei sicuro di voler eliminare la relazione ' + name + '?', function () {
      URL.Data.proxy.childrenGroup({
        type: 'DELETE',
        placeholder: {
          childFullName: authorityId,
          parentNodeRef: group
        }
      }).done(function () {
        UI.success('eliminata l\'associazione con ' + authorityId);
        if (typeof refreshFn === 'function') {
          refreshFn();
        }
      }).error(function () {
        UI.error('impossibile eliminare l\'associazione con ' + authorityId);
      });

    });
  }

  // edit group properties (e.g. name)
  function editGroup(nodeRef) {
    var content = $("<div></div>"),
      bulkinfo,
      afterRender = function () {
        UI.modal(i18n["modal.title.edit.cm.authorityContainer"], content, function () {
          Node.updateMetadataNode(nodeRef, bulkinfo.getData());
        });
      };
    URL.Data.proxy.metadata({
      data: {
        "nodeRef" : nodeRef,
        "shortQNames" : true
      },
      success: function (metadata) {
        bulkinfo = new BulkInfo({
          target: content,
          path: "cm:authorityContainer",
          metadata: metadata.properties,
          callback: {
            afterCreateForm: afterRender
          }
        });
        bulkinfo.render();
      }
    });
  }

  // delete group and subgroups
  function removeGroup(nodeRef, refreshFn) {
    UI.confirm('Sei sicuro di voler eliminare il gruppo ' + name + ', e tutti i sottogruppi?', function () {
      URL.Data.proxy.group({
        type: 'DELETE',
        placeholder: {
          group_node_ref: nodeRef,
          "cascade" : true
        }
      }).done(function () {
        UI.success('eliminato il gruppo <strong>' + name + '</strong>');
        if (typeof refreshFn === 'function') {
          refreshFn();
        }
      }).error(function () {
        UI.error('impossibile eliminare il gruppo <strong>' + name + '</strong>');
      });
    });
  }
  //Show history of documents
  function showHistory(nodeRef) {
    var myModal, textAbilita = 'Abilita revisioni', textDisabilita = 'Disabilita revisioni',
      content = $('<div></div>').addClass('modal-inner-fix'),
      table = $('<table class="table table-striped table-bordered"></table>').appendTo(content),
      tbody = $('<tbody></tabody>').appendTo(table),
      btnEnableRevisions = $('<button type="button" class="btn enable-versioning pull-left" data-toggle="button">Abilita revisioni</button>').click(function () {
        var active = $(this).hasClass('active');
        Node.updateMetadataNode(nodeRef.split(';')[0],
          [{"name" : "cm:autoVersion", "value" : !active}, {"name" : "cm:autoVersionOnUpdateProps", "value" : false}]);
        if (active) {
          $(this).text(textAbilita);
        } else {
          $(this).text(textDisabilita);
        }
      }),
      criteria = new Criteria(),
      versioni = new Search({
        dataSource: function () {
          return URL.Data.search.version({
            queue: true,
            data: {
              nodeRef: nodeRef
            }
          });
        },
        display : {
          row : function (el, refreshFn, permission) {
            $('<tr>').appendTo(tbody).append($('<td>').append('<b>Versione </b>')
              .append($('<a class="label label-info versionLabel">').on('click', function (event) {
                window.location = URL.urls.search.content + '?nodeRef=' + el['cmis:objectId'];
                return false;
              })
                 .append(el['cmis:versionLabel']))
              .append($('<span class="lastModifiedBy">').append(' aggiornata da ').append($('<a href="#">')
                 .append(el['cmis:lastModifiedBy'])))
              .append($('<span class="lastModificationDate">')
                 .append(' ' + CNR.Date.format(el['cmis:lastModificationDate'])))
              .append($('<span class="contentStreamLength">')
                 .append(', di dimensione ' + CNR.fileSize(el.contentStreamLength)))
              );
          },
          after : function (documents) {
            tbody.find('.lastModifiedBy').off('click').on('click', function (event) {
              Ace.showMetadata(event.target.text);
              return false;
            });
            URL.Data.proxy.metadata({
              data: {
                "nodeRef" : nodeRef.split(';')[0],
                "shortQNames" : true
              },
              success: function (metadata) {
                myModal = UI.modal('Cronologia revisioni', content);
                myModal.find('.modal-footer').append(btnEnableRevisions);
                btnEnableRevisions.text(textAbilita);
                if (metadata.properties['cm:autoVersion']) {
                  btnEnableRevisions.addClass('active');
                  btnEnableRevisions.text(textDisabilita);
                }
              }
            });
          }
        }
      });
    criteria.list(versioni);
  }
  // returns a new "actions button"
  function actionButton(obj, customPermission, customButtons, customIcons, refreshFn, nodropdown, maxUploadSize, extendClass, titles) {
    var name = obj.name || "",
      nodeRef = obj.nodeRef ? obj.nodeRef.split(';')[0] : obj.nodeRef,
      baseTypeId = obj.baseTypeId || 'cmis:document',
      objectTypeId = obj.objectTypeId || 'cmis:document',
      allowableActions = obj.allowableActions || [],
      myClass = 'btn-actions',
      storageKey = 'action' + baseTypeId,
      defaultChoice = obj.defaultChoice || 'select', // default action
      choice = CNR.Storage.get(storageKey, defaultChoice), //last used action
      splitButton,
      dropdown = $('<ul class="dropdown-menu"></ul>'),
      mimeType,
      warningClass = defaultChoice === 'application' ? "btn-warning" : '',
      buttons = {},
      icons = $.extend({
        permissions: 'icon-user',
        edit: 'icon-pencil',
        remove: 'icon-trash',
        select: 'icon-eye-open',
        update: 'icon-upload',
        removeAssociation: 'icon-resize-full',
        history: 'icon-th-list',
        copy: 'icon-copy',
        cut: 'icon-cut'
      }, customIcons),
      permissions = $.extend({
        edit: 'CAN_UPDATE_PROPERTIES',
        update: 'CAN_SET_CONTENT_STREAM',
        remove: 'CAN_DELETE_OBJECT',
        permissions: 'CAN_APPLY_ACL',
        removeAssociation: 'CAN_DELETE_ASSOCIATIONS',
        copy: 'CAN_MOVE_OBJECT',
        cut: 'CAN_MOVE_OBJECT'
      }, customPermission);
    function addActions(actions) {
      var btns = {};
      $.each(actions || {}, function (action, fn) {
        if (fn === false) {
          delete buttons[action];
        } else {
          if (permissions[action] === undefined || allowableActions.indexOf(permissions[action]) >= 0) {
            btns[action] = fn;
          }
        }
      });
      $.extend(buttons, btns);
    }

    // shared actions (i.e. actions common to several object types)
    addActions({
      permissions: function () {
        var authorityRoles;
        if (baseTypeId === "GROUP" || baseTypeId === "USER") {
          authorityRoles = ["FullControl", "Read", "Write"];
        }
        Ace.panel(nodeRef, name, authorityRoles);
      },
      edit: function () {
        var content = $("<div></div>").addClass('modal-inner-fix'), bulkinfo,
          afterRender = function () {
            UI.modal("Modifica " + (baseTypeId === 'cmis:folder' ? 'cartella' : 'documento'), content, function () {
              if (!bulkinfo.validate()) {
                UI.alert("alcuni campi non sono corretti");
                return false;
              }
              var d = bulkinfo.getData();
              d.push({
                id: 'cmis:objectId',
                name: 'cmis:objectId',
                value: nodeRef
              });
              Node.updateMetadata(d, refreshFn);
            });
          };
        bulkinfo = new BulkInfo({
          target: content,
          path: obj.objectTypeId,
          objectId: nodeRef,
          callback: {
            afterCreateForm: afterRender
          }
        });
        bulkinfo.render();
      },
      remove: function () {
        UI.confirm('Sei sicuro di voler eliminare ' + (baseTypeId === 'cmis:folder' ? 'la cartella ' : 'il documento') + ' ' + name  + '?', function () {
          Node.remove(nodeRef, refreshFn);
        });
      },
      removeAssociation: function () {
        removeAssociation(obj, refreshFn);
      },
      copy: function () {
        CNR.Storage.set('nodeRefToCopy', nodeRef);
        CNR.Storage.set('nodeRefToCut', '');
      },
      cut: function () {
        CNR.Storage.set('nodeRefToCopy', '');
        CNR.Storage.set('nodeRefToCut', obj.id);
      }
    });

    // ad hoc actions
    if (baseTypeId === "GROUP") {
      addActions({
        select: function () {
          Node.displayMetadata('cm:authorityContainer', nodeRef, false,
            function (div) {
              var tr = $('<tr></tr>'), tdAuthority = $('<td name="tdAuthority"></td>');
              URL.Data.proxy.members({
                placeholder: {
                  group_name: obj.authorityId
                },
                success: function (data) {
                  $.each(data.people, function (index, authority) {
                    $('<a href="#tdAuthority">' + authority + '</a><span> </span>').off('click').on('click', function () {
                      Ace.showMetadata(authority);
                    }).appendTo(tdAuthority);
                  });
                }
              });
              tr.append('<td><strong>Members</strong></td>');
              tr.append(tdAuthority);
              div.find('table > tbody').append(tr);
            });
        },
        edit: function () {
          editGroup(nodeRef);
        },
        remove: function () {
          removeGroup(nodeRef, refreshFn);
        }
      });
    } else if (baseTypeId === "USER") {
      addActions({
        select: function () {
          Node.displayMetadata('cm:person', nodeRef);
        },
        edit: false,
        remove: false
      });
    } else if (baseTypeId === 'cmis:folder') {
      myClass = 'btn-actions-folder';
      addActions({
        select: function () {
          $(this).parents('tr').find('.openFolder').click();
        }
      });
    } else {
      mimeType = obj.mimeType;
      addActions({
        select: function () {
          Node.displayMetadata(objectTypeId, nodeRef, true);
        },
        update: function () {
          updateDocument(nodeRef, mimeType, refreshFn, maxUploadSize);
        },
        history: function () {
          showHistory(nodeRef);
        }
      });
    }

    addActions(customButtons);

    buttons = populateDropdown(dropdown, buttons, icons, titles || {});
    if (nodropdown) {
      dropdown.css('display', 'block').css('position', 'static');
      dropdown.addClass('pull-right');
      return dropdown;
    }
    splitButton = $('<div class="btn-group btn-prevent-fade"></div>')
      .addClass(myClass + " " + extendClass)
      .append((buttons[choice] || buttons[defaultChoice] || buttons[Object.keys(buttons)[0]]).clone(true).addClass("btn btn-mini").addClass(warningClass))
      .append($('<button class="btn btn-mini dropdown-toggle" data-toggle="dropdown"><span class="caret"></span></button>').addClass(warningClass));

    splitButton.hover(function () {
      if (!$(this).hasClass('open')) {
        $(this).find('.dropdown-toggle').click();
      }
    });

    dropdown
      .appendTo(splitButton)
      .on("click touchend", "a", function () {
        var el = $(this),
          name = el.data("name"),
          btns = el.parents('.' + myClass).parents('table').find('.' + myClass);
        CNR.Storage.set(storageKey, name);

        $.each(btns, function (key, group) {
          var $group = $(group),
            oldBtn = $group.find('a.btn'),
            selectBtn = $group.find('ul [data-name=' + name + ']'),
            what = selectBtn.length ? selectBtn : $group.find('ul [data-name=' + defaultChoice + ']'),
            newBtn = what.clone(true).addClass('btn btn-mini').addClass(warningClass);
          oldBtn.replaceWith(newBtn);
        });
      });

    return splitButton;
  }

  return {
    actionButton: actionButton
  };
});
