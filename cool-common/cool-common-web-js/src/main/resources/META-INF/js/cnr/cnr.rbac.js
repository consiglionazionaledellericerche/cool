define(['cnr/cnr', 'cnr/cnr.url'], function (CNR, URL) {
	"use strict";


	return {
    add: function (data) {
      return URL.Data.rbac({
        type: 'POST',
        data: data
      });
    },
    remove: function (data) {
      return URL.Data.rbac({
        type: 'DELETE',
        placeholder: data
      });
    }
  };
});