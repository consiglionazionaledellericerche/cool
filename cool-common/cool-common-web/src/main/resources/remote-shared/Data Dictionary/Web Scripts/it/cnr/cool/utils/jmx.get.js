/*global args, jmx, model */
var filter = args.mBeansName;
model.mbeans = jmx.queryMBeans(filter);