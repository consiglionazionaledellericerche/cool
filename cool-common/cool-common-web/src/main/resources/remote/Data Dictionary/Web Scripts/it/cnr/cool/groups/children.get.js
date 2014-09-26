/*global args,utils,groupAuthority,model,status, url */
/**
 * Get children
 */
function main() {
  "use strict";
  var fullName = args.fullName,
    authorityType = args.authorityType,
    sortBy = args.sortBy || "fullName",
    paging = utils.createPaging(args),
    regex = /^((GROUP)|(USER))$/g,
    group,
    rootAuthority = groupAuthority.getRootAuthorityPermission();

  if (rootAuthority.getNodeRef().toString().equals(fullName)) {
    fullName = "";
  }

  if (fullName && fullName.length) {
    group = groupAuthority.getAuthorityPermission(fullName);
    if (!group) {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group :" + fullName + ", does not exist.");
      return;
    }
    model.group = group;
    if (authorityType && !regex.test(authorityType)) {
      status.setCode(status.STATUS_BAD_REQUEST, "The authorityType argument has does not have a correct value.");
      return;
    }
    model.children = groupAuthority.getChildAuthorities(fullName, authorityType, paging, sortBy);
  } else {
    model.group = rootAuthority;
    model.children = groupAuthority.getChildAuthorities(paging, sortBy);
  }
  model.paging = paging;
}
function root() {
  "use strict";
  model.group = groupAuthority.getRootAuthorityPermission();
}
if (url.getMatch() === "/cnr/groups/root") {
  root();
} else {
  main();
}