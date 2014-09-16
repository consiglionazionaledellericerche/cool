define(['jquery', 'header', 'cnr/cnr.url', 'i18n', 'cnr/cnr.ui.tree', 'cnr/cnr.bulkinfo', 'cnr/cnr.ui.widgets', 'cnr/cnr.search', 'cnr/cnr.criteria', 'cnr/cnr.ui.select', 'cnr/cnr.ui', 'cnr/cnr', 'ace', 'xsd2json', 'jquery.xmleditor', 'jquery-ui', 'cycle', 'vkbeautify', "ace/range"], function ($, header, URL, i18n, Tree, BulkInfo, Widgets, Search, Criteria, Select, UI, CNR, ACE) {
  "use strict";

  var bulkinfo,
    search,
    queryNameTree,
    prefixTree = 'a',
    aspectList = [],
    elementIdTree,
    defaultHandlebars = 'zebra',
    handlebarsMap = {
      'F:jconon_call:folder' : 'zebra',
      'F:jconon_application:folder': 'zebra',
      'D:jconon_attachment:document': 'zebra'
    },
    defaultSearchSettings = {
      elements: {
        table: $('#items'),
        pagination: $('#itemsPagination'),
        orderBy: $('#orderBy'),
        label: $('#emptyResultset')
      }
    },
    kindName = {
      'form': 'forms',
      'find': 'freeSearchSet',
      'column': 'columnSets'
    },
    settings = {
      target: $('.bulkinfo'),
      formclass: 'form-horizontal',
      kind: $('#select-kind').val(),
      name: "",
      path: ""
    },
    currentBulk = {},
    editor;

  function loadBulkInfo(content, type, name) {
    var kind, requestFind, requestForm, requestColumn;

    currentBulk = {};

    $('#select-kind').val();
    bulkinfo = new BulkInfo(settings);

    requestFind  = URL.Data.bulkInfo({
      placeholder: {
        path: type,
        kind: "find",
        name: "all"
      },
      data: {
        "cmis:objectId": null,
        "guest": true
      }
    });
    requestForm  = URL.Data.bulkInfo({
      placeholder: {
        path: type,
        kind: "form",
        name: "all"
      },
      data: {
        "cmis:objectId": null,
        "guest": true
      }
    });
    requestColumn  = URL.Data.bulkInfo({
      placeholder: {
        path: type,
        kind: "column",
        name: "all"
      },
      data: {
        "cmis:objectId": null,
        "guest": true
      }
    });

    $.when(requestFind, requestForm, requestColumn).then(function (data1, data2, data3) {
      var options, selectFind, selectForm, selectColumn;
//      
//      CNR.log(data1[0].freeSearchSets);
//      CNR.log(data2[0].forms);
//      CNR.log(data3[0].columnSets);
//      
//      CNR.log(kindName[$('#select-kind').val()]);
//      CNR.log(data[kindName[$('#select-kind').val()]]);

      currentBulk.find = data1[0];
      currentBulk.form = data2[0];
      currentBulk.column = data3[0];

      options = data1[0].freeSearchSets;
      selectFind = $("#select-find").empty();
      selectFind.append($("<option />"));
      $.each(options, function () {
        selectFind.append($("<option />").val(this).text(this));
      });

      options = data2[0].forms;
      selectForm = $("#select-form").empty();
      selectForm.append($("<option />"));
      $.each(options, function () {
        selectForm.append($("<option />").val(this).text(this));
      });

      options = data3[0].columnSets;
      selectColumn = $("#select-column").empty();
      selectColumn.append($("<option />"));
      $.each(options, function () {
        selectColumn.append($("<option />").val(this).text(this));
      });
    });
  }

  function reloadBulkInfo() {
    var form, kind = $("#select-kind").val(),
      instance = $('#select-' + kind).val(),
      currentJson = editor.getXMLString();

    $('.bulkinfo').empty();
    currentJson = JSON.parse(currentJson);

    settings.kind = '';
    //settings.
    bulkinfo = new BulkInfo(settings);


    CNR.log(currentJson);
    CNR.log(bulkinfo);

    currentBulk[kind][instance] = currentJson;
    form = $('<form></form>')
      .addClass(settings.formclass)
      .attr('id', currentJson.id);
    settings.target.prepend(form);
    bulkinfo.renderView("prova", form, currentJson, {});
  }

  function showSelectKind(el) {
    $('#select-find').hide();
    $('#select-form').hide();
    $('#select-column').hide();

    $('#select-' + el.value).show();
  }

  function loadBulkInfoIntoEditor(el) {
    var kind = $("#select-kind").val(),
      instance = $('#select-' + kind).val();
    CNR.log(kind + " " + instance);
    CNR.log(currentBulk[kind][instance]);

    editor.setValue(JSON.stringify(currentBulk[kind][instance], null, "\t"));
    editor.gotoLine(0);
  }

  function setup() {
//    editor = ACE.edit("bulkinfoEditArea");
//    editor.getSession().setMode("ace/mode/json");
//    editor.getSession().setTabSize(2);
//    editor.getSession().on(reloadBulkInfo); 
//    CNR.log(editor);

    var extractor, schema, editorEl;
    extractor = new Xsd2Json("/cool-jconon/res/model/BulkInfo.xsd", {});
    schema = extractor.getSchema()();

    editorEl = $("#bulkinfoEditArea").xmlEditor({
      schema: schema,
      loadSchemaAsychronously: false,
      ajaxOptions: {
        xmlRetrievalPath: "/cool-jconon/res/bulkInfo/F_jconon_call_folder.xml"
        //xmlUploadPath: '/cool-jconon/rest/modelDesigner/createModel'
        //xmlUploadFunction: customUpdateFunction
      }
    });

    editor = editorEl.data('xmlEditor');

    $("#select-kind").change(function () {
      showSelectKind(this);
    });
    $("#select-find,#select-form,#select-column").change(function () {
      loadBulkInfoIntoEditor(this);
    });
    $("#ricarica-button").click(reloadBulkInfo);

    URL.Data.typesTree({
      traditional: true,
      data : {
        "seeds" : [
          "F:jconon_call:folder",
          "F:jconon_application:folder",
          "D:jconon_attachment:document"
        ]
      }
    }).done(
      function (data) {
        Tree.Widget('idTree', '', {

          settings : {
            dataSource: function (node, callback) {
              callback(data);
            },
            selectNode: function (el, node) {
              $('.bulkinfo').empty();
              loadBulkInfo($('.bulkinfo'), el.id, 'all');
              queryNameTree = node[0].attributes.queryname.value;
              // FIXME: recuperare da node il nome del freeSearchSet ???
              elementIdTree = el.id;
            },
            elements: {
              target: $('.tree')
            }
          }

        });
      }
    );

  }

  setup();
});