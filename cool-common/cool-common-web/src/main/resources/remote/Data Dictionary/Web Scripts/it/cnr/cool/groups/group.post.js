/*global jsonUtils,requestbody,logger,status,groupAuthority,model,_,cnrutils */
/**
 * Post children
 */
var script = "/Company Home/Data Dictionary/Scripts/thirdparty/underscore.js";
var code = cnrutils.getBean("javaScriptProcessor").loadScriptResource(script);
eval(String(code));

var json = jsonUtils.toObject(requestbody.content),
  parentGroupName = json.parent_group_name,
  parentNodeRef = json.parent_node_ref,
  groupName = json.group_name,
  displayName = json.display_name || groupName,
  rootAuthority = groupAuthority.getRootAuthorityPermission(),
  group,
  key,
  zones = [],
  i;

if (json.zones) {
  for (i = 0; i < json.zones.size(); i++) {
    zones.push(json.zones.get(i));
  }
}

if (rootAuthority.getNodeRef().toString().equals(parentNodeRef)) {
  parentGroupName = "";
} else {
  if (!parentGroupName && parentNodeRef) {
    parentGroupName = groupAuthority.getAuthorityPermission(parentNodeRef).getFullName();
  }
}
if (!groupName) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify group_name");
  model.esito = false;
} else {
  group = groupAuthority.getAuthority(groupName);
  if (group === null) {
    if (!parentGroupName) {
      group = groupAuthority.createGroup(groupName, displayName, zones);
    } else {
      group = groupAuthority.createGroup(groupAuthority.getAuthority(parentGroupName), groupName, displayName, zones);
    }
  }
  if (json.extraProperty) {
    _.each(json.extraProperty, function (key, value) {
      "use strict";
      group.properties[value] = key;
    });
    group.save();
  }
  model.group = group;
  model.esito = true;
}