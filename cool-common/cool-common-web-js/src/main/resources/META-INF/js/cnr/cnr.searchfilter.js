define(['jquery', 'bootstrap', 'searchjs'], function ($) {
  "use strict";

  var searchJsSettings = { // search.js plugin settings
      logger: false
    },
    searchSettings = { // cnr.search.js settings
      fetchCmisObject: false,
      orderBy: false,
      maxItems: 2000,
      elements: {
        table: null,
        pagination: null,
        filter: null
      }
    };



  /**
   *
   * Search filter constructor
   *
   *
   * @param {object} parentSettings cnr.search.js "parent" settings.
   * @param {function} displayResultSetFn function to display the items filtered by search.js.
   * @param {function} searchFn function to retrieve all the data to initialize the search.js engine.
   * @param {function} parentSearchFn cnr.search.js "parent" search function.
   *
   */

  // sort of constructor
  function init(parentSettings, displayResultSetFn, searchFn, parentSearchFn, keysToExclude) {
    var nresults = $('<span class="muted"><span>'),
      controls = $('<div class="controls"></div>'),
      btn = $('<button class="btn btn-small" type="button">Abilita filtro</button>'),
      query = $('<input type="text" />'); // input query element

    query
      .val('')
      .attr('disabled', true)
      .hide()
      .css({
        'margin-bottom': 0,
        'margin-left': '5px'
      })
      .tooltip({
        html: true,
        title: 'Effettua la ricerca su <u>tutti</u> i campi.'
      });

    btn.on('click', function (event) {
      btn
        .attr('disabled', 'disabled')
        .hide();

      searchJsSettings.allowed = function (key) {
        return keysToExclude.indexOf(key) < 0 && !/^cmis/g.test(key)  && !/^alfcmis/g.test(key);
      };

      var customSettings = $.extend(true, {}, parentSettings, searchSettings, {
        queue: parentSettings.disableRequestReplay + '-js', //TODO: verificare
        display: {
          resultSet: function (documents) {
            query.searchjs({
              content: documents,
              engine: searchJsSettings,
              display: function (resultSet) {
                nresults.text('');

                parentSettings.elements.table.find('tbody tr').remove();

                displayResultSetFn($.map(resultSet, function (el, index) {
                  return documents[el];
                }), true);

                if (query.val().trim().length === 0 && typeof parentSearchFn === 'function') {
                  parentSearchFn();
                } else {
                  nresults.text(' ' + resultSet.length + ' ' + (resultSet.length === 1 ? 'elemento trovato' : 'elementi trovati'));
                }
              }
            });
          }
        }
      });
      // retrieve data and initialize Search.js component
      searchFn(customSettings);
    });

    controls
      .append(btn)
      .append(query)
      .append(nresults);

    // render filter DOM elements
    parentSettings.elements.filter
      .empty()
      .append('<label class="control-label">Filtra:</label>')
      .append(controls);

    // disable filter elements
    function disable() {
      btn
        .attr('disabled', null)
        .show();

      query
        .attr('disabled', true)
        .hide();
    }

    return disable;
  }

  return {
    init: init
  };
});