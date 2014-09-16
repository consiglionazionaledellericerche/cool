define(['jquery', 'cnr/cnr.advancedsearch', 'cnr/cnr.url', 'cnr/cnr.ui.tree', 'cnr/cnr.ui.select', 'cnr/cnr.ui', 'cnr/cnr.bulkinfo'], function ($, AdvancedSearch, URL, Tree, Select, UI, BulkInfo) {
  "use strict";

  var elementIdTree,
    queryNameTree,
    select,
    queryStringObj = URL.querystring.from,
    isExternalCall = queryStringObj.idTree && queryStringObj.queryNameTree,
    handlebarsMap = {
      'F:jconon_call:folder': 'zebra',
      'F:jconon_application:folder': 'zebra',
      'D:jconon_attachment:document': 'zebra'
    },
    options = {
      defaultSearchSettings: {
        elements: {
          table: $('#items'),
          pagination: $('#itemsPagination'),
          orderBy: $('#orderBy'),
          label: $('#emptyResultset')
        },
        display: {
          resultSet: function (resultSet, target) {
            new BulkInfo({
              handlebarsId: handlebarsMap[elementIdTree],
              path: elementIdTree,
              metadata: resultSet
            }).handlebars().done(function (html) {
              var tableBody = $('#items').find('tbody');
              tableBody.empty().append(html);
            });
          }
        }
      }
    },
    search = new AdvancedSearch(options);

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
            queryNameTree = node[0].attributes.queryname.value;
            elementIdTree = el.id;

            var bulkinfoOptions = {
                target: $('.bulkinfo'),
                formclass: 'form-horizontal',
                kind: 'find',
                name: node[0].attributes.freesearchsetname.value,
                path: el.id
              },
              bulkinfo = new BulkInfo(bulkinfoOptions);

            search.setBulkinfo(bulkinfo);
            bulkinfo.render();

          },
          elements: {
            target: $('.tree')
          }
        }
      });
    }
  );

  // Lista aspect
  URL.Data.typesTree({
    traditional: true,
    data : {
      "seeds" : ["P:cmisext:aspects"]
    }
  }).done(
    function (data) {
      var aspects = search.toAspectsList(data);
      select = Select.Widget(null, 'Aspects:', {
        placeholder : 'Selezionare aspect',
        'class': 'input-xlarge',
        multiple: true,
        jsonlist: aspects
      }).appendTo($('.aspect-list'));

      select
        .addClass('novalidate')
        .on('setData', aspects, function (event, key, value, initial) {

          $('.bulkinfoAspect').empty();

          try {
            search.buildAspectsBulkInfo(value, event.data);
          } catch (exception) {
            UI.alert(exception.message);
          }
        });

      select.promise().done(function () {
        if (isExternalCall) {
          select.hide();
        }
      });
    }
  );

  $('#applyFilter').click(function () {
    try {
      search.executeQuery(queryNameTree);
    } catch (exception) {
      UI.alert(exception);
    }
  });

  $('#resetFilter').click(function () {
    location.reload(false);
  });

  if (isExternalCall) {
    elementIdTree = decodeURIComponent(queryStringObj.idTree);
    queryNameTree = decodeURIComponent(queryStringObj.queryNameTree);
    search.queryStringRender($('.bulkinfo'), elementIdTree, queryStringObj.aspects);
  }

});