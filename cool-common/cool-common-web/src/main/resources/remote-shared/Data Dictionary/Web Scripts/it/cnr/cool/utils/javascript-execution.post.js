/*global cnrutils,requestbody,model,jsonUtils,logger */
/*jslint evil:true*/
var script = "/Company Home/Data Dictionary/Scripts/thirdparty/underscore.js";
var code = cnrutils.getBean("javaScriptProcessor").loadScriptResource(script);
eval(String(code));

// http://anaykamat.com/2008/05/08/using-json-in-alfresco-webscripts/

var jsonInput = jsonUtils.toObject(requestbody.content);
var command = jsonInput.command;

var data = {};

var logs = [];
var oldLogger = logger;

var logger = {
	info: function (s) {
		"use strict";
		logs.push(s);
		oldLogger.info(s);
	},
	error: function (s) {
		"use strict";
		logs.push(s);
		oldLogger.error(s);
	}
};

try {
  data.content = eval(command);
} catch (err) {
  model.error = err.toString();
}

model.data = jsonUtils.toJSONString(data);
model.command = command;
model.logs = logs;