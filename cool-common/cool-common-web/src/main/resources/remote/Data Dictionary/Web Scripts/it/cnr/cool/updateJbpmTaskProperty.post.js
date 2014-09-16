/*global requestbody, jsonUtils, workflow, model */

var json = jsonUtils.toObject(requestbody.content),
	taskId = json.taskId,
	key = json.key,
	value = json.value,
	currentTask = workflow.getTaskById(taskId),
	taskProperties = currentTask.getProperties();

model.original = taskProperties[key];

taskProperties[key] = value;
currentTask.setProperties(taskProperties);
model.updated = taskProperties[key];