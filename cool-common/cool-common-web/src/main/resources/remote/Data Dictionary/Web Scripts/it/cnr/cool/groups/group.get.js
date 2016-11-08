/*global jsonUtils,requestbody,logger,status,groupAuthority,model,_,url,args,groups */
/**
 * Get group
 */
var shortName = url.templateArgs.shortName || args.shortName,
  group = groups.getGroup(shortName);
model.group = group;