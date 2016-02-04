define(['jquery'], function ($) {
  "use strict";

  /* general utility methods */
  function escape(s) {
    return '\'' + s + '\'';
  }

  function parseValue(value, valueType) {
    if (!valueType) {
      return value;
    } else {
      if (valueType === 'date') {
        return "TIMESTAMP '" + value + "'";
      } else if (valueType === 'list') {
        return $.map(value.split(','), function (val, i) {
          return "'" + val + "'";
        }).join(',');
      } else if (valueType === 'string') {
        return "'" + value + "'";
      } else {
        return value;
      }
    }
  }

  /* transforms an object "Criteria" into a string */
  function analyzeCriteria(criteria, prefix) {

    var s, j = [], condiz = "";

    if (criteria.type === "criteria") {

      $.map(criteria.conditions, function (el) {
        var s = analyzeCriteria(el, prefix);
        j.push(s);
      });

      $.map(j, function (el, idx) {
        condiz +=  (idx > 0 ? ' ' + criteria.boolOp + ' ' : '')  + el;
      });
      if (condiz !== "") {
        s = "(" + condiz + ")";
      }

    } else if (criteria.type === "=") {
      s = criteria.what +  ' = ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === "<>") {
      s = criteria.what +  ' <> ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === "IN_FOLDER") {
      if (criteria.to) {
        s = 'IN_FOLDER (' + criteria.to + ',' + escape(criteria.what) + ')';
      } else {
        s = 'IN_FOLDER (' + escape(criteria.what) + ')';
      }
    } else if (criteria.type === ">") {
      s = criteria.what + ' > ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === ">=") {
      s = criteria.what + ' >= ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === "<") {
      s = criteria.what + ' < ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === "<=") {
      s = criteria.what + ' <= ' + parseValue(criteria.to, criteria.valueType);
    } else if (criteria.type === "IN") {
      s = criteria.what + ' IN (' + parseValue(criteria.to, criteria.valueType) + ')';
    } else if (criteria.type === "IN_TREE") {
      s = 'IN_TREE (' + escape(criteria.what) + ')';
    } else if (criteria.type === "NOT") {
      s = 'NOT (' + analyzeCriteria(criteria.what) + ')';
    } else if (criteria.type === "CONTAINS") {
      if (criteria.to) {
        s = 'CONTAINS (' + criteria.to + ',' + escape(criteria.what) + ')';
      } else {
        s = 'CONTAINS (' + escape(criteria.what) + ')';
      }
    } else if (criteria.type === "EQUALS") {
      s = criteria.what + ' = \'' + criteria.to + '\'';
    } else if (criteria.type === "LIKE") {
      s = criteria.where + ' like \'%' + criteria.what + '%\'';
    } else if (criteria.type === "NULL") {
      s = criteria.what +  ' is null ';
    } else if (criteria.type === "NOT NULL") {
      s = criteria.what +  ' is not null ';
    } else {
      s = null;
    }

    return s;
  }

  function Criteria(obj, prefix) {
    var c = obj || {
      type: 'criteria',
      boolOp: 'AND',
      conditions: [],
      prefix: prefix,
      criteria: []
    };
    function toString() {
      return analyzeCriteria(c, prefix);
    }
    function build() {
      c.analyzeCriteria = toString;
      return c;
    }
    return {
      build: build,
      contains: function (s, prefix) {
        var item = {};
        item.type = 'CONTAINS';
        item.to = prefix;
        item.what = s;
        c.conditions.push(item);
        return this;
      },
      like: function (field, value, valueType) {
        var item = {
          type: 'LIKE',
          where: prefix ? prefix + '.' + field : field,
          what: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      lt: function (field, value, valueType) {
        var item = {
          type: '<',
          what: prefix ? prefix + '.' + field : field,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      gt: function (field, value, valueType) {
        var item = {
          type: '>',
          what: prefix ? prefix + '.' + field : field,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      lte: function (field, value, valueType) {
        var item = {
          type: '<=',
          what: prefix ? prefix + '.' + field : field,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      gte: function (field, value, valueType) {
        var item = {
          type: '>=',
          what: prefix ? prefix + '.' + field : field,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      inFolder: function (f, type) {
        var item = {};
        item.type = 'IN_FOLDER';
        item.what = prefix ? prefix + '.' + f : f;
        item.to = type;
        c.conditions.push(item);
        return this;
      },
      inTree: function (f) {
        var item = {};
        item.type = 'IN_TREE';
        item.what = f; //FIXME: prefix ? prefix + '.' + f : f;
        c.conditions.push(item);
        return this;
      },
      equals: function (field, value, valueType) {
        var item = {
          type: 'EQUALS',
          what: prefix ? prefix + '.' + field : field,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      not: function (what) {
        var item = {
          type: 'NOT',
          what: prefix ? prefix + '.' + what : what
        };
        c.conditions.push(item);
        return this;
      },
      and: function () {
        c.conditions.push({
          type: 'criteria',
          boolOp: 'AND',
          conditions: arguments
        });
        return this;
      },
      or: function () {
        c.conditions.push({
          type: 'criteria',
          boolOp: 'OR',
          conditions: arguments
        });
        return this;
      },
      eq: function (property, value, valueType) {
        var item = {
          type: '=',
          what: prefix ? prefix + '.' + property : property,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      IN: function (property, value, valueType) {
        var item = {
          type: 'IN',
          what: prefix ? prefix + '.' + property : property,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      notEq: function (property, value, valueType) {
        var item = {
          type: '<>',
          what: prefix ? prefix + '.' + property : property,
          to: value,
          valueType: valueType
        };
        c.conditions.push(item);
        return this;
      },
      isNull: function (property) {
        var item = {
          type: 'NULL',
          what: prefix ? prefix + '.' + property : property
        };
        c.conditions.push(item);
        return this;
      },
      isNotNull: function (property) {
        var item = {
          type: 'NOT NULL',
          what: prefix ? prefix + '.' + property : property
        };
        c.conditions.push(item);
        return this;
      },
      list: function (search) { //TODO: eliminare metodo list!!!
        search.queryByCriteria(this);
      },
      toString: toString
    };
  }

  return Criteria;
});