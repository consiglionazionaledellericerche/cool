/*global cnrutils,model,search*/
var zoneContainer = cnrutils.getBean("GroupAuthorityService").getZoneContainer();
var nodeRefs = cnrutils.getBean("NodeService").getChildAssocs(zoneContainer);
var zones = [];
var i = 0;
while (i < nodeRefs.size()) {
  var nodeRef = nodeRefs.get(i).getChildRef();
  var key = search.findNode(nodeRef).getProperties()["{http://www.alfresco.org/model/content/1.0}name"];
  zones[key] = nodeRef.toString();
  i++;
}
model.zones = zones;
