/*global params*/
define(['jquery', 'i18n', 'header', 'cnr/cnr.search',
  'cnr/cnr.bulkinfo', 'cnr/cnr.ui',
  'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.ace', 'json!cache'
  ], function ($, i18n, header, Search, BulkInfo, UI, common, jconon, URL, Call, Ace, cache) {
  "use strict";
  var callTypes = [], rootTypeId = 'F:jconon_call:folder',
    rootQueryTypeId = 'jconon_call:folder',
    ul = $('.cnraffix'),
    aAllCall = $('<a href="#items"><i class="icon-chevron-right"></i>' + i18n.prop(rootTypeId, 'Tutti i Bandi') + '</a>'),
    liAllCall = $('<li class="active"></li>').append(aAllCall).appendTo(ul),
    elements = {
      table: $('#items'),
      pagination: $('#itemsPagination'),
      orderBy: $('#orderBy'),
      label: $('#emptyResultset')
    },
    search,
    bulkInfo;
  $.each(cache.jsonlistCallType, function (index, el) {
    callTypes.push({
      "key" : el.id,
      "label" : el.id,
      "defaultLabel" : el.title
    });
  });
  function manageFilterClick() {
    $('#applyFilter').on('click', function () {
      Call.filter(bulkInfo, search);
    });
    $('#filters-attivi_scaduti').closest('.widget').on('setData', function (event, key, value) {
      Call.filter(bulkInfo, search, null, null, null, value);
    });
    $('#resetFilter').on('click', function () {
      $('#F_jconon_call_folder input').val('');
      $('#F_jconon_call_folder .widget').data('value', '');
      $('#filters-attivi_scaduti').data('value', 'attivi');
      Call.filter(bulkInfo, search);
    });
  }

  function displayCall(typeId, queryTypeId) {
    URL.Data.bulkInfo({
      placeholder: {
        path: typeId,
        kind: 'column',
        name: 'home'
      },
      data: {
        guest : true
      },
      success: function (data) {
        var columns = [],
          sortFields = {
            nome: false
          };
        $.map(data[data.columnSets[0]], function (el) {
          if (el.inSelect !== false) {
            columns.push(el.property);
          }
        });
        $.each(data[data.columnSets[0]], function (index, el) {
          if (el['class'] && el['class'].split(' ').indexOf('sort') >= 0) {
            sortFields[i18n.prop(el.label, el.label)] = el.property;
          }
        });
        search = new Search({
          elements: elements,
          maxItems: 10,
          fetchCmisObject: true,
          type: jconon.joinQuery(queryTypeId, data.aspect, ['P:jconon_call:aspect_macro_call']),
          columns: columns,
          fields: sortFields,
          mapping: function (mapping, doc) {
            $.each(data[data.columnSets[0]], function (index, el) {
              mapping[el.name] = doc[el.property] !== undefined ? doc[el.property] : null;
            });
            mapping.aspect = doc.aspect !== undefined ? doc.aspect : null;
            return mapping;
          },
          display: {
            resultSet: function (resultSet, target) {
              Call.displayRow(bulkInfo, search, typeId, rootTypeId, resultSet, target);
            }
          }
        });
        Call.filter(bulkInfo, search);
      }
    });
  }

  function changeActiveState(btn) {
    btn.parents('ul').find('.active').removeClass('active');
    btn.parent('li').addClass('active');
  }
  aAllCall.click(function (eventObject) {
    changeActiveState($(eventObject.target));
    displayCall(rootTypeId, rootQueryTypeId);
  });

  bulkInfo = new BulkInfo({
    target: $('#criteria'),
    formclass: 'form-horizontal jconon',
    path: rootTypeId,
    name: 'all-filters',
    callback : {
      beforeCreateElement: function (item) {
        if (item.name === 'filters-codice') {
          item.val = params.query;
        } else if (item.name === 'call-type') {
          item.jsonlist = callTypes;
        }
      },
      afterCreateForm: function (form) {
        manageFilterClick();
        displayCall(rootTypeId, rootQueryTypeId);
      }
    }
  });
  bulkInfo.render();

  $.each(cache.jsonlistCallType, function (index, el) {
    var li = $('<li></li>'),
      a = $('<a href="#items"><i class="icon-chevron-right"></i>' + i18n.prop(el.id, el.title) + '</a>').click(function (eventObject) {
        changeActiveState($(eventObject.target));
        displayCall(el.id, el.queryName);
      });
    li.append(a).appendTo(ul);
  });
});