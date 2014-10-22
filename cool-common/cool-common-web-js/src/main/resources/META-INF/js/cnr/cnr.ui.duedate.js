define(['cnr/cnr'], function (CNR) {
	"use strict";
	return {
		Widget: function (id, labelText, value) {
      var dueDate = new Date(value),
        isPast = CNR.Date.isPast(dueDate),
        span = $('<span>' + (isPast ? 'scaduto ' : 'scade ') + CNR.Date.format(dueDate, "-") + '</span>');

      if (dueDate && isPast) {
        span.addClass('label label-important');
      }
      return span;
    }
	};
});