define([], function () {
	"use strict";
	return {
		Widget: function (id, labelText, priority) {
	    var el, t = {
	      1: {
	        label: "bassa"
	      },
	      3: {
	        label: "importante",
	        css: 'badge-warning'
	      },
	      5: {
	        label: "critico",
	        css: 'badge-important animated flash'
	      }
	    };
	    el = $('<span class="badge">' + (t[priority] ? t[priority].label : 'priorita\' '  + priority) + '</span>');
	    if (t[priority] && t[priority].css) {
	      el.addClass(t[priority].css);
	    }
	    return priority > 1 ? el : "";
	  }
	};
});