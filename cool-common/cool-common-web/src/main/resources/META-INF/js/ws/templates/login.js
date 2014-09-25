define(['jquery', 'header', 'cnr/cnr.validator', 'cnr/cnr'], function ($, header, Validator, CNR) {
  "use strict";

//resetto nodeRefToCopy e nodeRefToCut ad ogni login
  CNR.Storage.set('nodeRefToCopy', '');
  CNR.Storage.set('nodeRefToCut', '');


  Validator.validate($('.form-signin'), {
    rules: {
      username: {
        required: true,
        minlength: 2
      },
      password: {
        required: true
      }
    }
  });
});