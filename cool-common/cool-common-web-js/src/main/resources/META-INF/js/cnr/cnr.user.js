define(['jquery', 'json!common', 'cnr/cnr.bulkinfo', 'i18n', 'cnr/cnr.url', 'cnr/cnr.ui'], function ($, common, BulkInfo, i18n, URL, UI) {

  "use strict";

  function salvaAccount(bulkinfo, queryType, callback) {
    if (bulkinfo.validate()) {

      var opts = {
          type: 'POST',
          data: bulkinfo.getData(),
          success: callback
        };

      if (queryType === 'PUT') {
        opts.type = "PUT";
        opts.data.push({
          'name' : 'httpMethod',
          'value' : 'PUT'
        });
      }

      URL.Data.account.createAccount(opts);
    } else {
      UI.error('il form contiene elementi non validi');
    }
    return false;
  }

  function renderBulkInfo(userData, afterCreateFormFn, content, isUserNameShow) {

    var bulkinfo,
      settings = {
        target: content,
        formclass: 'form-horizontal',
        path: "accountBulkInfo",
        callback: {
          afterCreateForm: function (form) {
            if (typeof afterCreateFormFn === 'function') {
              afterCreateFormFn(form);
            }
          }
        }
      };

    if (userData) {
      settings.metadata = userData;
    }
    settings.name = (userData ? "editUser" : isUserNameShow ? "createUser" : "createUserWithoutUsername") + ",italy,foreign";

    bulkinfo = new BulkInfo(settings);
    bulkinfo.render();
    return bulkinfo;
  }


  return {
    renderBulkInfo: renderBulkInfo,
    salvaAccount: salvaAccount
  };

});