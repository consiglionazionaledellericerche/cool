define(['jquery', 'header', 'json!common', 'cnr/cnr.bulkinfo', 'cnr/cnr.search', 'cnr/cnr.url', 'i18n', 'cnr/cnr.ui', 'cnr/cnr.actionbutton', 'cnr/cnr.jconon', 'handlebars', 'cnr/cnr', 'moment', 'cnr/cnr.application', 'cnr/cnr.criteria', 'cnr/cnr.ace', 'cnr/cnr.call', 'cnr/cnr.node'], function ($, header, common, BulkInfo, Search, URL, i18n, UI, ActionButton, jconon, Handlebars, CNR, moment, Application, Criteria, Ace, Call, Node) {
  "use strict";

  var search,
    rootTypeId = 'F:jconon_application:folder',
    typeId = 'F:jconon_call:folder',
    myType = 'jconon_application:folder',
    elements = {
      table: $('#items'),
      pagination: $('#itemsPagination'),
      orderBy: $('#orderBy'),
      label: $('#emptyResultset'),
      total: $('#total'),
      //icone per l'export dei dati
      exportPanel:  $('<th><div id="export-div">' +
                      '<a id="export-xls" title="Esporta dati in Excel" class="btn btn-mini"><i class="icon-table"></i> </a>' +
                      '</div> </th> <th><div id="download-div"> </div> </th>').appendTo($('#orderBy'))
    },
    bulkInfo,
    criteria = $('#criteria'),
    callId = URL.querystring.from['cmis:objectId'];


  function displayAttachments(nodeRef, type, displayFn, i18nModal) {
    var content = $('<div></div>').addClass('modal-inner-fix');
    jconon.findAllegati(nodeRef, content, type, true, displayFn);
    UI.modal(i18n[i18nModal || 'actions.attachments'], content, undefined, undefined, true);
  }

  function manageUrlParams() {

    if (callId) {

      URL.Data.node.node({
        data: {
          nodeRef : callId
        },
        success: function (data) {
          $('#items h3').text(data['cmis:name']);
        },
        error: function (jqXHR, textStatus, errorThrown) {
          CNR.log(jqXHR, textStatus, errorThrown);
        }
      });
    }
  }

  manageUrlParams();

  // filter ajax resultSet according to the Criteria form
  function filterFn(data) {

    var filtered = $.grep(data.items, function (el) {
      var callCode = bulkInfo.getDataValueById('filters-codice'),
        callFromDate = bulkInfo.getDataValueById('filters-da_data'),
        callToDate = bulkInfo.getDataValueById('filters-a_data'),
        callStatus = callId ? 'tutti' : bulkInfo.getDataValueById('filters-attivi_scaduti'),
        call = el.relationships.parent ? el.relationships.parent[0] : {},
        now = new Date(common.now),
        isActive = call['jconon_call:data_fine_invio_domande'] === "" ||
          (new Date(call['jconon_call:data_inizio_invio_domande']) < now && new Date(call['jconon_call:data_fine_invio_domande']) > now);

      if (callCode) {
        if (!new RegExp(callCode, "gi").test(call['jconon_call:codice'])) {
          return false;
        }
      }

      if (callFromDate) {
        if (new Date(call['jconon_call:data_fine_invio_domande']) < new Date(callFromDate)) {
          return false;
        }
      }

      if (callToDate) {
        if (new Date(call['jconon_call:data_fine_invio_domande']) > new Date(callToDate)) {
          return false;
        }
      }

      if (callStatus) {
        if (callStatus === 'attivi' && !isActive) {
          return false;
        }
        if (callStatus === 'scaduti' && isActive) {
          return false;
        }
      }
      return true;
    });
    data.items = filtered;
    data.totalNumItems = filtered.length;
    data.hasMoreItems = filtered.length > data.maxItemsPerPage;

    return data;
  }

  function filter() {
    search.execute();
  }

  Handlebars.registerHelper('applicationStatus', function declare(code, dataInvioDomanda, dataUltimaModifica, dataScadenza) {
    var dateFormat = "DD/MM/YYYY HH:mm:ss",
      isTemp = (code === 'P' || code === 'I'),
      msg = i18n['label.application.stato.' + (code === 'I' ? 'P' : code)],
      item = $('<label class="label"></label>')
        .addClass(isTemp ? 'label-warning' : 'label-success')
        .addClass(dataScadenza !== "" && (moment().diff(dataScadenza, 'days') > -7) ? 'animated flash' : '')
        .append(msg)
        .append(isTemp ? (' - modificata il ' + CNR.Date.format(dataUltimaModifica, '-', dateFormat)) : (' il ' + CNR.Date.format(dataInvioDomanda, '-', dateFormat)));
    return $('<div>').append(item).html();
  });

  Handlebars.registerHelper('esclusioneRinuncia', function esclusioneRinunciaFn(esclusioneRinuncia, statoDomanda, dataDomanda) {

    var m = {
      'E': 'Esclusa',
      'R': 'Ritirata'
    }, a;

    if (statoDomanda === 'C' && dataDomanda && m[esclusioneRinuncia]) {
      a = $('<span class="label label-important"></span>').append(m[esclusioneRinuncia]);
    }

    return $('<div>').append(a).html();
  });

  Handlebars.registerHelper('scadenza', function scadenza(date) {
    var isExpired = CNR.Date.isPast(new Date(date)),
      a = $('<span>' + i18n[isExpired ? "label.th.jconon_bando_data_scadenza_expired" : "label.th.jconon_bando_data_scadenza"] + '</span>')
        .append(' ')
        .addClass(isExpired ? 'text-error' : '')
        .append(CNR.Date.format(date, "-", "DD/MM/YYYY HH:mm:ss"));
    return $('<div>').append(a).html();
  });

  search = new Search({
    elements: elements,
    columns: ['jconon_application:stato_domanda', 'jconon_application:nome', 'jconon_application:cognome', 'jconon_application:data_domanda', 'jconon_application:codice_fiscale', 'jconon_application:data_nascita', 'jconon_application:esclusione_rinuncia', 'jconon_application:user'],
    fields: {
      'nome': null,
      'data di creazione': null,
      'cognome': 'jconon_application:cognome',
      'stato domanda': 'jconon_application:stato_domanda',
      'data invio': 'jconon_application:data_domanda',
      'esclusioneRinuncia':  'jconon_application:esclusione_rinuncia'
    },
    orderBy: {
      field: 'jconon_application:cognome',
      asc: true
    },
    type: myType,
    maxItems: callId ? undefined : 100,
    dataSource: function (page, settings, getUrlParams) {
      var deferred,
        baseCriteria = new Criteria().not(new Criteria().equals('jconon_application:stato_domanda', 'I').build()),
        criteria = new Criteria(),
        applicationStatus = bulkInfo.getDataValueById('filters-provvisorie_inviate'),
        user = bulkInfo.getDataValueById('user'),
        url;

      if (applicationStatus && applicationStatus !== 'tutte') {
        baseCriteria.and(new Criteria().equals('jconon_application:stato_domanda', applicationStatus).build());
      }

      if (callId) {
        criteria.inTree(callId);

        if (user) {
          criteria.and(new Criteria().equals('jconon_application:user', user).build());
        }
      } else {
        criteria.equals('jconon_application:user', common.User.id);
      }
      settings.lastCriteria = criteria.and(baseCriteria.build()).build();

      url = URL.template(URL.urls.search.queryExcel, $.extend({}, getUrlParams(page), {
        relationship: 'parent',
        fetchCmisObject: true,
        maxItems: 10000,
        exportData: true,
        mimeType: 'application/vnd.ms-excel;charset=UTF-8'
      }));
      $('#export-xls').attr('href', url);

      deferred = URL.Data.search.query({
        queue: true,
        data: $.extend({}, getUrlParams(page), {
          relationship: 'parent'
        })
      });

      deferred.done(function (data) {
        if (elements.total) {
          elements.total.text(data.totalNumItems + ' elementi trovati in totale');
        }
      });

      if (!callId) {
        deferred = deferred.pipe(filterFn);
      }

      return deferred;
    },
    display: {
      resultSet: function (resultSet, target) {
        var xhr = new BulkInfo({
          target: $('<tbody>').appendTo(target),
          handlebarsId: 'application-main-results',
          path: typeId,
          metadata: resultSet,
          handlebarsSettings: {
            call_type: typeId === rootTypeId ? true : false,
            callId: callId
          }
        }).handlebars();

        xhr.done(function () {

          target
            .off('click')
            .on('click', '.requirements', function () {
              var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
              UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.th.jconon_bando_elenco_titoli_studio'], data);
            })
            .on('click', '.code', function () {
              var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
              UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.call'], data);
            })
            .on('click', '.user', function (event) {
              var authority = $(event.target).attr('data-user');
              Ace.showMetadata(authority);
            });

          var rows = target.find('tbody tr');
          $.each(resultSet, function (index, el) {
            var target = $(rows.get(index)).find('td:last'),
              callData = el.relationships.parent[0],
              bandoInCorso = (callData['jconon_call:data_fine_invio_domande'] === "" ||
                new Date(callData['jconon_call:data_fine_invio_domande']) > new Date(common.now)),
              displayActionButton = true,
              defaultChoice,
              customButtons = {
                select: false,
                permissions: false,
                remove: false,
                workflow: false,
                copy: false,
                cut: false,
                duplicate: function () {
                  var call = el.relationships.parent[0];
                  Call.pasteApplication(el.id, call['cmis:objectTypeId'], call['cmis:objectId'], call['jconon_call:has_macro_call']);
                },
                print: function () {
                  Application.print(el.id, el['jconon_application:stato_domanda'], bandoInCorso, el['jconon_application:data_domanda']);
                }
              };

            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabTitoli') >= 0) {
              customButtons.attachments = function () {
                displayAttachments(el.id, 'jconon_attachment:generic_document', Application.displayTitoli);
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabCurriculum') >= 0) {
              customButtons.curriculum = function () {
                //Curriculum
                displayAttachments(el.id, 'jconon_attachment:cv_element', Application.displayCurriculum, 'actions.curriculum');
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabElencoProdotti') >= 0) {
              customButtons.productList = function () {
                //Elenco Prodotti
                displayAttachments(el.id, 'cvpeople:noSelectedProduct', Application.displayProdotti, 'actions.productList');
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabProdottiScelti') >= 0) {
              customButtons.productSelected = function () {
                //Prodotti Scelti
                displayAttachments(el.id, 'cvpeople:selectedProduct', Application.displayProdottiScelti, 'actions.productSelected');
              };
            }
            //  Modifica
            customButtons.edit = function () {
              window.location = jconon.URL.application.manage + '?callId=' + el.relationships.parent[0]['cmis:objectId'] + '&applicationId=' + el['cmis:objectId'];
            };
            if (el['jconon_application:stato_domanda'] === 'P') {
              // provvisoria
              if (bandoInCorso) {
                if (common.User.isAdmin || common.User.id === el['jconon_application:user']) {
                  defaultChoice = 'edit';
                } else {
                  customButtons.edit = false;
                  defaultChoice = 'print';
                }
              } else {
                //  label Scaduto
                $.each(customButtons, function (index, el) {
                  if (index !== "print" && index !== "duplicate") {
                    customButtons[index] = false;
                  }
                });
                customButtons.edit = false;
                displayActionButton = true;
              }
            } else if (el['jconon_application:stato_domanda'] === 'C') {
              // definitiva è editbile per ora solo da amministratori, poi sarà il RDP
              if (!common.User.isAdmin) {
                customButtons.edit = false;
              }
              defaultChoice = 'print';

              if (bandoInCorso) {
                customButtons.reopen = function () {
                  Application.reopen(el, function () {
                    window.location = jconon.URL.application.manage + '?callId=' + el.relationships.parent[0]['cmis:objectId'] + '&applicationId=' + el['cmis:objectId'];
                  });
                };
              } else {
                if (el['jconon_application:esclusione_rinuncia'] !== 'E') {
                  customButtons.reject = function () {
                    Node.submission({
                      nodeRef: el['cmis:objectId'],
                      objectType: 'D:jconon_esclusione:attachment',
                      crudStatus: "INSERT",
                      requiresFile: true,
                      showFile: true,
                      externalData: [
                        {
                          name: 'aspect',
                          value: 'P:jconon_attachment:generic_document'
                        },
                        {
                          name: 'jconon_attachment:user',
                          value: common.User.id
                        }
                      ],
                      modalTitle: i18n['D:jconon_esclusione:attachment'],
                      success: function () {
                        jconon.Data.application.reject({
                          type: 'POST',
                          data: {
                            nodeRef : el['cmis:objectId']
                          },
                          success: function () {
                            $('#applyFilter').click();
                          },
                          error: jconon.error
                        });
                      },
                      forbidArchives: true
                    });
                  };
                } else {
                  customButtons.reject = false;
                }
                if (el.relationships.parent[0]['jconon_call:scheda_valutazione'] === true) {
                  customButtons.scheda_valutazione = function () {
                    URL.Data.search.query({
                      queue: true,
                      data: {
                        q: "select cmis:versionSeriesId from jconon_attachment:scheda_valutazione where IN_FOLDER ('" + el['cmis:objectId'] + "')"
                      }
                    }).done(function (rs) {
                      if (rs.totalNumItems === 0) {
                        UI.confirm('Non &egrave; presente nessuna scheda di valutazione del candidato. Si vuole procedere con la sua predisposizione?', function () {
                          var close = UI.progress();
                          jconon.Data.application.print_scheda_valutazione({
                            type: 'POST',
                            data: {
                              nodeRef : el['cmis:objectId']
                            },
                            success: function (data) {
                              window.location = URL.urls.search.content + '?nodeRef=' + data.nodeRef;
                            },
                            complete: close,
                            error: jconon.error
                          });
                        });
                      } else {
                        window.location = jconon.URL.application.scheda_valutazione + '?applicationId=' + el['cmis:objectId'] + '&nodeRef=' + rs.items[0]['cmis:versionSeriesId'];
                      }
                    });
                  };
                } else {
                  customButtons.scheda_valutazione = false;
                }
              }
            }
            if (common.User.id !== el['jconon_application:user']) {
              customButtons.duplicate = false;
            }
            if (displayActionButton) {
              new ActionButton.actionButton({
                name: el.name,
                nodeRef: el.id,
                baseTypeId: el.baseTypeId,
                objectTypeId: el.objectTypeId,
                mimeType: el.contentType,
                allowableActions: el.allowableActions,
                defaultChoice: defaultChoice
              }, {
                edit: 'CAN_CREATE_DOCUMENT',
                scheda_valutazione: 'CAN_CREATE_DOCUMENT',
                reject: 'CAN_UPDATE_PROPERTIES'
              }, customButtons, {
                print: 'icon-print',
                attachments : 'icon-download-alt',
                curriculum: 'icon-file-alt',
                productList: 'icon-list',
                productSelected: 'icon-list-ol',
                reopen: 'icon-share',
                duplicate: 'icon-copy',
                scheda_valutazione: 'icon-table',
                reject: 'icon-eject'
              }).appendTo(target);
            }
          });
        });
      }
    }
  });

  bulkInfo =  new BulkInfo({
    target: criteria,
    formclass: 'form-horizontal jconon',
    path: rootTypeId,
    name: 'filters',
    callback : {
      afterCreateForm: function () {
        // rearrange btn-group as btn-group-vertical
        $('#filters-attivi_scaduti').
          add('#filters-provvisorie_inviate')
          .addClass('btn-group-vertical');

        criteria.find('input:not("#user")').on('change', filter);

        $('#applyFilter').on('click', filter);

        criteria
          .find('.btn-group-vertical')
          .closest('.widget')
          .on('changeData', filter);

        $('#resetFilter').on('click', function () {
          criteria.find('input').val('');
          criteria.find('.widget').data('value', '');

          var btns = criteria.find('.btn-group-vertical .btn');

          btns
            .removeClass('active');

          criteria
            .find('.btn-group-vertical')
            .find('.default')
            .addClass('active');

        });

        filter();
        if (callId) {
          $('#filters .control-group').hide();
          $('#filters .authority')
            .show()
            .on('changeData', function (event, key, value) {
              if (value) {
                filter();
              }
            });
          $('#filters-provvisorie_inviate').parents('.control-group').show();
        } else {
          $('#filters .authority').hide();
          $('#export-div').remove();
        }
      }
    }
  });
  bulkInfo.render();
});