require(['jquery', 'header', 'cnr/cnr.url', 'cnr/cnr.ui', 'cnr/cnr.ui.authority', 'i18n', 'cnr/cnr.rbac', 'cnr/cnr.ui.radio', 'list', 'cnr/cnr.rbac'], function ($, header, URL, UI, authority, i18n, rbac, radio, List, Rbac) {
  "use strict";

  var listaIds = [],
    options,
    fliterList;

  function insertRigaTabellaRbac(id, metodo, lista, tipo, valore, authority) {

    var btn, tr, identificativo;

    btn = $('<button class="btn btn-small btn-danger"><i class="icon-trash" /></button>');

    tr = $('<tr></tr>').appendTo('#rbac-admin-table-data-body')
      .append($('<td></td>').text(id).addClass('rbac-id'))
      .append($('<td></td>').text(metodo))
      .append($('<td></td>').text(lista))
      .append($('<td></td>').text(tipo))
      .append($('<td></td>').text(authority || valore).addClass('rbac-authority'))
      .append($('<td></td>').append(btn));

    btn.click(function () {
      identificativo = id + ' ' + metodo + ' ' + lista + ' ' + tipo + ' ' + (authority || '');
      UI.confirm(i18n['conferma-elimina-elemento'] + '<br />' + identificativo, function () {
        rbac.remove({
          "id": id,
          "method": metodo,
          "list": lista,
          "type": tipo,
          "authority": authority
        }).done(function () {
          tr.hide("slow");
        });
      });
    });
  }

  function getTabellaRbacAdmin() {

    URL.Data.rbac().done(function (risposta) {

      $('#rbac-admin-table-data-body').text('');
      listaIds = Object.keys(risposta);

      $.each(risposta, function (idStringa, metodo) {
        $.each(metodo, function (metodoStringa, lista) {
          $.each(lista, function (listaStringa, tipo) {
            $.each(tipo, function (tipoStringa, valore) {
              if (tipoStringa === 'all') {
                insertRigaTabellaRbac(idStringa, metodoStringa, listaStringa, tipoStringa, valore);
              } else {
                $.each(valore, function (authorityIndex, authority) {
                  insertRigaTabellaRbac(idStringa, metodoStringa, listaStringa, tipoStringa, valore, authority);
                });
              }
            });
          });
        });
      });

      options = {
        valueNames : [ 'rbac-id', 'rbac-authority' ]
      };
      fliterList = new List('rbac-admin-data', options);
      fliterList.search($('#rbac-admin-filter').val());

      $('#rbac-admin-table-data').animate({
        opacity : 1
      }, 400);
    });
  }

  function callRbacPost(addIdInput, addMetodoRadio, addListaRadio, addTipoRadio, addTipoUserAuthority, addTipoGroupAuthority) {

    var authority = true;

    if (addTipoRadio.data('value') === 'user') {
      authority = addTipoUserAuthority.data('value');
    } else if (addTipoRadio.data('value') === 'group') {
      authority = addTipoGroupAuthority.data('value');
    }

    if (typeof (addIdInput.val()) === 'undefined' || addIdInput.val() === null || addIdInput.val() === '') {
      UI.alert(i18n['error-empty-path']);
    } else if (addTipoRadio.data('value') !== 'all' && (typeof (authority) === 'undefined' || authority === null || authority === '')) {
      UI.alert(i18n['error-empty-authority']);
    } else {
      Rbac.add({
        "id": addIdInput.val(),
        "method": addMetodoRadio.data('value'),
        "list": addListaRadio.data('value'),
        "type": addTipoRadio.data('value'),
        "authority": authority
      }).done(function () {

        $('#rbac-admin-table-data').animate({opacity: 0}, 100, function () {
          getTabellaRbacAdmin();
        });
      });
    }
  }

  function creaRbac() {
    var addDiv = $('<div />').addClass('form-horizontal'),
      addIdDiv = $('<div id="rbac-admin-add-id-div" />').addClass("control-group").addClass("error").appendTo(addDiv),
      addMetodoDiv = $('<div id="rbac-admin-add-metodo-div" />').addClass("control-group").appendTo(addDiv),
      addListDiv = $('<div id="rbac-admin-add-list-div" />').addClass("control-group").appendTo(addDiv),
      addTipoDiv = $('<div id="rbac-admin-add-tipo-div" />').addClass("control-group").appendTo(addDiv),
      addUserDiv = $('<div id="rbac-admin-add-user-div" />').addClass("control-group").appendTo(addDiv).hide(),
      addGroupDiv = $('<div id="rbac-admin-add-group-div" />').addClass("control-group").appendTo(addDiv).hide(),
      addIdDivLabel = $('<label class="control-label" for="rbac-admin-add-id">Path</label>').addClass('control-label').appendTo(addIdDiv),
      addIdInput = $('<input id="rbac-admin-add-id" autocomplete="off" type="text">').keyup(function () {
        if ($(this).val().trim().length === 0) {
          addIdDiv.addClass('error');
        } else {
          addIdDiv.removeClass('error');
        }
      }).typeahead({'source': listaIds}),
      addIdDivContainer = $('<div class="controls control-group" />').appendTo(addIdDiv).append(addIdInput),
      addMetodoRadio = radio.Widget("rbac-admin-add-metodo", "Label", {
        "default": "GET",
        jsonlist: [
          {
            "key" : "GET",
            "label" : "label.rbac.admin.add.metodo.GET",
            "defaultLabel" : "GET"
          },
          {
            "key" : "POST",
            "label" : "label.rbac.admin.add.metodo.POST",
            "defaultLabel" : "POST"
          },
          {
            "key" : "PUT",
            "label" : "label.rbac.admin.add.metodo.PUT",
            "defaultLabel" : "PUT"
          },
          {
            "key" : "DELETE",
            "label" : "label.rbac.admin.add.metodo.DELETE",
            "defaultLabel" : "DELETE"
          }
        ],
        "class": "btn-small"
      }).appendTo(addMetodoDiv),
      addListaRadio = radio.Widget("rbac-admin-add-list", "Label", {
        "default": "whitelist",
        jsonlist: [
          {
            "key" : "whitelist",
            "label" : "label.rbac.admin.add.list.Whitelist",
            "defaultLabel" : "Whitelist"
          },
          {
            "key" : "blacklist",
            "label" : "label.rbac.admin.add.list.Blacklist",
            "defaultLabel" : "Blacklist"
          }
        ],
        "class": "btn-small"
      }).appendTo(addListDiv),
      addTipoRadio = radio.Widget("rbac-admin-add-tipo", "Label", {
        "default": "all",
        jsonlist: [
          {
            "key" : "all",
            "label" : "label.rbac.admin.add.tipo.all",
            "defaultLabel" : "all"
          },
          {
            "key" : "group",
            "label" : "label.rbac.admin.add.tipo.group",
            "defaultLabel" : "group"
          },
          {
            "key" : "user",
            "label" : "label.rbac.admin.add.tipo.user",
            "defaultLabel" : "user"
          }
        ],
        "class": "btn-small"
      }).appendTo(addTipoDiv),
      addTipoUserAuthority = authority.Widget('rbac-admin-add-tipo_user', i18n['user-label'], {jsonsettings: {usersOnly: true}}).addClass('rbac-admin-add-tipo_user').appendTo(addUserDiv),
      addTipoGroupAuthority = authority.Widget('rbac-admin-add-tipo_group', i18n['group-label'], {jsonsettings: {groupsOnly: true}}).addClass('rbac-admin-add-tipo_group').appendTo(addGroupDiv);

    UI.modal(i18n['rbac-admin-aggiungi-permesso'], addDiv, function () {
      callRbacPost(addIdInput, addMetodoRadio, addListaRadio, addTipoRadio, addTipoUserAuthority, addTipoGroupAuthority);
    }, function () {}, false);
  }

  $('#rbac-admin-button-add').click(creaRbac);
  getTabellaRbacAdmin();
});