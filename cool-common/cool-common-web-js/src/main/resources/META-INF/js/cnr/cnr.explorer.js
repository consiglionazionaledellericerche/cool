/* document Explorer Module */
define(['jquery', 'json!common', 'cnr/cnr', 'cnr/cnr.node', 'cnr/cnr.ui', 'cnr/cnr.tree', 'cnr/cnr.search', 'cnr/cnr.ui.authority', 'cnr/cnr.actionbutton', 'cnr/cnr.url', 'json!cache', 'i18n'], function ($, common, CNR, Node, UI, Tree, Search, Authority, ActionButton, URL, cache, i18n) {
  "use strict";

  // default Explorer settings
  var search, //CNR.Search object
    settings, // current Explorer settings
    path = [], // breadcrumb path
    selectedFolder = null, //current selected folder
    nameOfCopyDiv = $('<div><div class="control-group"><div class="controls"><input type="text" id="nameOfCopy" name="cmis:name"></div></div></div>');

  /* utility functions */

  // pluggable search function
  function searchFunction(nodeRef, isRoot) {
    settings.search.searchFunction(nodeRef, search, isRoot || false);
  }

  // public function to refresh the result grid
  function refresh() {
    Tree.refresh();
    searchFunction(selectedFolder);
  }

  /* enable/disable buttons responsible for folder/document creation */
  function initCreationButtons(allowableactions) {
    function initCreationButton(btn, action, allowableactions, fn) {
      btn.off('click');
      if ($.inArray(action, allowableactions) >= 0) {
        btn
          .on('click', fn)
          .removeClass('disabled');
      } else {
        btn
          .on('click', function () {
            CNR.log('unauthorized');
          })
          .addClass('disabled');
      }
    }

    function createFolder() {
      UI.dialog('Inserire il nome della cartella', function (data) {
        var type = 'cmis:folder';
        URL.Data.folder({
          type: 'POST',
          data: {
            'cmis:name': data,
            'cmis:objectTypeId': type,
            root: false,
            'cmis:parentId': selectedFolder,
            folderType: type
          }
        }).done(function () {
          Tree.refresh();
          refresh();
        });
      });
    }

    function createDocument() {
      Node.submission({
        nodeRef: selectedFolder,
        crudStatus: 'INSERT',
        success: refresh
      });
    }

    function paste() {
      var nodeRefToCopy = CNR.Storage.get('nodeRefToCopy'),
        nodeRefToCut = CNR.Storage.get('nodeRefToCut'),
        close;
      if (nodeRefToCopy) {
        URL.Data.node.node({
          data: {
            nodeRef: nodeRefToCopy
          },
          success: function (response) {
            nameOfCopyDiv.find('#nameOfCopy').val('copyOf' + response['cmis:name']);
            UI.modal(i18n['message.nameOfCopy'], nameOfCopyDiv, function () {
              close = UI.progress();
              URL.Data.node.copy({
                type: 'POST',
                data: {
                  nodeRefToCopy: nodeRefToCopy,
                  nodeRefDest: selectedFolder,
                  newName: $('#nameOfCopy').val()
                },
                success: function (response) {
                  close();
                  if (response.status === 'ok') {
                    UI.info(i18n['message.copy.success']);
                  } else {
                    UI.error(i18n['message.copy.failed'] + ': ' + response.message);
                  }
                  refresh();
                }
              });
            },
              false);
          }
        });
      } else if (nodeRefToCut) {
        close = UI.progress();
        URL.Data.node.cut({
          type: 'POST',
          data: {
            nodeRefToCopy: nodeRefToCut,
            nodeRefDest: selectedFolder
          },
          success: function (response) {
            close();
            if (response.status === 'ok') {
              UI.info(i18n['message.cut.success']);
            } else {
              UI.error(i18n['message.cut.failed'] + ': ' + response.message);
            }
            refresh();
          }
        });
      } else {
        UI.info(i18n['message.copyToPaste']);
      }
    }

    if (settings.dom.buttons.createFolder) {
      initCreationButton(settings.dom.buttons.createFolder, 'CAN_CREATE_FOLDER', allowableactions, createFolder);
    }

    if (settings.dom.buttons.uploadDocument) {
      initCreationButton(settings.dom.buttons.uploadDocument, 'CAN_CREATE_DOCUMENT', allowableactions, createDocument);
    }

    if (settings.dom.buttons.paste) {
      initCreationButton(settings.dom.buttons.paste, 'CAN_UPDATE_PROPERTIES', allowableactions, paste);
    }


    function addChild(parent, child) {
      URL.Data.proxy.childrenGroup({
        type: 'POST',
        data: JSON.stringify({
          'parent_group_name': parent,
          'child_name': child
        }),
        contentType: 'application/json'
      }).done(function () {
        UI.success(child + ' aggiunto al gruppo ' + parent);
        refresh();
      }).fail(function () {
        UI.error("impossibile aggiungere " + child + " al gruppo " + parent);
      });
    }

    if (settings.dom.buttons.createGroup) {
      initCreationButton(settings.dom.buttons.createGroup, 'CAN_CREATE_CHILDREN', allowableactions, function () {

        var content = $('<div></div>'),
          name = $('<input id="groupName" class="input-xlarge" type="text" />'),
          description = $('<textarea class="input-xlarge" id="groupDescription" type="text" />'),
          zones = $('<div class="btn-group" data-toggle="buttons-checkbox">');

        $.each(cache.zones, function (key, el) {
          zones.append('<button type="button" class="btn" data-id="' + key + '">' + key + '</button>');
        });

        content
          .append('<label for="groupName">nome</label>')
          .append(name)
          .append('<label for="groupDescription">descrizione</label>')
          .append(description)
          .append('<label>zona</label>')
          .append(zones);

        UI.modal("Nuovo gruppo", content, function () {
          var groupName = name.val(),
            groupDescription = description.val(),
            groupZones = $.map(zones.find('.active'), function (el) {
              return $(el).data('id');
            });

          URL.Data.proxy.group({
            type: 'POST',
            data: JSON.stringify({
              'parent_node_ref': selectedFolder,
              'group_name': groupName,
              'display_name': groupDescription,
              'zones': groupZones
            }),
            contentType: 'application/json'
          }).done(function () {
            UI.success('creato il gruppo ' + groupName);
            refresh();
          }).fail(function () {
            UI.error("impossibile creare il gruppo " + groupName);
          });
        });
      });
    }

    if (settings.dom.buttons.createAssociation) {
      initCreationButton(settings.dom.buttons.createAssociation, 'CAN_CREATE_ASSOCIATIONS', allowableactions, function () {
        var widget = Authority.Widget("username", null);
        UI.modal("Seleziona utente", widget, function () {
          var parentGroupName = CNR.nodeRefSelector(selectedFolder).attr('fullname');
          addChild(parentGroupName, widget.data('value'));
        });
      });
    }

  }

  // manage folder-change event in file browser / explorer
  function folderChange(itemFolder, isRoot) {
    var nodeRef = itemFolder.nodeRef, allowableactions = itemFolder.allowableActions;
    isRoot = isRoot || false;
    if (settings.dom.explorerItems) {
      // show explorer items
      settings.dom.explorerItems.removeClass('hide');
    }

    selectedFolder = nodeRef;
    searchFunction(nodeRef, isRoot);
    if (settings.dom.buttons) {
      initCreationButtons(allowableactions);
    }
    if (!isRoot) {
      Tree.openNode(nodeRef);
    }

    // add selected folder to breadcrumb "path"
    path.push(itemFolder);

    // show breadcrumb
    if (settings.dom.breadcrumb) {
      UI.breadCrumb(settings.dom.breadcrumb, path, function () {
        var data = $(this).data(),
          p = data.index,
          selectedItem = path[p];
        path.splice(p, path.length - p);
        folderChange(selectedItem, p === 0);
      });
    }

  }

  // defines the behaviour for the selection of a tree folder
  function treeNodeSelect(data) {
    var nodeRef = data.id, json = data.json, current_path = data.current_path, tail;

    // given the tree as a json object and a folder name, retrieves the nodeRef and the children of the folder
    function getElement(obj, key) {
      var item = $(obj).filter(function (index, item) {
        return item.data === key;
      })[0];
      return {
        nodeRef: item.attr.id,
        children: item.children
      };
    }

    // remove the last element of the tree path (i.e. array of strings)
    current_path.splice(current_path.length - 1, 1);

    // map a tree path to a breadcrumb path, adding metadata (e.g. nodeRef, allowableActions)
    tail = $.map(current_path, function (key) {
      var item = getElement(json, key);
      json = item.children;
      return {
        name: key,
        nodeRef: item.nodeRef,
        allowableActions: CNR.nodeRefSelector(item.nodeRef).attr('allowableactions').split(',') //TODO: hacky
      };
    });
    path = [].concat(path[0]).concat(tail);
    folderChange({name: data.name, nodeRef: nodeRef, allowableActions: data.allowableactions});
  }

  // display the document details as a grid row
  function defaultDisplayGridRow(el) {
    var tdText,
      tdButton,
      isFolder = el.baseTypeId === 'cmis:folder',
      item = $('<a href="#">' + el.name + '</a>'),
      annotation = $('<span class="muted annotation">modificato ' + CNR.Date.format(el.lastModificationDate) + ' da ' +  el.lastModifiedBy + '</span>');

    if (isFolder) {
      item
        .addClass('openFolder')
        .after(annotation)
        .click(function () {
          var nodeRef = el.id;
          folderChange({name: el.name, nodeRef: nodeRef, allowableActions: el.allowableActions});
          return false;
        });
    } else {
      item.attr('href', URL.urls.search.content + '?nodeRef=' + el.id);
      item.after(annotation.prepend(', ').prepend(CNR.fileSize(el.contentStreamLength)));
    }

    tdText = $('<td></td>')
      .append(CNR.mimeTypeIcon(el.contentType, el.name))
      .append(' ')
      .append(item);
    tdButton = $('<td></td>').append(ActionButton.actionButton({
      name: el.name,
      id: el.id,
      nodeRef: el['alfcmis:nodeRef'],
      baseTypeId: el.baseTypeId,
      objectTypeId: el.objectTypeId,
      mimeType: el.contentType,
      allowableActions: el.allowableActions
    }, null, null, null, refresh));
    return $('<tr></tr>')
      .append(tdText)
      .append(tdButton);
  }

  return {
    init: function (opts) {

      var defaults = {
        tree: {
          rootFn: URL.Data.search.rootFolder
        },
        search: {
          display: {
            row : defaultDisplayGridRow
          },
          searchFunction: function (nodeRef) {
            // by default the search function will show the documents inside the selected folder
            search.displayDocs(nodeRef);
          }
        }
      };

      settings = $.extend(true, {}, defaults, opts, true);

      // inizializzazione del pannello di ricerca
      search = new Search({
        dataSource: settings.search.dataSource, //TODO: valutare se e' il caso di uniformare searchFunction
        elements: settings.dom.search,
        display: settings.search.display
      });

      // inizializzazione dell'albero di navigazione per folder
      settings.tree.rootFn({}).done(function (data) {
        var nodeRef = data.id;

        // make first query
        folderChange({
          name: 'Root',
          nodeRef: nodeRef,
          allowableActions: data.allowableActions
        }, true);

        // init Tree only if it's defined
        if (settings.dom.tree) {
          Tree.init({
            customClass: settings.tree.customClass,
            paramName: settings.tree.paramName,
            childrenFn: settings.tree.childrenFn,
            folderId: nodeRef,
            elements: {
              target: settings.dom.tree
            },
            selectNode: treeNodeSelect
          });
        }
      });

    },
    refresh: refresh,
    folderChange: folderChange
  };
});