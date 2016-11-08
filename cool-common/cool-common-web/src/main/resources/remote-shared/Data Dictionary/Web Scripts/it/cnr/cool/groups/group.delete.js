/*global args,model,groupAuthority,status,requestbody */
/**
 * Post children
 */

var groupNodeRef = args.group_node_ref,
  groupName = args.group_name,
  cascade =  (args.cascade === "true");
if (groupName === null && groupNodeRef !== null) {
	groupName = groupAuthority.getAuthorityPermission(groupNodeRef).getFullName();
}
if (groupName === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify groupName or groupNodeRef");
  model.esito = false;
} else {
	groupAuthority.deleteGroup(groupName, cascade);
	model.esito = true;
}
