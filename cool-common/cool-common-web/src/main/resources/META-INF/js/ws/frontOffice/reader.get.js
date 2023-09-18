define(['jquery', 'header', 'cnr/cnr.bulkinfo', 'i18n', 'cnr/cnr', 'cnr/cnr.ui.authority', 'cnr/cnr.ui', 'cnr/cnr.url', 'datepicker-i18n'], function ($, header, BulkInfo, i18n, CNR, Authority, UI, URL) {
  "use strict";
  var target = $('#docs'),//div dove vengono caricati i "documenti" recuperati
    bulkinfo,
    suffisso,
    defaultType,
    query,
    filter = {},
    spinner = $('<br><i class="icon-spinner icon-spin icon-2x" id="spinner-' + new Date().getTime() + '"></i>'),
    fadeTime = 100,
    authorityId = 'Authority',
    filtriDiv = $('#filtriDiv'),
    typeLog = $('<div class="control-group"><label class="control-label" for="state">Mostra solo</label><div class="controls"> <div class="btn-group" id="typeLog" data-toggle="buttons-radio"><button type="button" data-state="3" class="btn btn-mini">Browser</button><button type="button" data-state="1" class="btn btn-mini">AjaxError</button></div></div></div>'),
    applicationLog = $('<div class="control-group"><label class="control-label" for="application">Application</label><div class="controls"><div class="btn-group" id="application" data-toggle="buttons-radio"><button type="button" data-state=\'jconon\' class="btn btn-mini">Jconon</button><button type="button" data-state=\'doccnr\' class="btn btn-mini">Doccnr</button></div></div></div>'),
    filterAnswer = $('<div class="control-group widget"><label for="filterAnswer" class="control-label">Parole contenute nella domanda </label><div class="controls"><input type="text" class="input-small datepicker" id="filterAnswer"></div></div>'),
    checkboxVisibiliUtenti = $('<label class=\"checkbox\" id=\"editor\">  <input type=\"checkbox\" name=\"editor\" value=\"true\">  Visualizzare solo documenti visibili agli utenti</label>'),
    createButton = $('<a id=\"createModifyButtonId\" data-role=\"button\" href=\"frontOfficeCreateModify\" class=\"btn btn-small btn-primary\"><i class=\"icon-edit icon-white\"></i> Creazione </a>');

//funzione d'evidenziazione del json(funzionalità visualizza(log))
  function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
      var cls = 'number';
      if (/^"/.test(match)) {
        if (/:$/.test(match)) {
          cls = 'key';
        } else {
          cls = 'string';
        }
      } else if (/true|false/.test(match)) {
        cls = 'boolean';
      } else if (/null/.test(match)) {
        cls = 'null';
      }
      return '<span class="' + cls + '">' + match + '</span>';
    });
  }

  function retrieve(callBack, filter) {
    target.fadeOut(fadeTime, function () {
      spinner.insertBefore(target);
      query = URL.Data.frontOffice.document({
        cache: false, // prevents default caching
        queue: true,
        placeholder: {
          'type_document': [$('#typeDocument .active').data('state')]
        },
        data: filter || {},
        success: function (data) {
          if (typeof callBack === "function") {
            callBack(data);
            target.fadeIn();
          }
        },
        complete: function () {
          spinner.remove();
        },
        fail: function () {
          UI.error('Errore nella ricerca dei Documenti');
        }
      });
    });
  }

