/*global jsonUtils,requestbody,args,logger,status,groupAuthority,model,search, cnrutils */
/**
 * DELETE version
 */
var nodeRef = args.nodeRef,
  versionLabel = args.versionLabel,
  versionService = cnrutils.getBean("versionService"),
  node = search.findNode(nodeRef),
  version;

if (nodeRef === null || versionLabel === null || node === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify nodeRef and versionLabel");
  model.esito = false;
} else {
  version = versionService.getVersionHistory(node.nodeRef).getVersion(versionLabel);
  if (version.versionType !== null) {
    model.esito = false;
  } else {
    versionService.deleteVersion(node.nodeRef, version);
    model.esito = true;
  }
}