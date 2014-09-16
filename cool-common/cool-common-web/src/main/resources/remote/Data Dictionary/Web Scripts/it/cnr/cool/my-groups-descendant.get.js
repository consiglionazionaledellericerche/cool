/*global people, url, args, groups, _, model,cnrutils */
var script = "/Company Home/Data Dictionary/Scripts/thirdparty/underscore.js";
var code = cnrutils.getBean("javaScriptProcessor").loadScriptResource(script);
eval(String(code));

var user = people.getPerson(url.templateArgs.user);
var zone = args.zone;
var mygroups = people.getContainerGroups(user);

var parenthood = {};
var detail = {};

function addToResp(myGroup) {
  "use strict";
  var parent = groups.getGroupForFullAuthorityName(myGroup.getFullName()).getParentGroups()[0],
    key = parent ? parent.getFullName() : "ROOT";
  detail[myGroup.getFullName()] = myGroup;
  parenthood[key] = (parenthood[key] || []).concat(myGroup.getFullName());
}

_.each(mygroups, function (el) {
  "use strict";
  var myGroupName = el.properties.authorityName,
    myGroup = groups.getGroupForFullAuthorityName(myGroupName);
  if (zone) {
    if (groups.searchGroupsInZone(myGroup.getShortName(), zone).length > 0) {
      addToResp(myGroup);
    }
  } else {
    addToResp(myGroup);
  }
});

function explore(key) {
  "use strict";
  var m = {};
  _.each(parenthood[key || "ROOT"], function (k, index) {
    m[k] = parenthood[k] ? explore(k) : true;
  });
  return m;
}

model.tree = explore();
model.detail = detail;