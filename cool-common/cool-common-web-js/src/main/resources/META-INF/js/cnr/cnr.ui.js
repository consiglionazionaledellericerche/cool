// reusable user interface components
define(['jquery', 'bootstrap'], function ($) {
  "use strict";

  function modal(title, content, callback, callbackClose, isBigModal) {
    var hasCallback = typeof callback === "function",
      myModal = $('<div role="dialog" tabindex="-1" data-backdrop="static" class="modal hide fade"></div>'),
      modalBody = $('<div class="modal-body"></div>').append(content),
      btnClose = $('<button class="btn" data-dismiss="modal" aria-hidden="true"></button>').append(hasCallback ? "annulla" : "chiudi"),
      btn = $('<button data-dismiss="modal" class="btn btn-primary submit">ok</button>').click(callback);
    if (title) {
      myModal.append('<div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button> <h3 id="myModalLabel">' + title + '</h3></div>');
    }

    myModal
      .append(modalBody)
      .append($('<div class="modal-footer"></div>').append(hasCallback ? btn : ' ').append(btnClose))
      .modal("show")
      .on('hidden', function (event) {
        if (event.target === myModal[0] && typeof callbackClose === 'function') {
          callbackClose();
        }
      })
      .addClass(isBigModal ? 'hugemodal' : '');

    return myModal;
  }

  function bigmodal(title, content, callback, callbackClose) {
    return modal(title, content, callback, callbackClose, true);
  }

  function alert(text, type, callbackClose, modalInnerFix, isBigModal) {
    type = type || "Warning";
    var icon = $('<i></i>'),
      heading = $("<h1>" + type + "</h1>").addClass('animated flash'),
      content,
      symbols =  {
        Success: 'icon-ok text-success',
        Error: 'icon-minus-sign text-error',
        Info: 'icon-info-sign text-info'
      };

    icon
      .addClass(symbols[type] || 'icon-warning-sign text-warning')
      .prependTo(heading);

    content = heading
      .after(text);
    modal(null, modalInnerFix ? $('<div></div>').addClass('modal-inner-fix').append(content) : content, null, callbackClose, isBigModal);
  }

  return {
    modal: modal,
    bigmodal: bigmodal,
    dialog: function (text, callback) {
      var input = $('<input type="text" />');
      modal(text || "Inserimento dati", input, function () {
        callback(input.val());
      });
    },
    confirm: function (text, callback) {
      modal(null, '<i class="icon-question-sign icon-4x text-info"></i> ' + text, callback);
    },
    alert: alert,
    success: function (text, callback) {
      alert(text, "Success", callback);
    },
    error: function (text, callback, modalInnerFix, isBigModal) {
      alert(text, "Error", callback, modalInnerFix || false, isBigModal || false);
    },
    info: function (text, callback) {
      alert(text, "Info", callback);
    },
    progress: function () {
      var striped = $('<div class="progress progress-striped progress-warning active"></div>').append('<div class="bar" style="width: 100%;"></div>'),
        myModal = modal(null, striped);
      myModal
        .addClass('modal-invisible')
        .on('hide', function () {
          return false;
        })
        .css('z-index', 1055);

      return function () {
        myModal
          .off('hide')
          .modal('hide');
      };
    },
    breadCrumb: function (target, path, f) {
      target.html('');
      $.each(path, function (index, item) {
        var li = $('<li></li>'), a;
        if (index === 0) {
          li.append('<i class="icon-home"></i>').append(' ');
        }
        if (index < path.length - 1) {
          a = $('<a href="#">' + item.name + '</a>')
            .click(f)
            .data(item)
            .data('index', index);
          li
            .append(a)
            .append('<span class="divider">/</span>');
        } else {
          li
            .append(item.name)
            .addClass('active');
        }
        target.append(li);
      });
    }
  };
});