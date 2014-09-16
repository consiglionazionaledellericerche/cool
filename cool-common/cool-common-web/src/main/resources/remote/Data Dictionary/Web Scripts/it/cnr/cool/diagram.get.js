/*global cnrutils, companyhome, model, args */

var processDefinitionId = args.definitionId.split('$')[1];

var service = cnrutils.getBean("activitiRepositoryService");

var workflowDefinitions = "Data Dictionary/Workflow Definitions";
var image = "image";

var dir = companyhome.childByNamePath(workflowDefinitions + "/" + image);
if (!dir) {
  throw new Error('need to create the folder "' + (workflowDefinitions + "/" + image) + '" and set visible to everyone');
}

var fileName = "diagram-" + processDefinitionId.replace(/[^a-z0-9]/gi, '_') + ".png";

var diagram = service.getProcessDiagram(processDefinitionId);

var file = dir.childByNamePath(fileName);

if (!file) {
  file = dir.createFile(fileName);
  file.properties.content.write(diagram);
  file.properties.content.mimetype = 'image/png';
}

model.contentNode = file;