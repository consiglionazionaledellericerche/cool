/*global search*/
var luceneQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}zone\"";
var nodeRefs = search.luceneSearch("workspace://SpacesStore", luceneQuery);
var zones = {};
var i = 0;
while (i < nodeRefs.length) {
  var zone = nodeRefs[i];
  zones[zone.name] = zone.nodeRef.toString();
  i++;
}
model.zones = zones;