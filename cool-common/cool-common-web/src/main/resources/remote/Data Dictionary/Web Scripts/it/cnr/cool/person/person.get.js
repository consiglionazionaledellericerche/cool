/*global person, model, args, argsM, search, url, people, status, utils*/
// Get the user name of the person to get
var userName = url.extension;

// Do we want to return containing groups?
var groups = args.groups;

// Get the person who has that user name
var person = people.getPerson(userName), properties = [];
if (person !== null) {
  model.person = person;
  model.capabilities = people.getCapabilities(person);
  model.groups = groups ? people.getContainerGroups(person) : null;
  model.immutability = people.getImmutableProperties(userName);
} else {
  status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
}