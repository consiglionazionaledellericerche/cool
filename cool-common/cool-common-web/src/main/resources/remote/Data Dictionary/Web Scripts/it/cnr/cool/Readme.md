Script di esempio da esuire sulla Console.js
===

Per cambiare la propietà di in nodo avendo il NodeRef
---

	var node = search.findNode("workspace://SpacesStore/af25aa4f-4d94-44b4-a5ad-c4184176554f");
	node.getProperties()["{http://www.cnr.it/model/jconon_application/cmis}stato_domanda"] = "R";
	node.save();

	var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_call/cmis}folder\" ";

	var nodes = search.luceneSearch(luceneQuery);
	var i = 0;
	while (i < nodes.length){
	  nodes[i].getProperties()["{http://www.cnr.it/model/jconon_call/cmis}elenco_field_not_required"] = null;
	  nodes[i].save();
	  i = i+1;
	}


	var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_call/cmis}folder\" ";

	var nodes = search.luceneSearch("archive://SpacesStore", luceneQuery);
	nodes.length;
	var i = 0;
	while (i < nodes.length){
	  logger.error(nodes[i].getNodeRef() + nodes[i].getName());
	  i = i+1;
	}

	var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_attachment/cmis}document_mono\" ";

	var nodes = search.luceneSearch(luceneQuery);
	nodes.length;
	var i = 0;
	while (i < nodes.length){
	  nodes[i].addAspect("{http://www.cnr.it/model/jconon_attachment/cmis}generic_document");
	  i = i+1;
	}

	var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_application/cmis}folder\" AND @jconon_application\\:stato_domanda:\"C\"";
	var nodes = search.luceneSearch("workspace://SpacesStore", luceneQuery);
	var i = 0;
	var conta = 0;
	while (i < nodes.length){
	  if (nodes[i].children.length > 0 ) {
	  	conta = conta + 1;
	  }
	  i = i+1;
	}
	conta;



  var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_application/cmis}folder\" AND @jconon_application\\:stato_domanda:\"C\"";
	var nodes = search.luceneSearch("workspace://SpacesStore", luceneQuery);
	var i = 0;
  var domande = [];
	while (i < nodes.length){
	  var node = nodes[i];
	  var permesso = "ALLOWED;" + node.getProperties()["{http://www.cnr.it/model/jconon_application/cmis}user"] + ";Contributor;DIRECT";
	  if (node.getFullPermissions().indexOf(permesso) !== -1) {
	  	domande.push(node.getNodeRef() + " - " + node.getName());
	  }	  
	  i = i+1;
	}
  domande;


  var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_application/cmis}folder\" AND @jconon_application\\:stato_domanda:\"P\" AND PARENT :\"workspace://SpacesStore/e127b850-964c-42b0-8a1a-f5c8cbb714de\" ";
var nodes = search.luceneSearch("workspace://SpacesStore", luceneQuery);
var i = 0;
var j = 0;
var domande = [];
logger.info(nodes.length);
while (i < nodes.length){
  var domanda = nodes[i];
  j = 0;
  while (j < domanda.getChildren().length) {
    if (domanda.getChildren()[j].getSize() == 0) {
      domande.push(domanda.getNodeRef() + " - " + domanda.getName());
	}
  	j = j + 1;
  }
  i = i+1;
}
domande; 


var luceneQuery = "TYPE:\"{http://www.cnr.it/model/jconon_call/cmis}folder\" ";
var nodes = search.luceneSearch("workspace://SpacesStore", luceneQuery);
var i = 0;
var qname = cnrutils.executeStatic('org.alfresco.service.namespace.QName.createQName', "{http://www.cnr.it/model/jconon_call/cmis}data_fine_invio_domande");
while (i < nodes.length){
  //cnrutils.getBean("nodeService").removeProperty(node.nodeRef,qname);
  logger.info("nodi.push({\"nodeRef\" : \"" + nodes[i].getNodeRef() + "\", value : \"" + utils.toISO8601(nodes[i].properties["{http://www.cnr.it/model/jconon_call/cmis}data_fine_invio_domande"]) + "\"});");
  i = i+1;
}


var nodi = [];
var i = 0, node;
while (i < nodi.length) {
  node = search.findNode(nodi[i].nodeRef);
  node.properties["{http://www.cnr.it/model/jconon_call/cmis}data_fine_invio_domande"] = nodi[i].value;
  node.save();
  i = i+1;
}
	