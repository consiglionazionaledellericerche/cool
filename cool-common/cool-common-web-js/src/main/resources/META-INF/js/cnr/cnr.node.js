// CMS Node management
define(['jquery', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.bulkinfo', 'i18n', 'cnr/cnr.url', 'behave', 'fileupload', 'bootstrap-fileupload'], function ($, CNR, UI, BulkInfo, i18n, URL, Behave) {
  "use strict";

  var defaultObjectTypeDocument = "cmis:document",
    reNodeRef = new RegExp("([a-z]+)\\:\/\/([a-z]+)\/(.*)", 'gi');

  function displayOutcome(data, isDelete) {
    var msg = isDelete ? "file(s) deleted" : (Object.keys(data.attachments).length + ' files ok');
    UI.success(msg);
  }

  // operations on documents: insert, update and delete
  function manageNode(nodeRef, operation, input, rel, forbidArchives, maxUploadSize) {

    var httpMethod = "GET",
      fd = new CNR.FormData();

    fd.data.append("cmis:objectId", nodeRef.split(';')[0]);

    if (operation === "INSERT" || operation === "UPDATE") {

      if (!window.FormData) {
        UI.error("Impossibile eseguire l'operazione");
        return;
      }

      fd.data.append("cmis:objectTypeDocument", defaultObjectTypeDocument);
      fd.data.append("crudStatus", operation);
      if (rel) {
        fd.data.append('cmis:sourceId', rel['cmis:sourceId']);
        fd.data.append('cmis:relObjectTypeId', rel['cmis:relObjectTypeId']);
      }
      if (forbidArchives) {
        fd.data.append('forbidArchives', true);
      }
      $.each(input[0].files || [], function (i, file) {
        fd.data.append('file-' + i, file);
      });
      httpMethod = "POST";
    } else if (operation === "DELETE") {
      httpMethod = "DELETE";
    }
    if (operation === "GET") {
      window.location = URL.urls.search.content + '?nodeRef=' + nodeRef;
    } else {
      return URL.Data.node.node({
        data: fd.getData(),
        contentType: fd.contentType,
        processData: false,
        type: httpMethod,
        placeholder : {
          maxUploadSize : maxUploadSize || false
        }
      });
    }
  }

  function updateMetadata(data, cb) {
    URL.Data.node.metadata({
      type: 'POST',
      data: data,
      success: cb
    });
  }

  function updateMetadataNode(nodeRef, data, success) {
    var metadataToUpdate = {};
    $.map(data, function (metadata) {
      metadataToUpdate[metadata.name] = metadata.value;
    });
    CNR.log(metadataToUpdate);
    URL.Data.proxy.metadataNode({
      placeholder: {
        'store_type' : nodeRef.replace(reNodeRef, '$1'),
        'store_id' : nodeRef.replace(reNodeRef, '$2'),
        'id' : nodeRef.replace(reNodeRef, '$3')
      },
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        "properties" : metadataToUpdate
      }),
      success: success
    });
  }

  // file uploader for Internet Explorer
  function manageIE(selectedFolder, crudStatus, input, setValue, rel, forbidArchives) {
    var myData,
      success = null,
      fd = {
        "cmis:objectTypeDocument": defaultObjectTypeDocument,
        "crudStatus" : crudStatus,
        "cmis:objectId": selectedFolder
      };
    if (rel) {
      fd["cmis:sourceId"] = rel.sourceId;
      fd["cmis:relObjectTypeId"] = rel.objectTypeId;
    }

    if (forbidArchives) {
      fd.forbidArchives = true;
    }

    input
      .fileupload({
        url: URL.urls.node + '.html',
        formData: fd,
        add: function (e, data) {
          myData = data;
        },
        done: function (e, data) {
          var content, j;
          if ($.browser.safari) {
            content = $(data.result).val();
          } else {
            content = $(data.result[0].documentElement).find('textarea').val();
          }
          try {
            j = JSON.parse(content);
            displayOutcome(j, false);
            if (typeof success === 'function') {
              success(j);
            }
          } catch (error) {
            UI.error("Errore nel caricamento del file");
          }
        }
      })
      .bind('fileuploadchange', function (e, data) {
        var path = data.files[0].name;
        if (typeof setValue === 'function') {
          setValue(path);
        }
      });

    return function (nodeRef, status, successFn, relationship) {
      if (nodeRef) {
        if (relationship) {
          fd['cmis:sourceId'] = relationship['cmis:sourceId'];
          fd['cmis:relObjectTypeId'] = relationship['cmis:relObjectTypeId'];
        }
        fd["cmis:objectId"] = nodeRef;
        fd.crudStatus = status || fd.crudStatus;
        myData.formData = fd;
      }
      success = successFn;
      myData.submit();
      return false;
    };
  }



  /**
   * Create a new input file ("widget") powered by fileupload
   *
   * manages the fallback operations (e.g. FormData) in InternetExplorer
   * write the file name in .data('value')
   *
   */
  function inputWidget(folder, crudStatus, rel, forbidArchives, maxUploadSize) {

    var container = $('<div class="fileupload fileupload-new" data-provides="fileupload"></div>'),
      input = $('<div class="input-append"></div>'),
      btn = $('<span class="btn btn-file"></span>'),
      inputFile = $('<input type="file" />'),
      submitFn,
      isExplorer = window.FormData && window.FileReader ? false : true;

    btn
      .append('<span class="fileupload-new">Aggiungi allegato</span>')
      .append('<span class="fileupload-exists">Cambia</span>')
      .append(inputFile);

    input
      .append('<div class="uneditable-input input-xlarge"><i class="icon-file fileupload-exists"></i><span class="fileupload-preview"></span></div>')
      .append(btn)
      .appendTo(container);

    // set widget 'value'
    function setValue(value) {
      container.data('value', value);
    }

    setValue(null);

    if (isExplorer) {
      inputFile.attr('name', 'file-0');
      submitFn = manageIE(folder, crudStatus, inputFile, setValue, rel, forbidArchives, maxUploadSize);
    } else {
      input.append('<a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Rimuovi</a>'); //remove cannot be use in IE-compatible mode
      submitFn = function (nodeRef, status, success, relationship) {
        var xhr = manageNode(nodeRef || folder, status || crudStatus, inputFile, relationship || rel, forbidArchives, maxUploadSize);
        xhr.done(function (data) {
          if (success) {
            success(data);
          }
        });
        return xhr;
      };
      inputFile.on('change', function (e) {
        var path = $(e.target).val();
        setValue(path);
      });
    }

    return {
      item: container,
      fn: submitFn
    };
  }

  /**
   * Submission of a new document.
   *
   * opens a modal window containing the input form for the specified object type and an input file
   * create/update the document
   *
   * @param {string} nodeRef the parent folder OR the document to update
   * @param {string} objectType the type of the node to create (e.g. 'cmis:document')
   * @param {string} crudStatus the type of the operation to perform (i.e. 'UPDATE' or 'INSERT')
   * @param {boolean} requiresFile if the submission require (at least) one file, true by default
   * @param {boolean} showFile if the submission show an input file
   * @param [array] externalData add to submission
   * @param {boolean} multiple if the submission allow multiple files
   *
   */
  function submission(opts) {

    opts.objectType = opts.objectType || 'cmis:document';

    var content = $("<div></div>").addClass('modal-inner-fix'),
      bulkinfo,
      fileInputs = [],
      modal,
      isInsert = opts.crudStatus === 'INSERT',
      addFileUploadInput,
      regex = /^.*:([^:]*)/gi;


    function addPlusButton(element) {
      var btn = $('<button class="btn">+</button>').click(addFileUploadInput);
      btn.appendTo(element.find('.input-append'));
    }

    addFileUploadInput = function () {
      var w = inputWidget(null, "UPDATE", (opts.input ? opts.input.rel : undefined), opts.forbidArchives, opts.maxUploadSize);
      fileInputs.push(w);
      addPlusButton(w.item);
      content.append(w.item);
    };

    if (opts.showFile !== false) {
      fileInputs.push(inputWidget(null, "UPDATE", undefined, opts.forbidArchives, opts.maxUploadSize));
      if (opts.multiple) {
        addPlusButton(fileInputs[0].item);
      }
      content.append(fileInputs[0].item);
    }

    bulkinfo = new BulkInfo({
      target: content,
      path: opts.objectType,
      objectId: isInsert ? null : opts.nodeRef,
      callback: {
        afterCreateForm: function () {
          modal = UI.modal(opts.modalTitle || (isInsert ? 'Inserimento allegato' : 'Aggiornamento allegato'), content, function () {
            if (!bulkinfo.validate()) {
              UI.alert("alcuni campi non sono corretti");
              return false;
            }

            function filterFileInputs() {
              var filtered = $(fileInputs).filter(function (index, el) {
                return el.item.find('span.fileupload-preview').text();
              });
              return $.makeArray(filtered);
            }



            var data = bulkinfo.getData(),
              filteredFileInputs = filterFileInputs(),
              inputName,
              fileName,
              fileinput = filteredFileInputs[0],
              displayedFileName = fileinput ? fileinput.item.find('span.fileupload-preview').text() : null;

            if (opts.externalData) {
              $.each(opts.externalData, function (i, exData) {
                data.push(exData);
              });
            }

            if (isInsert) {
              data.push({name: 'cmis:parentId', value: opts.nodeRef});
              data.push({name: 'cmis:objectTypeId', value: opts.objectType});

              inputName = $(data).filter(function (index, el) {
                return el.name === 'cmis:name';
              })[0];

              data.splice(data.indexOf(inputName), 1);

              if (inputName && inputName.value && !opts.multiple) {
                fileName = inputName.value;
              } else if (displayedFileName && !opts.multiple) {
                fileName = displayedFileName;
              } else {


                if (regex.test(opts.objectType)) {
                  fileName = opts.objectType.replace(regex, "$1");
                } else {
                  fileName = 'doc';
                }
                fileName += '_' + (new Date().getTime());
              }

              data.push({name: 'cmis:name', value: fileName});
            } else {
              // update object with 'cmis:objectId' === nodeRef
              data.push({
                name: 'cmis:objectId',
                value: opts.nodeRef.split(';')[0]
              });
            }

            if (opts.requiresFile !== false && !fileinput) {
              UI.alert("inserire un allegato!");
              return false;
            } else {
              updateMetadata(data, function (data) {
                if (fileinput && fileinput.item.data('value')) {
                  var close = UI.progress(),
                    xhrs = $.map(filteredFileInputs, function (f) {
                      if (opts.multiple) {
                        return f.fn(opts.nodeRef, "INSERT", function (attachmentsData) {
                          close();
                          if (typeof opts.success === 'function') {
                            opts.success(attachmentsData, data);
                          }
                        }, {
                          "cmis:sourceId" : data['cmis:objectId'],
                          "cmis:relObjectTypeId" : opts.input.rel['cmis:relObjectTypeId']
                        });
                      } else {
                        return f.fn(data['cmis:objectId'], null, function (attachmentsData) {
                          close();
                          if (typeof opts.success === 'function') {
                            opts.success(attachmentsData, data);
                          }
                        });
                      }
                    });
                  $.when.apply(this, xhrs)
                    .done(function () {
                      if (xhrs && xhrs[0]) {
                        close();
                        UI.success((filteredFileInputs.length === 1 ? 'allegato inserito' : 'allegati inseriti') + ' correttamente');
                      }
                    })
                    .fail(function (xhr) {
                      close();
                      if (typeof opts.success === 'function') {
                        opts.success();
                      }
                    });
                } else {
                  UI.success('Dato inserito correttamente.');
                  opts.success(undefined, data);
                }
              });
            }
          }, undefined, opts.bigmodal);
          if (opts.callbackModal) {
            opts.callbackModal(modal);
          }
        }
      }
    });

    bulkinfo.render();
  }

  /**
   *
   *  Update the content of a given node using an editor (Behave.js) supporting IDE-like features such as parenthesis autocompletion, Auto Indent
   *
   */
  function updateContentEditor(content, mimeType, nodeRef) {
    var textarea = $('<textarea class="input-block-level" rows="15"></textarea>').val(content), editor;

    editor = new Behave({
      textarea: textarea[0],
      tabSize: 2,
      autoIndent: true
    });
    UI.modal('Aggiornamento di ' + name, textarea, function () {
      var file = new window.Blob([textarea.val()], {type: mimeType}),
        input = [{
          files: [file]
        }];

      manageNode(nodeRef, "UPDATE", input);
    });
  }

  return {
    updateMetadata: updateMetadata,
    updateMetadataNode: updateMetadataNode,
    // display object metadata using bulkinfo
    displayMetadata : function (bulkInfo, nodeRef, isCmis, callback) {
      if (!nodeRef) {
        UI.alert("No information found");
      } else {
        var f = isCmis ? URL.Data.node.node : URL.Data.proxy.metadata;
        f({
          data: {
            "nodeRef" : nodeRef,
            "shortQNames" : true
          }
        }).done(function (metadata) {
          new BulkInfo({
            handlebarsId: 'zebra',
            path: bulkInfo,
            metadata: isCmis ? metadata : metadata.properties
          }).handlebars().done(function (html) {
            var content = $('<div></div>').addClass('modal-inner-fix').append(html),
              title = i18n.prop("modal.title.view." + bulkInfo, 'Propriet&agrave;');
            if (callback) {
              callback(content);
            }
            UI.modal(title, content);
          });
        });
      }
    },
    updateContentEditor: updateContentEditor,
    submission: submission,
    inputWidget: inputWidget,
    remove: function (nodeRef, refreshFn, showMessage) {
      manageNode(nodeRef, "DELETE").done(function (data) {
        if (refreshFn) {
          if (showMessage !== false) {
            displayOutcome(data, true);
          }
          refreshFn(data);
        }
      });
    }
  };
});
