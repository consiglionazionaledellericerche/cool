/*global cmisserver,paging,args,_,logger,jsonUtils,model,requestbody,cnrutils,utils */

var script = "/Company Home/Data Dictionary/Scripts/thirdparty/underscore.js";
var code = cnrutils.getBean("javaScriptProcessor").loadScriptResource(script);
eval(String(code));

var skipCount = args.skipCount ? Number(args.skipCount) : 0,
  maxItems = args.maxItems ? Number(args.maxItems) : 10,
  query = args.q || '';

//TODO: problemi di prestazioni, recupera tutte le properties del resultSet indipendentemente dal limite della paginazione
var rs = cmisserver.query(query, paging.createPageOrWindow(null, null, skipCount, maxItems)).result;
var resultSet = [];


function addPrefix(el) {
  "use strict";
  return 'P:' + el;
}

// variabili di appoggio
var i, row, nodeRef, ref, node, jsonItem, wrappedProperties, longQNames = {};
for (i = 0; i < rs.length() && i < maxItems; i++) {
  row = rs.getRow(i);
  nodeRef = row.getValue("cmis:objectId");
  args.noderef =  String(nodeRef);
  ref = cmisserver.createObjectReferenceFromUrl(args, null);
  node = cmisserver.getNode(ref);
  jsonItem = jsonUtils.toObject(node.toJSON(true));
  wrappedProperties = node.properties;

  _.each(jsonItem.properties, function (value, key) {
    longQNames[key] = longQNames[key] || utils.longQName(key);
    "use strict";
    var jsonValue,
      re1 = /^cm:/g,
      re2 = /^sys:/g,
      longQName = longQNames[key],
      wrappedValue = wrappedProperties[longQName];

    // convert date to ISO
    if (wrappedValue instanceof Date) {
      value = utils.toISO8601(wrappedValue);
    }

    // convert value to JSON format
    jsonValue = jsonUtils.toJSONString(value);

    if (re1.test(key) || re2.test(key)) {
      delete jsonItem.properties[key];
    } else {
      jsonItem.properties[key] = jsonValue;
    }
  });

  // standard cmis:* properties...
  var fields = _.keys(row.values);
  _.each(fields, function (field) {
    "use strict";
    var value = row.getValue(field);
    if (value && value.getClass().getSimpleName().equals('Date')) {
      value = utils.toISO8601(value);
    }
    jsonItem.properties[field] = jsonUtils.toJSONString(value);
  });

  // add prefix "P:" to each aspect
  jsonItem.aspects = _.map(jsonItem.aspects.toArray(), addPrefix);

  resultSet.push({
    wrapped: node,
    json: {
      properties: jsonItem.properties,
      aspects: jsonItem.aspects
    }
  });
}

model.models = resultSet;
model.totalNumItems = rs.length() + skipCount;
model.maxItemsPerPage = maxItems;
model.activePage = Math.floor(skipCount / maxItems);
model.hasMoreItems = rs.length() > maxItems;

// close resultset 
// http://alfrescoshare.wordpress.com/2009/11/27/coding-best-practice-lucene-search-query-resultset-close-part2/
rs.close();

// cached for half hour
cache.setMaxAge(600);