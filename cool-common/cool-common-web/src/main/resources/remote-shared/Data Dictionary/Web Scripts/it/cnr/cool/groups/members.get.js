/*global people, url, args, groups, _, model */
var node = people.getGroup(url.templateArgs.group_name);
if (node) {
  model.members = people.getMembers(node);
}