define(['jquery', 'header', 'cnr/cnr.url', 'i18n', 'cnr/cnr.bulkinfo', 'cnr/cnr.ui.widgets', 'cnr/cnr.search', 'cnr/cnr.criteria', 'cnr/cnr.ui'], function ($, header, URL, i18n, BulkInfo, Widgets, Search, Criteria, UI) {
  "use strict";

  return function (options) {

    var bulkinfo,
      search,
      prefixTree = 'a',
      aspectList = [],
      defaults = {
        defaultHandlebars: 'zebra',
        handlebarsMap: {},
        defaultSearchSettings: {}
      },
      settings = $.extend(true, {}, defaults, options);

    function setBulkinfo(bi) {
      bulkinfo = bi;
    }

    function renderBulkInfo(content, type, name, callback) {
      var bulkinfoOptions = {
          target: content,
          formclass: 'form-horizontal',
          kind: 'find',
          name: name,
          path: type,
          callback: callback
        };

      bulkinfo = new BulkInfo(bulkinfoOptions);
      bulkinfo.render();
    }

    function nextPrefix(c) {
      if (/\W/.test(c)) {     // controllo che vengano inseriti la max 25 prefissi
        throw 'Prefisso non valido';
      }
      return String.fromCharCode(c.charCodeAt(0) + 1);
    }

    /*
     * value: array id aspect selezionati
     * data: array di tutti gli aspect recuperati
     */
    function buildAspectsBulkInfo(value, data) {

      var prefix = prefixTree,
        aspectBulkinfo,
        selected;

      // per ogni aspect selezionato crea il bulkinfo da visualizzare
      aspectList = value === null ? [] : $.map(value, function (el) {
        prefix = nextPrefix(prefix);

        // recupera dalla lista degli aspect l'elemento selezionato
        selected = $.grep(data, function (e) {
          return e.key === el;
        });

        aspectBulkinfo = new BulkInfo({
          target: $('.bulkinfoAspect'),
          formclass: 'form-horizontal',
          kind: 'find',
          name: 'search',
          bulkinfoLabel: selected[0].defaultLabel + ':',  // nome dell'aspect inserito come label del bulkinfo
          path: el
        });

        aspectBulkinfo.render();

        return {'prefix': prefix,
          'queryType': selected[0].queryName,
          'bulkinfo': aspectBulkinfo
          };

      });

    }

    /*
     * value: aspect da insereire nell'aspectList
     * bi: bulkinfo aspect
     */
    function buildRawJoin(value, bi) {

      var prefix = prefixTree;

      aspectList.push({
        'prefix': nextPrefix(prefix),
        'queryType': value,
        'bulkinfo': bi
      });

    }

    function getType(bulkInfoProperies) {
      var type = bulkInfoProperies.widget === 'ui.datepicker' ? 'date' : 'string';

      if (bulkInfoProperies.inputType === 'TEXTAREA' &&
          bulkInfoProperies.op === 'IN') {
        type = 'list';
      }

      return type;
    }

    function getBulkinfoProperties(bulkinfo, criteria) {
      var operation,
        type;
      if (bulkinfo) {
        $.each(bulkinfo.getFieldProperties(), function (index, el) {
          var propValue = bulkinfo.getDataValueById(el.name);
          if (el.property && (propValue || typeof propValue === 'boolean')) {
              // XXX: attenzione alle date/string
            criteria[el.op || 'eq'](el.property, propValue, getType(el));
          }
        });
        return criteria;
      }

      return criteria;

    }

    function getAspectsListCriteria() {
      var aspectsListCriteria = [];

      $.each(aspectList, function (index, aspect) {
        var criteria = new Criteria(null, aspect.prefix),
          fieldProperties = aspect.bulkinfo.getFieldProperties();

        if (fieldProperties && fieldProperties.length > 0) {
          criteria = getBulkinfoProperties(aspect.bulkinfo, criteria);
          if (criteria.build().conditions.length !== 0) {
            aspectsListCriteria.push(criteria.build());
          }
        }
      });

      return aspectsListCriteria;
    }

    // crea il criteria a partire dall'elemento del tree selezionato e dai filters.
    // successivamente effettua la ricerca
    function executeQuery(queryName) {
      var searchSettings,
        criteria,
        andCriteria = [],
        andClause = new Criteria();

      searchSettings = $.extend(true, {}, settings.defaultSearchSettings, {type: {'prefix': prefixTree, 'queryType': queryName}});  // from clause
      searchSettings = $.extend(true, {}, searchSettings, {joinTables: aspectList});  // inner join clause

      criteria = new Criteria(null, searchSettings.type.prefix);

      if (bulkinfo && bulkinfo.getFieldProperties().length) {       // controllo che sia stato caricato almeno un bulkinfo dei tipi

        criteria = getBulkinfoProperties(bulkinfo, criteria);       // bulkinfo tree filter
        if (criteria.build().conditions.length !== 0) {
          andClause.and.call(andClause, criteria.build());
        }

        andCriteria = getAspectsListCriteria();                     // aspect list filters
        if (andCriteria.length > 0) {
          andClause.and.apply(andClause, andCriteria);
        }
      }
      search = new Search(searchSettings);
      andClause.list(search);
    }

    function convertObj(obj) {
      return {
        'key': obj.attr.id,
        'label': obj.data,
        'defaultLabel': obj.data,
        'queryName': obj.attr.queryName
      };
    }

    function toAspectsList(obj) {
      return $.map([].concat(obj), function (el) {
        if (el.children) {
          var child = el.children;
          delete el.children;
          return [].concat(convertObj(el)).concat(toAspectsList(child));
        } else {
          return convertObj(el);
        }
      });
    }

    /*
     * per la creazione della maschera di ricerca da query string è necessario specificare:
     *
     * idTree:        id del tipo
     * queryNameTree: query name associato all'id
     *
     * aspects:       lista con gli id degli aspect di cui si vuole visualizzare il bulkinfo
     *
     * e.g.:          ?idTree=F%3Ajconon_application%3Afolder&queryNameTree=jconon_application%3Afolder&aspects=P%3Ajconon_application%3Aaspect_idoneita_fisica&aspects=P%3Ajconon_application%3Aaspect_diploma
     */
    function queryStringRender(content, typeId, aspectIdsArray) {
      // creazione bulkinfo del tree
      if (typeId) {
        renderBulkInfo(content, typeId, 'default');
      }

      // creazione bulkinfo degli aspects
      if (aspectIdsArray && aspectIdsArray.length) {
        aspectIdsArray = $.map(aspectIdsArray, function (el) {
          return decodeURIComponent(el);
        });

        URL.Data.typesTree({
          traditional: true,
          data : {
            "seeds" : aspectIdsArray
          }
        }).done(function (data) {
          try {
            buildAspectsBulkInfo(aspectIdsArray, toAspectsList(data));
          } catch (exception) {
            UI.alert(exception.message);
          }
        });
      }
    }

    return {
      setBulkinfo: setBulkinfo,
      buildRawJoin: buildRawJoin,
      buildAspectsBulkInfo: buildAspectsBulkInfo,
      executeQuery: executeQuery,
      toAspectsList: toAspectsList,
      queryStringRender: queryStringRender
    };

  };

});