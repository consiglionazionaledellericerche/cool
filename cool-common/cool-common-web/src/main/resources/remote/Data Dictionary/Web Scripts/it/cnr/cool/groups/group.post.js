/*global jsonUtils,requestbody,logger,status,groupAuthority,model */
/**
 * Post children
 */
var json = jsonUtils.toObject(requestbody.content),
  parentGroupName = json.parent_group_name,
  parentNodeRef = json.parent_node_ref,
  groupName = json.group_name,
  displayName = json.display_name || groupName,
  rootAuthority = groupAuthority.getRootAuthorityPermission(),
  group,
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
	if (!parentGroupName) {
		group = groupAuthority.createGroup(groupName, displayName, zones);
	} else {
		group = groupAuthority.createGroup(groupAuthority.getAuthority(parentGroupName), groupName, displayName, zones);
	}
	model.group = group;
	model.esito = true;
}
