define(['jquery', 'header', 'cnr/cnr.validator'], function ($, header, Validator) {
  "use strict";
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