//css specifici per alcuni campi dei documenti visualizzati
  function getBadge(type) {
    var el, t = {
      Browser: {
        label: "Browser",
        css: 'badge-warning'
      },
      AjaxError: {
        label: "AjaxError",
        css: 'badge-important animated flash'
      },
      Visibile: {
        label: "Visibile",
        css: 'badge-inverse animated flash'
      },
      Nascosto: {
        label: "Nascosto",
        css: 'animated flash'
      },

      alert: {
        label: "alert",
        css: 'animated flash'
      },
      error: {
        label: "error",
        css: 'badge-important animated flash'
      },
      information: {
        label: "info",
        css: 'badge-info animated flash'
      },
      success: {
        label: "success",
        css: 'badge-success animated flash'
      },
      warning: {
        label: "warning",
        css: 'badge-warning animated flash'
      }
    };
    el = $('<span class="badge">' + (t[type] ? t[type].label : ' '  + type) + '</span>');
    if (t[type] && t[type].css) {
      el.addClass(t[type].css);
    }
    return el;
  }


  function applyFilter() {
    var dateFrom = $('#dateFrom').val(),
      dateTo = $('#dateTo').val(),
      userLog = $('#userLog').val(),
      typeBando = $('#typeBando').val(),//filtro sulla tipologia bando (faq-avvisi)
      filterAnswer = $('#filterAnswer').val();//filtro sulla domanda delle faq

//se non seleziono il campo il widget mi restituisce "" quindi nn entro nell'if => campo undefinited
    if (userLog) {
      filter.userLog = userLog;
    }
    if (typeBando) {
      filter.typeBando = typeBando;
    }
    if (filterAnswer) {
      filter.filterAnswer = filterAnswer;
    }
    if (dateFrom) {
      filter.after = $('#dateFrom').data().datepicker.date.getTime();
    }
    if (dateTo) {
      filter.before = $('#dateTo').data().datepicker.date.getTime();
    }

    filter.typeLog = $('#typeLog .active').data('state');
    filter.application = $('#application .active').data('state');
    //filtro per visualizare solo documenti visibili agli utenti(se selezionato ==> editor=false)
    if ($('input[name=editor]').is(':checked')) {
      filter.editor = false;
    } else {
      filter.editor = true;
    }
  }

  function showDelete(docs) {
    UI.info($('#typeDocument .active').data('state') + ' è stato cancellato');
  }

  function mostraDati(docs) {
    var docsNumber = docs.docs.length;
    if (docsNumber > 1) {
      suffisso = 'ono';
    } else if (docsNumber === 1) {
      suffisso = 'ente';
    } else {
      suffisso = 'ono';
    }
    target.html('')
      .append('<br>').append('<span class="label">' + docsNumber + ' ' + $('#typeDocument .active').data('state') + ' corrispond' + suffisso + ' ai criteri di ricerca</span>')
      .append('<br/>');

    $.each(docs.docs, function (index, el) {
      var item = $('<div style="white-space:normal" align="justify"></div>'),
        btnGroup = $('<div class="btn-group"></div>'),
        btnDelete = $('<button class="delete btn btn-mini btn-danger"><i class="icon-trash icon-white"></i> Cancella</button>').data('nodeRef', el.nodeRef),
        btnShow = $('<button class="showLog btn btn-mini"><i class="icon-upload"></i> Visualizza</a>').data('el', el),
        btnEdit = $('<a href="' + URL.urls.root + 'frontOfficeCreateModify?nodeRef=' + el.nodeRef + '" class="edit btn btn-mini"><i class="icon-edit icon-white"></i> Edit</a>'),
        tipologiaBando = $('<h5>Tipologia Bando: </h5>'),
        line = $('<hr color="red" size="4" >');//linea di separazione tra i documenti recuperati

      btnGroup.append(btnDelete).append(' ');

      if ($('#typeDocument .active').data('state') === 'log') {
        btnGroup.append(' ').append(btnShow);
        item.append('<h5>' + el.name + '</h5>')
          .append('<strong>Type </strong>').append(getBadge(el.type)).append('<br/>')
          .append('<strong>Creato: </strong>' + CNR.Date.format(el.dataCreazione, null, 'ddd DD MMM YYYY')).append(' alle ' + CNR.Date.format(el.dataCreazione, null, 'HH:mm:ss')).append(' (' + CNR.Date.format(el.dataCreazione) + ') ').append('<br/>')
          .append('<strong>dall\'utente </strong>' + el.user).append('<br/>')
          .append('<strong>nell\'applicazione: <strong>' + el.application + '</strong>').append('<br/>')
          .append(btnGroup);
      } else {
        btnGroup.append(' ').append(btnEdit);

        if ($('#typeDocument .active').data('state') === 'notice') {
          item.append('<h5>Nome file Avviso: ' + el.name + '</h5>');
          if (el.number !== null) {
            item.append('<strong>Numero progressivo: </strong>').append(getBadge(el.number)).append('<br/>');
          }
          if (el.type !== null) {
            item.append('<strong>Tipologia Bando: </strong>').append(el.type).append('<br/>');
          }
          item.append('<strong>Titolo: </strong>').append(getBadge(el.title).attr("style", "white-space:normal")).append('<br/>');
          if (el.noticeStyle !== null) {
            item.append('<strong>Style: </strong>').append(getBadge(el.noticeStyle)).append('<br/>');
          }
          item.append('<strong>Testo: </strong>' + el.text).append('<br/>')
            .append('<strong>Data di pubblicazione: </strong>' + CNR.Date.format(el.dataPubblicazione, null, 'ddd DD MMM YYYY')).append('<br/>');
          if (el.dataScadenza) {
            item.append('<strong>Data di scadenza: </strong>' + CNR.Date.format(el.dataScadenza, null, 'ddd DD MMM YYYY')).append('<br/>');
          }
        } else if ($('#typeDocument .active').data('state') === 'faq') {
          if (el.show === true) {
            item.append('<strong>Stato: </strong>').append(getBadge('Visibile')).append('<br/>');
          } else if (el.show === false) {
            item.append('<strong>Stato: </strong>').append(getBadge('Nascosto')).append('<br/>');
          }
          if (el.faqBando) {
            tipologiaBando.append(el.faqBando);
          } else {
            tipologiaBando.append(' - ');
          }
          item.append(tipologiaBando);
          if (el.number !== null) {
            item.append('<strong>Numero progressivo: </strong>').append(getBadge(el.number)).append('<br/>');
          }
          item.append('<strong>Data di creazione: </strong>').append(CNR.Date.format(el.dataCreazione, null, 'ddd DD MMM YYYY')).append('<br/>')
            .append('<strong>Data di pubblicazione: </strong>' + CNR.Date.format(el.dataPubblicazione, null, 'ddd DD MMM YYYY')).append('<br/>')
            .append('<strong>Question: </strong>').append(getBadge(el.question).attr("style", "white-space:normal")).append('<br/>')
            .append('<strong>Answer: </strong>').append(el.answer).append('<br/>');
        }
      }
      item.append(btnGroup);
      item.prepend(line);
      target.append(item);
    });
  }
