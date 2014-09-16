/*global jsonUtils,requestbody,logger,status,groupAuthority,model,search,args */
/**
 * Post removeToFolder
 */
var parentNodeRef = args.parentNodeRef,
  childNodeRef = args.childNodeRef;

if (parentNodeRef === null || childNodeRef === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify childNodeRef and parentNodeRef");
  model.esito = false;
} else {
  var parentNode = search.findNode(parentNodeRef),
    childNode = search.findNode(childNodeRef);
  parentNode.removeNode(childNode);
  model.esito = true;
}