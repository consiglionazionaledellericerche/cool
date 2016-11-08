/*global args,model,groupAuthority,status,requestbody */
/**
 * Post children
 */

var parentGroupNodeRef = args.parentNodeRef,
  parentGroupName = groupAuthority.getAuthorityPermission(parentGroupNodeRef).getFullName(),
  childName = args.childFullName;

if (parentGroupName === null || childName === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify parentNodeRef and childFullName");
  model.esito = false;
} else {
	groupAuthority.removeAuthority(parentGroupName, childName);
	model.esito = true;
}