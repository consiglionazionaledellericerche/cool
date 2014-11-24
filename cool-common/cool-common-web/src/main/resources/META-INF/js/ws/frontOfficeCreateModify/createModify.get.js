define(['jquery', 'header', 'cnr/cnr.bulkinfo', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.url', 'cnr/cnr.ui.wysiwyg', 'cnr/cnr.ui.widgets', 'datepicker-i18n'], function ($, Header, BulkInfo, CNR, UI, URL, Wysiwyg, Widgets) {
  "use strict";
  var typeWriter, //tipo di writer (faq o notice)
    typeBulkinfo, //tipo di bulkinfo da caricare (loadFAQ o loadNotice)
    div = $('#noticeDiv'),
    node_ref = URL.querystring.from.nodeRef, //in caso di modifica di un avviso o una faq esistente
    noticeFAQ = {}, //array con i dati dell'avviso o della faq esistete (da modificare)
    bulkinfo;

//widget per la risposta della FAQ
  Widgets['ui.wysiwyg'] = Wysiwyg;

  typeWriter = CNR.Storage.get('frontOffice', 'notice');
  if (typeWriter === 'undefinited' || typeWriter === 'notice') {
    typeBulkinfo = 'loadNotice';
  } else if (typeWriter === 'faq') {
    typeBulkinfo = 'loadFAQ';
  }

  function bulkInfoFunction() {
    if (node_ref !== undefined) {
    //modifica
      bulkinfo = new BulkInfo({
        target: div,
        path: 'frontOfficeBulkinfo',
        name: typeBulkinfo,
        objectId: node_ref,
        callback: {
          afterCreateForm: function (form) {
            var appo = bulkinfo.getData();
            $.each(appo, function (index, el) {
              noticeFAQ[el.name] = el.value;
            });
            form.addClass('form-horizontal').addClass('jconon');
          }
        }
      });
    } else {
      //creazione
      bulkinfo = new BulkInfo({
        target: div,
        path: 'frontOfficeBulkinfo',
        name: typeBulkinfo,
        callback: {
          afterCreateForm: function (form) {
            form.addClass('form-horizontal').addClass('jconon');
            //recupero il massimo number tra gli avvisi o le faq create
            URL.Data.frontOffice.document({
              cache: false, // prevents default caching
              queue: true,
              data: {},
              placeholder: {
                'type_document': typeWriter,
                editor: true
              },
              success: function (data) {
                if (typeWriter === 'notice') {
                  $('#noticeNumber').val(data.maxNotice);
                } else if (typeWriter === 'faq') {
                  $('#faqNumber').val(data.maxFaq);
                }
              },
              error: function () {
                $('#noticeNumber').val('');
              }
            });
          }
        }
      });
      $("#modify").html('Salva');
    }
    bulkinfo.render();
  }

  bulkInfoFunction();

  $('#modify').click(function () {
    if (bulkinfo.validate()) {
      var close = UI.progress(),
        appo = bulkinfo.getData(),
        azione;
      if (node_ref === undefined) {
        azione = ' creato ';
      } else {
        azione = ' modificato ';
      }
      noticeFAQ.nodeRefToEdit = node_ref;
      $.each(appo, function (index, el) {
        noticeFAQ[el.name] = el.value;
      });
      URL.Data.frontOffice.doc({
        type: "POST",
        data: {
          stackTrace: JSON.stringify(noticeFAQ),
          type_document: typeWriter
        },
        success: function (data) {
          UI.success(typeWriter + azione + 'correttamente', function () {
            window.location = URL.urls.root + 'frontOffice';
          });
        },
        fail: function () {
          if (azione === 'creato') {
            azione = 'creazione';
          } else {
            azione = 'modifica';
          }
          UI.error('Errore nella' + azione + ' del ' + typeWriter);
        },
        complete: close
      });
    }
  });
});