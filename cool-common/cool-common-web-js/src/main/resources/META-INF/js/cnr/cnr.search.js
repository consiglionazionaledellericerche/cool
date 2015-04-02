/* javascript closure providing all the search functionalities */
define(['jquery', 'cnr/cnr', 'i18n', 'cnr/cnr.url', 'cnr/cnr.searchfilter', 'cnr/cnr.criteria'], function ($, CNR, i18n, URL, SearchFilter, Criteria) {
  "use strict";

  return function Search(options) {

    var executeQuery,
      baseColumns = ["cmis:name", "cmis:baseTypeId", "cmis:objectId", "cmis:objectTypeId",
          "cmis:lastModificationDate", "cmis:lastModifiedBy", "alfcmis:nodeRef"],
      defaults = {
        maxItems: 10,
        type: "cmis:document",
        joinTables: [],
        fetchCmisObject: false,
        calculateTotalNumItems: false,
        fields: {
          "nome": "cmis:name",
          "data di creazione": "cmis:creationDate"
        },
        orderBy: {
          field: "cmis:lastModificationDate",
          asc: false
        },
        elements: {},
        lastCriteria: {},
        disableRequestReplay: true,
        refreshFn : function () {
          executeQuery();
        },
        display: {
          row: function (el, refreshFn) {
            return $('<tr><td>' + el.name + '</td><td><a href="#" title="' + el.id + '" class="btn btn-small">' + CNR.mimeTypeIcon(el.contentType, el.name) + ' ' + i18n.view  + '</a></td></tr>');
          },
          after: function (documents) {
            CNR.log(documents.totalNumItems);
          }
        },
        dataSource: function (page, setting, getUrlParams) {
          return URL.Data.search.query({
            queue: setting.disableRequestReplay,
            data: getUrlParams(page)
          });
        }
      },
      settings = $.extend(true, {}, defaults, options);

    /* full text search */
    function query(keywords) {

      var criteriaContains = new Criteria().contains(keywords).build(),
        clauses,
        criteriaFields;

      if (keywords.length) {

        clauses = $.map(keywords.split(/[^0-9a-zA-Z]/g), function (keyword) {
          return 'cmis:name:' + keyword;
        }).join(' and ');

        criteriaFields = new Criteria().contains(clauses).build();

        settings.lastCriteria = new Criteria().or(criteriaContains, criteriaFields).build();
      }

      executeQuery();
    }

    /* displays documents (and, if !documentsOnly, folders too) in folder "noderef" */
    function displayDocs(nodeRef, documentsOnly) {
      if (nodeRef) {
        settings.lastCriteria = new Criteria({
          type: "criteria",
          conditions: [
            {
              type: "IN_FOLDER",
              what: nodeRef,
              documentsOnly: documentsOnly || false
            }
          ]
        }).build();
      } else {
        settings.lastCriteria = new Criteria(null).build();
      }

      executeQuery();
    }

    function changeOrder(order) {
      settings.orderBy = order;
      executeQuery();
    }

    function changeType(type) {
      if (type) {
        settings.type = type;
      }
      return settings.type;
    }

    /* "private" methods, utility functions */
    function getInterval(pages, pagesToDisplay, currentPage) {
      var half = Math.ceil(pagesToDisplay / 2),
        upperLimit = pages - pagesToDisplay,
        start = currentPage > half ? Math.max(Math.min(currentPage - half, upperLimit), 0) : 0,
        end = currentPage > half ? Math.min(currentPage + half, pages) : Math.min(pagesToDisplay, pages);
      return {start: start, end: end};
    }

    function buildFromClause() {
      // FIXME: aggiungere controllo sui froms null
      var clause = ' FROM ';

      if (typeof settings.type === 'string') {
        return clause + settings.type;
      }

      clause += settings.type.queryType + ' AS ' + settings.type.prefix;
      $.each(settings.joinTables, function (index, element) {
        clause += ' INNER JOIN ' + element.queryType + ' AS ' + element.prefix + ' ON ' +
          settings.type.prefix + '.cmis:objectId' + ' = ' + element.prefix + '.cmis:objectId';
      });

      return clause;
    }

    function getUrlParams(page) {
      page = page || 0;

      var o = {
        maxItems: settings.maxItems,
        skipCount: (settings.maxItems * page),
        fetchCmisObject: settings.fetchCmisObject,
        calculateTotalNumItems: settings.calculateTotalNumItems,
        cache: settings.cache,
        groups: settings.groups

      },
        conditions = settings.lastCriteria.conditions,
        // FIXME: aggiungere il prefisso anche al sort ???
        sort = settings.orderBy ? settings.orderBy.field + (settings.orderBy.asc ? " ASC " : " DESC ") : false,
        query, columns = settings.columns ? baseColumns.concat(settings.columns).join(',') : "*";

      if (conditions && conditions.length === 1 && conditions[0].type === "IN_FOLDER" && conditions[0].documentsOnly === false) {
        o.f = conditions[0].what;
        o.orderBy = sort;
      } else {
        query = "SELECT " + columns + buildFromClause();
        if (Object.keys(settings.lastCriteria).length) {
          if (Object.keys(settings.lastCriteria.conditions).length) {
            query += " WHERE " + settings.lastCriteria.analyzeCriteria() + " ";
          }
        }
        if (sort) {
          query += " ORDER BY " + sort;
        }
        o.q = query;
      }
      return o;
    }

    function changePage(page) {
      executeQuery(page);
    }

    function pagination(documents) {

      var i,
        pagesToDisplay = 10,
        list = settings.elements.pagination.find("ul"),
        nPages = Math.ceil(documents.totalNumItems / documents.maxItemsPerPage),
        page = documents.activePage,
        interval,
        prev,
        cur,
        next,
        hasTotalNumItems = documents.totalNumItems >= 0,
        showPagination = hasTotalNumItems ? (nPages > 1) : (page > 0 || documents.hasMoreItems);
      list.find("li").remove();

      if (showPagination) {

        prev = $('<li><a href="#"><i class="icon-backward"></i> ' + i18n.prev + '</a></li>').appendTo(list);
        prev.data("page", page - 1);
        if (nPages === 1 || page === 0) {
          prev.addClass("disabled");
        }

        interval = getInterval(nPages, pagesToDisplay, page);

        if (hasTotalNumItems && interval.start > 0) {
          $('<li class="disabled"><a href="#">...</a></li>').appendTo(list);
        }

        if (hasTotalNumItems) {
          for (i = interval.start; i < interval.end; i++) {
            cur = $("<li><a href=\"#\">" + (i + 1) + "</a></li>").appendTo(list);
            cur.data("page", i);
            if (page + 1 === i + 1) {
              cur.addClass("active");
            }
          }
        }

        if (hasTotalNumItems && nPages > interval.end) {
          $('<li class="disabled"><a href="#">...</a></li>').appendTo(list);
        }

        next = $('<li><a href="#">' + i18n.next + ' <i class="icon-forward"></i></a></li>').appendTo(list);
        next.data("page", page + 1);
        if ((hasTotalNumItems && (nPages === 1 || page + 1 === nPages)) || (!hasTotalNumItems && !documents.hasMoreItems)) {
          next.addClass("disabled");
        }

        list.find("li:not('.active'):not('.disabled')").on("click", function (e) {
          var pag = $(this).data("page");
          changePage(pag);
          e.preventDefault();
        });
      }

      return showPagination;
    }

    function defaultMapping(doc) {
      return {
        'name': doc["cmis:name"],
        'contentType': doc["cmis:baseTypeId"] === "cmis:folder" ? "folder" : doc["cmis:contentStreamMimeType"],
        'id': doc["cmis:objectId"],
        'objectTypeId': doc["cmis:objectTypeId"],
        'baseTypeId': doc["cmis:baseTypeId"],
        'allowableActions': doc.allowableActions,
        'contentStreamLength': doc['cmis:contentStreamLength'] || 0,
        'lastModificationDate' : doc['cmis:lastModificationDate'],
        'lastModifiedBy': doc['cmis:lastModifiedBy']
      };
    }

    function processResultSet(documents, isFilter) {

      var showPagination,
        list,
        sorting = {},
        resultSet,
        disableFilterFn;

      if (documents.items) {
        // 'standard' alfresco documents resultset
        resultSet = $.map(documents.items, function (doc) {

          var mapping = defaultMapping(doc);
          $.each(doc, function (key, value) {
            mapping[key] = value;
          });
          return settings.mapping ? settings.mapping(mapping, doc) : mapping;
        });
      } else {
        // as hoc resultset
        resultSet = documents;
      }

      if (resultSet.length > 0) {

        if (settings.display.resultSet) {
          settings.display.resultSet(resultSet, settings.elements.table);
        } else {
          $.each(resultSet, function (key, el) {
            var row = settings.display.row(el, settings.refreshFn);
            if (settings.elements.table) {
              settings.elements.table.append(row);
            }
          });
          settings.display.after(documents, settings.refreshFn, resultSet, isFilter);
        }


        showPagination = settings.elements.pagination && pagination(documents);
        if (settings.elements.label) {
          settings.elements.label.fadeOut();
        }

        if (settings.elements.table) {
          settings.elements.table.fadeIn();
        }

        if (settings.elements.pagination) {
          if (showPagination) {
            settings.elements.pagination.fadeIn();
          } else {
            settings.elements.pagination.fadeOut();
          }
        }

        if (settings.elements.orderBy) {
          // inizializzazione del dropdown per la selezione dell'ordinamento
          sorting[i18n.asc] = "ASC";
          sorting[i18n.desc] = "DESC";
          list = settings.elements.orderBy.find("ul");
          list.find('li').remove();

          $.each(settings.fields, function (field, fieldId) {

            if (fieldId) {

              var a = $('<a href="#">' + field + '</a>'),
                item = $('<li></li>').append(a).appendTo(list),
                isAsc;

              if (settings.orderBy.field === fieldId) {
                isAsc = !settings.orderBy.asc;
                $('<i></i>').addClass(isAsc ? 'icon-caret-down' : 'icon-caret-up').appendTo(a);
              } else {
                isAsc = true; //asc by default
              }

              a
                .data({
                  field: fieldId,
                  asc: isAsc
                });
            }

          });

          list.off('click').on('click', 'a', function (event) {
            changeOrder($(this).data());
          });

          settings.elements.orderBy.fadeIn().css("display", "inline-block");
        }

      } else {
        settings.display.after(documents, settings.refreshFn, resultSet, isFilter);
        if (settings.elements.label) {
          settings.elements.label.fadeIn();
        }
        if (settings.elements.table) {
          settings.elements.table
            .add(settings.elements.pagination)
            .add(settings.elements.orderBy)
            .fadeOut();
        }
      }
      // filter based on Search.js
      if (settings.elements.filter && !isFilter) {
        disableFilterFn = SearchFilter.init(
          settings,
          processResultSet,
          function (customSettings) {
            customSettings.type = settings.filter.getType();
            new Search(customSettings).execute();
          },
          function () {
            disableFilterFn();
            executeQuery();
          },
          Object.keys(defaultMapping({}))
        );
      }
    }

    executeQuery = function (page) {
      var spinner = $('<i class="icon-spinner icon-spin icon-2x" id="spinner-' + new Date().getTime() + '"></i>'), xhr;
      spinner.insertAfter(settings.elements.label);

      if (settings.elements.table) {
        settings.elements.table
          .find("tbody tr").remove().end()
          .add(settings.elements.pagination)
          .add(settings.elements.orderBy)
          .hide();
      }

      xhr = settings.dataSource(page, settings, getUrlParams); //TODO: passare qualcosa di meno estremo di settings...

      xhr
        .done(function (rs) {
          processResultSet(rs);
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
          CNR.log(jqXHR, textStatus, errorThrown);
        })
        .always(function () {
          spinner.remove();
        });
    };

    /* Revealing Module Pattern */
    return {
      displayDocs: displayDocs,
      query: query,
      changeOrder: changeOrder,
      changeType: changeType,
      execute: executeQuery,
      queryByCriteria: function (criteria) {
        settings.lastCriteria = criteria.build();
        executeQuery();
      }
    };
  };
});