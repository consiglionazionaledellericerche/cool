/*jslint vars:true */
/*global argsM,people,model,search,cnrutils,_ */

var script = "/Company Home/Data Dictionary/Scripts/thirdparty/underscore.js";
var code = cnrutils.getBean("javaScriptProcessor").loadScriptResource(script);
eval(String(code));


var TASKS_LIMIT = 10000;


/**
  *
  * GET USERNAME
  * returns the username of a given user nodeRef
  *
  */
function getUsername(nodeRef) {
  "use strict";
  return search.findNode(nodeRef).properties['{http://www.alfresco.org/model/content/1.0}userName'];
}


/**
  *
  * GET ALL WORKFLOWS
  * returns a map containing all the workflows (the key is workflow id, the value is the actual workflow object)
  *
  */
function getAllWorkflows() {
  "use strict";

  var workflowService = cnrutils.getBean('workflowServiceImpl');
  var l = workflowService.getWorkflows();
  var m = [];
  var i;

  for (i = 0; i < l.size(); i++) {
    var wf = l.get(i);
    var id = wf.id.split('\\$')[1];
    m[id] = {
      startTask: workflowService.getStartTask(wf.id),
      wf: wf
    };

  }

  return m;
}


/**
 *
 * SERIALIZE TASK TO JSON
 * return a JSON representation of a given task
 *
 */
function serializeTask(task) {
  "use strict";

  var j;

  // Query Variables
  var variables = task.queryVariables;
  var queryVariables = {};
  for (j = 0; j < variables.size(); j++) {
    var variable = variables.get(j);
    //TODO: gestire tipi di dato diversi da String
    queryVariables[variable.name] = variable.textValue;
  }

  return {
    name: task.name,
    description: task.description,
    taskDefinitionKey: task.taskDefinitionKey,
    assignee: task.assignee,
    owner: task.owner,
    id: task.id,
    createTime: task.createTime ? utils.toISO8601(task.createTime) : null,
    startTime: task.startTime ? utils.toISO8601(task.startTime) : null,
    endTime: task.endTime ? utils.toISO8601(task.endTime) : null,
    javaclass: task.getClass().getCanonicalName(),
    queryVariables: queryVariables
  };
}


/**
 *
 * TO ARRAY
 * convert a java List to a javascript Array
 *
 */
function toArray(list) {
  "use strict";
  var array = [];
  var i;

  for (i = 0; i < list.size(); i++) {
    array.push(list.get(i));
  }

  return array;
}


/**
  *
  * GET ALL TASKS
  * returns an array of tasks
  *
  */
function getAllTasks() {
  "use strict";
  var taskService = cnrutils.getBean('activitiTaskService');

  var rs = taskService
    .createTaskQuery()
    .includeTaskLocalVariables()
    .includeProcessVariables()
    .listPage(0, TASKS_LIMIT);

  return toArray(rs);
}


/**
  *
  * GET START TASK PROPERTIES
  * serialize the start task properties to JSON
  *
  */
function getStartTaskProperties(startTask) {
  "use strict";
  // start task properties
  var j;
  var props = cnrutils.transformMap(startTask.properties);
  var startTaskProperties = {};
  for (j in props) {
    if (props[j] !== undefined && props[j] !== null) {
      if (props[j].getClass && String(props[j].getClass().getCanonicalName()) === 'java.util.Date') {
        startTaskProperties[j] = utils.toISO8601(props[j]);
      } else {
        startTaskProperties[j] = props[j];
      }
    }
  }
  return startTaskProperties;
}

/**
  *
  * GET ALL HISTORIC TASKS
  * retrieve all historic tasks
  *
  */
function getAllHistoricTasks() {
  "use strict";
  var historicService = cnrutils.getBean('activitiHistoryService');
  var rs = historicService.createHistoricTaskInstanceQuery()
    .includeTaskLocalVariables()
    .includeProcessVariables()
    .listPage(0, TASKS_LIMIT);
  return toArray(rs);
}


/**
 *
 * GET USER GROUPS
 * get groups of a given user
 *
 */
function getUserGroups(username) {
  "use strict";

  var person = people.getPerson(username);
  var groups = people.getContainerGroups(person);

  return _.map(groups, function (group) {
    return group.properties['{http://www.alfresco.org/model/content/1.0}authorityName'];
  });
}

/**
 *
 * FILTER USERS
 * remove duplicates and null values from the list of the users involved in the workflow
 *
 */
function filterUsers(users) {
  "use strict";
  var l = _.filter(users, function (el) {
    return el !== null && el !== undefined;
  });

  l = _.uniq(l, function (x) {
    return String(x).valueOf();
  });
  return l;
}


/**
 *
 * GET UNIQUE GROUPS
 * return an array of groups involved into the workflow
 *
 */
function getUniqueGroups(users) {
  "use strict";
  var groups = [];

  _.each(users, function (user) {
    var userGroups = getUserGroups(user);
    _.each(userGroups, function (group) {
      if (groups.indexOf(group) < 0) {
        groups.push(group);
      }
    });
  });

  return groups;
}


/**
 *
 * SERIALIZE WORKFLOW
 * generate a JSON representation of the workflow
 *
 */
function serializeWorkflow(wf, startTask) {
  "use strict";

  var initiator = getUsername(wf.initiator);

  return {
    tasks: [],
    workflow: {
      id: wf.id,
      description: wf.description,
      dueDate: utils.toISO8601(wf.dueDate),
      initiator: initiator,
      workflowPackage: wf.workflowPackage,
      definition: {
        title: wf.definition.title,
        id: wf.definition.id
      }
    },
    startTask: startTask,
    visibility: [
      getUsername(startTask['{http://www.alfresco.org/model/bpm/1.0}assignee']),
      startTask['{http://www.alfresco.org/model/content/1.0}owner'],
      initiator
    ]
  };

}


/**
  *
  * MAIN - APPLICATION ENTRY POINT
  * returns a JSON containing all the meaningful data of tasks and workflows
  *
  */
function main() {
  "use strict";
  var workflows = getAllWorkflows();
  var tasks = getAllTasks();
  var historicTasks = getAllHistoricTasks();

  var list = {};

  _.each([].concat(historicTasks).concat(tasks), function (task) {
    var data = workflows[task.processInstanceId];
    var wf = data.wf;
    var startTaskProperties = getStartTaskProperties(data.startTask);

    if (!list[wf.id]) {
      list[wf.id] = serializeWorkflow(wf, startTaskProperties);
    }
    var serializedTask = serializeTask(task);

    list[wf.id].tasks.push(serializedTask);
    list[wf.id].visibility.push(serializedTask.assignee);
    list[wf.id].visibility.push(serializedTask.owner);
  });

  _.each(list, function (workflow) {
    var users = filterUsers(workflow.visibility);
    workflow.users = users;
    workflow.groups = getUniqueGroups(users);
    delete workflow.visibility;
  });

  return list;
}

/**
 *
 * TEMPORARY WORKAOUND TO FILTER DATA, USE PROPER JSON DOCUMENT-ORIENTED DBMS
 *
 */
function filterByGroup(groups) {
  "use strict";
  var workflows = main();
  var filteredWorkflows = {};
  groups = _.map(groups, function (group) {
    return String(group);
  });
  _.each(workflows, function (workflow, id) {
    if (_.intersection(workflow.groups, groups).length > 0) {
      filteredWorkflows[id] = workflow;
    }
  });
  return filteredWorkflows;
}

model.data = argsM.groups ? filterByGroup(argsM.groups) : main();
