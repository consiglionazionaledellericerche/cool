define(['jquery', 'i18n', 'header', 'cnr/cnr.search',
  'cnr/cnr.bulkinfo', 'cnr/cnr.ui',
  'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.ace', 'modernizr', 'json!cache'
  ], function ($, i18n, header, Search, BulkInfo, UI, common, jconon, URL, Call, Ace, Modernizr, cache) {
  "use strict";
  var rootTypeId = 'F:jconon_call:folder',
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
    bulkInfo,
    extra = 'jconon_call:has_macro_call',
    msg;

  function manageFilterClick() {

    $('#filters-attivi_scaduti').closest('.widget').on('setData', function (event, key, value) {
      Call.filter(bulkInfo, search, 'equals', extra, false, value);
    });
  }

  $(document.body).on('click', '.code', function () {
    var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
    UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.call'], data);
  });

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
            nome: null,
            'data di creazione': null
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
          orderBy: {
            field: "jconon_call:codice",
            asc: true
          },
          cache: 60 * 15, // 15 minutes
          mapping: function (mapping, doc) {
            $.each(data[data.columnSets[0]], function (index, el) {
              var pointIndex = el.property.indexOf('.'),
                property = pointIndex !== -1 ? el.property.substring(pointIndex + 1) : el.property;
              mapping[el.name] = doc[property] !== undefined ? doc[property] : null;
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
        Call.filter(bulkInfo, search, 'equals', extra, false);
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
    name: 'filters',
    callback : {
      afterCreateForm: function (form) {
        $('#filters').find('.control-group').hide();
        $('#filters').find('#filters-attivi_scaduti').closest('.control-group').show();
        manageFilterClick();
        displayCall(rootTypeId, rootQueryTypeId);
      }
    }
  });
  bulkInfo.render();

  $.each(cache.jsonlistCallType.sort(function (a, b) {
    return i18n.prop(a.id, a.title) > i18n.prop(b.id, b.title);
  }), function (index, el) {
    var li = $('<li></li>'),
      a = $('<a href="#items"><i class="icon-chevron-right"></i>' + i18n.prop(el.id, el.title) + '</a>').click(function (eventObject) {
        changeActiveState($(eventObject.target));
        displayCall(el.id, el.queryName);
      });
    li.append(a).appendTo(ul);
  });


  if (!(Modernizr.fileinput && Modernizr.filereader)) {
    msg = "<p>La versione del browser che sta utilizzando non e' compatibile con le tecnologie implementate nel sito Selezioni online. Deve pertanto <strong>aggiornare il suo browser</strong>&nbsp; Qui di seguito sono presentate alcune alternative, tutte molto valide:</p>";
    msg += "<ul><li><strong>Mozilla Firefox</strong>&nbsp;( versione 19 o superiore);</li><li><strong>Google Chrome</strong>&nbsp;(versione 25 o superiore);</li><li><strong>Safari</strong>&nbsp;(versione 6 o superiore).</li></ul>";
    $('.list-main-call').prepend('<div class="alert alert-error">' + "Il browser che si sta utilizzando è obsoleto. Si consiglia di aggiornarlo all’ultima versione per un corretto funzionamento della procedura." + '</div>');
    UI.alert(msg);
  }

  if (cache.debug) {
    $('.jumbotron > h1').after($('<h2>').text('TEST Vers: ' + common.version));
  }

});
