/*global cnrutils, person, model, args, argsM*/
//var keys = ["{http://www.cnr.it/model/workflow/1.0}groupName", "{http://www.cnr.it/model/workflow/1.0}notifyMe"];

var keys = argsM.properties || [];
var ids = argsM.assignedByMeWorkflowIds || [];

// variables
var service = cnrutils.getBean("WorkflowService");
var state = cnrutils.constant("org.alfresco.service.cmr.workflow.WorkflowTaskState.IN_PROGRESS");

var tasks = service.getAssignedTasks(person.properties.userName, state);

var m, i, j;

function transformMap(props) {
  "use strict";
  var m2 = cnrutils.transformMap(props),
    key,
    obj;
  obj = {};
  for (j = 0; j < keys.length; j++) {
    key = keys[j];
    obj[key] = m2[key];
  }
  return obj;
}

function getPathProperties(instanceId) {
  "use strict";
  var props = service.getPathProperties(instanceId);
  return transformMap(props);
}

m = {mine : {}, theirs : {}};

// mine (taks)
for (i = 0; i < tasks.size(); i++) {
  var task = tasks.get(i);
  m.mine[task.id] = getPathProperties(task.path.id);
}

// theirs (workflows)
for (i = 0; i < ids.length; i++) {
  var instanceId = ids[i];
  m.theirs[instanceId] = getPathProperties(instanceId);
}

model.data = m;