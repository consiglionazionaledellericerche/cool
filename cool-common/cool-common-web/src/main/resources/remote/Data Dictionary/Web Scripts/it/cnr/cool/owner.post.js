/*global jsonUtils,requestbody,logger,status,groupAuthority,model,search,args */
/**
 * Post children
 */
var json = jsonUtils.toObject(requestbody.content),
  nodeRef = json.nodeRef,
  userid = json.userid,
  children = json.children,
  excludedTypes = json.excludedTypes || [],
  i = 0;

if (nodeRef === null || userid === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify nodeRef and userid");
  model.esito = false;
} else {
  var node = search.findNode(nodeRef), el;
  if (children) {
    for (i = 0; i < node.children.length; i++) {
      el = node.children[i];
      if (excludedTypes.indexOf(String(el.type)) < 0 && el.owner !== userid) {
        el.setOwner(userid);
      }
    }
  } else {
    if (node.owner !== userid) {
      node.setOwner(userid);
    }
  }
  model.esito = true;
}
