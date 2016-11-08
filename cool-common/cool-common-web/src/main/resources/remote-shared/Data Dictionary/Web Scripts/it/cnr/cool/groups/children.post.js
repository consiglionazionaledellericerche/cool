/*global jsonUtils,requestbody,logger,status,groupAuthority,model */
/**
 * Post children
 */
var json = jsonUtils.toObject(requestbody.content),
  parentGroupName = json.parent_group_name,
  childName = json.child_name;

if (parentGroupName === null || childName === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify parent_group_name and child_name");
  model.esito = false;
} else {
	groupAuthority.addAuthority(parentGroupName, childName);
	model.esito = true;
}