//delete singolo documento (utilizzando il noderef)
  target.on('click', '.delete', function (event) {
    var nodeRef = $(event.target).data('nodeRef'),
      reNodeRef = new RegExp("([a-z]+)\\:\/\/([a-z]+)\/(.*)", 'gi');
    URL.Data.frontOffice.nodeRef({
      processData: false,
      type: "DELETE",
      placeholder: {
        'store_type' : nodeRef.replace(reNodeRef, '$1'),
        'store_id' : nodeRef.replace(reNodeRef, '$2'),
        'id' : nodeRef.replace(reNodeRef, '$3')
      },
      success: function (data) {
        showDelete(data);
        $('#applyFilter').click();
      },
      error: function () {
        UI.error('Errore nella rimozione dei Documenti');
      }
    });
    return false;
  });
//visualizza singolo log
  target.on('click', '.showLog', function (event) {
    var el = $(event.target).data('el');
    URL.Data.search.content({
      data: {
        'nodeRef': el.nodeRef
      },
      success: function (data) {
        var label = $('<pre class="json"></pre>').addClass('modal-inner-fix').html(syntaxHighlight(JSON.stringify(data, undefined, 4)));
        UI.bigmodal('Visualizzazione di ' + el.name, label);
      },
      error: function () {
        UI.error('Errore nella visualizzazione del Documento');
      }
    });
  });

  $('.datepicker').datepicker({
    language: i18n.locale,
    autoclose: true,
    todayHighlight: true,
    todayBtn: 'linked'
  });

  $('#applyFilter').click(function () {
    applyFilter();
    retrieve(mostraDati, filter);
    return false;
  });

  $('#resetFilter').click(function () {
    $('#typeLog .active').removeClass('active');
    $('#application .active').removeClass('active');
    $('#state .active').removeClass('active');
    $('.datepicker').val('');
    $('#' + authorityId).trigger('reset');
    filter = {};
    //pulisco la checkboxVisibiliUtenti
    $('input[name=editor]').attr('checked', false);
    //pulisco l'ui.select dei bandi
    $('select').each(function () {
      $($(this).children()[0]).attr('selected', 'selected');
      $(this).change();
    });
    return false;
  });

/*Filtri: vengono ricaricato se cambio typeDocument*/
  function bulkinfoFunction() {
    bulkinfo = new BulkInfo({
      target: filtriDiv,
      path: 'frontOfficeBulkinfo',
      name: 'coolDocument',
      callback: {
        afterCreateForm: function () {
          var type = $('#typeDocument .active').data('state');
          CNR.Storage.set('frontOffice', type);
          if (type === 'log') {
            $('#filtriDiv').prepend(typeLog).prepend(applicationLog);
            $('#coolDocument').children().eq(0).children().eq(2).remove();//rimuove il filtro per tipologia di bando (se è selezionato log)
            $('#createModifyButtonId').remove();
          } else {
            $('#userLogButtonId').remove();
            $('#createModify').append(createButton);
            $("label[for='dateFrom']").text("Data di pubblicazione da ");
            $("label[for='dateTo']").text(" a ");
            if (type ===  'faq') {
              $('#coolDocument').append(filterAnswer);
              $('#coolDocument').append(checkboxVisibiliUtenti);
            }
          }
          //query all'avvio 
          if (type !== 'log') {
            filter.editor = true;
            retrieve(mostraDati, filter);
          } else {
            target.html(''); //per i log ripulisco solo la pagina
          }
        }
      }
    });
    bulkinfo.render();
  }
  $('#typeDocument').click(function () {
    if (query !== undefined) {
      query.abort();//stoppo la query se è ancora in esecuzione
    }
    $('#resetFilter').click();
    filtriDiv.html('');//pulisco il div dei filtri
    bulkinfoFunction();
  });

  //carico il tipo di documento che stavo visualizzando l'ultima volta
  defaultType = CNR.Storage.get('frontOffice') || 'log';
  $('#typeDocument button[data-state="' + defaultType + '"]').addClass('active');
  bulkinfoFunction();
});