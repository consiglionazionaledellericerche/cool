/*jslint es5:true */
define(['jquery', 'header', 'json!common', 'cnr/cnr', 'ace', 'cnr/cnr.ui', 'cnr/cnr.url'], function ($, header, common, CNR, ace, UI, URL) {

  "use strict";
  var term = $("#outcome"),
    symbol = common.User.isAdmin ? '#' : '$',
    editor = ace.edit("editor"),
    sessions = editor.getSession(),
    timeoutId,
    delay = 600,
    help =
      'function help(item){ \
        var obj = {}; \
        for(k in item){ \
          obj[k] = (typeof item[k] !== "object") ? item[k] : typeof item[k]; \
        } \
        return obj; \
      };';

  // utility functions
  function write(msg, type) {
    var prompt = '<b>' + common.User.id + '@' + 'alfresco' + ' ' + symbol + '</b> ',
      div = $('<div class="console-' + (type || "success") + '"></div>')
        .append((type === 'command' ? prompt : '') + msg);
    term.append(div);
    term.scrollTop(100000); //TODO: hacky
  }

  function ajax(script) {
    var resource = URL.Data.proxy.jsRemote;
    return resource({
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({command: script}),
      error: function () {}
    });
  }

  function executeCmd(command) {

    write(command, 'command');

    ajax(help + command)
      .done(function (data) {
        if (data.error) {
          // javascript errors
          write(data.error, 'error');
        } else {
          $.each(data.logs, function (index, log) {
            write('logger: ' + log);
          });
          var s = JSON.stringify(data.output.content, null, '  ');
          write(s);
        }

      })
      .fail(function (xhr, text, err) {
        // internal server errors
        write("Error: " + text + " " + err, 'error');
      });

    CNR.Storage.set("jsConsoleCode", command);
  }

  function autocompletion(editor) {
    var cursor = editor.selection.getCursor(),
      row = cursor.row,
      tokens = editor.getSession().bgTokenizer.lines[row],
      count = 0,
      i,
      j,
      k,
      token,
      comando,
      p;

    for (i = 0; i < tokens.length && cursor.column > count; i++) {
      token = tokens[i];
      count += token.value.length;
    }

    for (j = i - 1; j >= 0 && !(tokens[j].type === 'text' && !tokens[j].value.trim().length); j--) {
      CNR.log("do nothing");
    }

    p = $(tokens)
      .filter(function (index, el) {
        return index >= j + 1 && index <= i - 1;
      })
      .map(function (index, el) {
        return el.value;
      })
      .toArray()
      .join('');

    comando = p.replace(/\.[^\.]*$/, '');

    ajax(help + 'help(' + comando + ');')
      .done(function (data) {
        var keys = Object.keys(data.output.content), filtered = [];
        CNR.log(keys);
        if (keys.length) {
          filtered = $.grep(keys, function (index, el) {
            var k = p.replace(/.*\./g, '');
            return new RegExp("^" + k + '.+', 'g').exec(index);
          });

          if (filtered.length === 1) {
            editor.removeWordLeft();
            editor.insert(filtered[0]);
          } else if (filtered.length > 1) {
            write(comando + ': ' + filtered.join(', '));
          } else {
            write(comando + ' not found', 'error');
          }
        }
      });
  }

  function suggestion(e) {
    var cmd = editor.session.getTextRange(editor.getSelectionRange());
    window.clearTimeout(timeoutId);
    timeoutId = window.setTimeout(function () {
      if (cmd) {
        ajax(cmd)
          .done(function (data) {
            if (!data.error) {
              var s = JSON.stringify(data.output.content, null, '  ');
              $('.ace_active-line')
                .tooltip('destroy')
                .tooltip({
                  title: s,
                  placement: 'right',
                  trigger: 'manual',
                  container: 'body'
                })
                .tooltip('show');
            }
          });
      }
    }, delay);
  }

  write('', 'command');

  // editor setup
  sessions.setMode("ace/mode/javascript");
  sessions.setTabSize(2);
  sessions.setUseSoftTabs(true);
  editor.setTheme("ace/theme/github");
  editor.setValue(CNR.Storage.get("jsConsoleCode"));

  // custom selection event
  sessions.selection.on('changeSelection', suggestion);

  sessions.on('change', function () {
    $('.tooltip').remove();
  });

  // hotkeys
  editor.commands.addCommand({
    name: 'autocompletion',
    bindKey: {win: 'Ctrl-Space'},
    exec: autocompletion,
    cnr: true
  });

  editor.commands.addCommand({
    name: 'saveSnippet',
    bindKey: {win: 'Ctrl-s'},
    exec: function (editor) {
      var code = editor.getValue();
      UI.dialog("Nome snippet", function (name) {
        var m = CNR.Storage.get("jsConsoleHistory") || {};
        if (name && name.length && !m[name]) {
          m[name] = code;
          CNR.Storage.set("jsConsoleHistory", m);
        } else {
          UI.error("Nome script non valido " + name);
        }
      });
    },
    cnr: true
  });

  editor.commands.addCommand({
    name: 'loadSnippet',
    bindKey: {win: 'Ctrl-o'},
    exec: function (editor) {
      var names = Object.keys(CNR.Storage.get("jsConsoleHistory") || {}),
        content = $.map(names, function (index, el) {
          return $('<button class="btn btn-spaced" data-dismiss="modal">' + index + '</button>');
        }),
        modal = UI.modal("Seleziona il file", content),
        history = CNR.Storage.get("jsConsoleHistory");

      modal.on("click", "button", function (ev) {
        editor.setValue(history[ev.target.innerText]);
      });
    },
    cnr: true

  });

  editor.commands.addCommand({
    name: 'execute',
    bindKey: {win: 'Ctrl-r'},
    exec: function (editor) {
      var code = editor.getValue();
      executeCmd(code);
    },
    cnr: true
  });

  $("#hotkeys").click(function () {
    var lis = $.map(editor.commands.commands, function (el) {
      var text = typeof el.bindKey === "object"  ? el.bindKey.win : el.bindKey;
      return $('<dt>' + text + "</dt>").addClass(el.cnr ? 'label label-success' : 'label').after("<dd>" + el.name + "</dd>");
    });
    UI.modal("Hotkeys", $('<dl class="dl-horizontal"></dl>').append(lis));
  });
});